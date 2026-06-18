package com.teguh0051.asesmen3mobro.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.teguh0051.asesmen3mobro.BuildConfig
import com.teguh0051.asesmen3mobro.R
import com.teguh0051.asesmen3mobro.model.Barang
import com.teguh0051.asesmen3mobro.model.User
import com.teguh0051.asesmen3mobro.network.UserDataStore
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(initial = User("", "", ""))
    val scope = rememberCoroutineScope()

    val viewModel: MainViewModel = viewModel()
    val barangList by viewModel.data
    
    // Sinkronisasi status login antara DataStore dan ViewModel
    LaunchedEffect(user.email) {
        if (user.email.isNotEmpty()) {
            viewModel.login(user.email)
        } else {
            viewModel.logout()
        }
    }

    var selectedBarang by remember { mutableStateOf<Barang?>(null) }
    var showDialog by remember { mutableStateOf(false) }
    var showProfilDialog by remember { mutableStateOf(false) }

    // Scaffold diletakkan di luar agar TopBar dan FAB selalu terlihat
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inventaris Barang") },
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            // Jika belum login, jalankan proses Sign-In Google
                            scope.launch { signIn(context, dataStore) }
                        } else {
                            // Jika sudah login, tampilkan dialog profil
                            showProfilDialog = true
                        }
                    }) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_account_circle),
                            contentDescription = stringResource(id = R.string.profil_desc)
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Tombol tambah tetap ada agar fitur tidak hilang
            FloatingActionButton(onClick = { showDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah")
            }
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            items(barangList) { barang ->
                BarangCard(
                    barang = barang,
                    onClick = { selectedBarang = barang },
                    onDelete = { viewModel.deleteBarang(barang.id) }
                )
            }
        }

        if (showDialog) {
            BarangDialog(
                onDismiss = { showDialog = false },
                onSave = { nama: String, lokasi: String, imageFile: File? ->
                    viewModel.addBarang(nama, lokasi, imageFile)
                    showDialog = false
                }
            )
        }

        if (showProfilDialog) {
            ProfilDialog(
                user = user,
                onDismissRequest = { showProfilDialog = false },
                onConfirmation = {
                    scope.launch {
                        signOut(context, dataStore)
                        showProfilDialog = false
                    }
                }
            )
        }

        selectedBarang?.let { barang ->
            DetailDialog(
                barang = barang,
                onDismiss = { selectedBarang = null },
                onDelete = { id ->
                    viewModel.deleteBarang(id)
                    selectedBarang = null
                }
            )
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    Log.d("SIGN-IN", "Memulai Sign-In dengan Client ID: ${BuildConfig.API_KEY}")
    
    val googleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    val credentialManager = CredentialManager.create(context)

    try {
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.message}")
        // Jika gagal, pastikan untuk memberitahu user melalui Log atau Toast
    }
}
private suspend fun handleSignIn(
    result: androidx.credentials.GetCredentialResponse,
    dataStore: UserDataStore
) {

    val credential = result.credential

    if (
        credential is CustomCredential &&
        credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
    ) {

        try {

            val googleCredential =
                GoogleIdTokenCredential.createFrom(
                    credential.data
                )

            val email =
                googleCredential.id

            val name =
                googleCredential.displayName ?: ""

            val photoUrl =
                googleCredential.profilePictureUri?.toString() ?: ""

            dataStore.saveData(
                User(
                    name,
                    email,
                    photoUrl
                )
            )

            Log.d(
                "SIGN-IN",
                "Login berhasil : $email"
            )

        } catch (e: GoogleIdTokenParsingException) {

            Log.e(
                "SIGN-IN",
                "Token parsing error",
                e
            )
        }

    } else {

        Log.e(
            "SIGN-IN",
            "Credential tidak dikenali"
        )
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    val credentialManager = CredentialManager.create(context)
    credentialManager.clearCredentialState(ClearCredentialStateRequest())
    dataStore.clearData()
    Log.d("SIGN-OUT", "Berhasil Logout")
}
