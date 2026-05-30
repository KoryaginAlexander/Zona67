package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.ImageUploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ImageApi {
    @Multipart
    @POST("images")
    suspend fun uploadImage(@Part image: MultipartBody.Part): Response<ImageUploadResponse>
}
