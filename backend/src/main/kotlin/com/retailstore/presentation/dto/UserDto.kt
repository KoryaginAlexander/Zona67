package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class UpdateUserRequest(
    val fullName: String? = null,
    val phone: String? = null,
    val address: String? = null
)
