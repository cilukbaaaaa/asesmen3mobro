package com.teguh0051.asesmen3mobro.network

import com.teguh0051.asesmen3mobro.model.Barang
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface BarangApiService {
    @GET("barang")
    suspend fun getBarang(): List<Barang>

    @POST("barang")
    suspend fun postBarang(
        @Body barang: Barang
    ): Barang

    @Multipart
    @POST("barang")
    suspend fun uploadImage(
        @Part("nama") nama: RequestBody,
        @Part("lokasi") lokasi: RequestBody,
        @Part("email") email: RequestBody,
        @Part image: MultipartBody.Part
    ): Barang

    @DELETE("barang/{id}")
    suspend fun deleteBarang(@Path("id") id: String): Barang
}