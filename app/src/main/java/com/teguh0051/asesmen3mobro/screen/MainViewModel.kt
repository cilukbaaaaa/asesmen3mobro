package com.teguh0051.asesmen3mobro.screen

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.edit
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.teguh0051.asesmen3mobro.data.DummyData
import com.teguh0051.asesmen3mobro.model.Barang
import com.teguh0051.asesmen3mobro.network.BarangApi
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPreferences = application.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    private val _data = mutableStateOf<List<Barang>>(emptyList())
    val data: State<List<Barang>> = _data

    private val _currentUserEmail = mutableStateOf<String?>(sharedPreferences.getString("email", null))
    val currentUserEmail: State<String?> = _currentUserEmail

    // Track if we have real data from server or user
    private var hasRealData = false

    init {
        retrieveData()
    }
    private fun filterByCurrentUser(
        barangList: List<Barang>
    ): List<Barang> {

        val email = _currentUserEmail.value

        return barangList.filter {
            it.email == email
        }
    }
    fun login(email: String) {

        _currentUserEmail.value = email

        sharedPreferences.edit {
            putString("email", email)
        }

        retrieveData()
    }
    fun logout() {

        _currentUserEmail.value = null

        _data.value = emptyList()

        sharedPreferences.edit {
            remove("email")
        }
    }

    fun retrieveData() {
        viewModelScope.launch {
            try {
                val response =
                    BarangApi.service.getBarang()
                if (response.isNotEmpty()) {
                    _data.value =
                        filterByCurrentUser(response)
                    hasRealData = true
                } else if (!hasRealData) {
                    _data.value =
                        DummyData.getBarang()
                }
                Log.d(
                    "MainViewModel",
                    "Data retrieved: ${response.size} items"
                )
            } catch (e: Exception) {
                Log.e(
                    "MainViewModel",
                    "Error: ${e.message}"
                )
                if (!hasRealData) {
                    _data.value =
                        DummyData.getBarang()
                }
            }
        }
    }

    fun addBarang(nama: String, lokasi: String, imageFile: File?) {
        val email = _currentUserEmail.value ?: "anonymous"
        viewModelScope.launch {
            // 1. Create temporary item
            val tempId = System.currentTimeMillis().toString()
            val tempBarang = Barang(
                id = tempId,
                nama = nama,
                lokasi = lokasi,
                imageId = imageFile?.absolutePath ?: "",
                email = email
            )
            
            // 2. Add to list immediately and hide dummy data
            if (!hasRealData) {
                _data.value = listOf(tempBarang)
                hasRealData = true
            } else {
                _data.value = _data.value + tempBarang
            }

            try {
                if (imageFile != null) {
                    val namePart = nama.toRequestBody("text/plain".toMediaTypeOrNull())
                    val lokasiPart = lokasi.toRequestBody("text/plain".toMediaTypeOrNull())
                    val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
                    val requestFile = imageFile.asRequestBody("image/jpeg".toMediaTypeOrNull())
                    val imagePart = MultipartBody.Part.createFormData("image", imageFile.name, requestFile)

                    try {
                        BarangApi.service.uploadImage(namePart, lokasiPart, emailPart, imagePart)
                    } catch (e: Exception) {
                        // Fallback if multipart fails
                        BarangApi.service.postBarang(tempBarang.copy(id = ""))
                    }
                } else {
                    BarangApi.service.postBarang(tempBarang.copy(id = ""))
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Server sync failed: ${e.message}")
            } finally {
                // Refresh from server but don't clear the list if server is slow
                val response = try { BarangApi.service.getBarang() } catch(e: Exception) { emptyList() }
                if (response.isNotEmpty()) {
                    _data.value = response
                }
            }
        }
    }

    fun deleteBarang(id: String) {
        viewModelScope.launch {
            // Instant UI feedback
            _data.value = _data.value.filter { it.id != id }
            
            try {
                if (id.length > 5) { // Assuming dummy IDs are "1", "2", "3"
                    BarangApi.service.deleteBarang(id)
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "Delete failed: ${e.message}")
            }
        }
    }
}
