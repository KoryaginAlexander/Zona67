package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val firebaseIdToken: String, val fullName: String? = null)

@Serializable
data class RefreshRequest(val refreshToken: String)

@Serializable
data class LogoutRequest(val refreshToken: String)

@Serializable
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class UserResponse(
    val id: String,
    val email: String,
    val fullName: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val role: String
)
