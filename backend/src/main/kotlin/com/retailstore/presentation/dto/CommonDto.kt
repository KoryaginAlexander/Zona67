package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(val error: String, val message: String, val statusCode: Int)

@Serializable
data class MessageResponse(val message: String)

@Serializable
data class AccessTokenResponse(val accessToken: String)
