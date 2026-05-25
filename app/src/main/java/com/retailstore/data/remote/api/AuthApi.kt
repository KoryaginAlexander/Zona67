package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.*
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): Response<Map<String, String>>

    @POST("auth/logout")
    suspend fun logout(@Body request: LogoutRequest): Response<Unit>
}
