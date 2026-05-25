package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.UpdateUserRequest
import com.retailstore.data.remote.dto.UserDto
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @GET("users/me")
    suspend fun getMe(): Response<UserDto>

    @PATCH("users/me")
    suspend fun updateMe(@Body request: UpdateUserRequest): Response<UserDto>

    @GET("users")
    suspend fun getAllUsers(): Response<List<UserDto>>

    @GET("users/{id}")
    suspend fun getUserById(@Path("id") id: String): Response<UserDto>
}
