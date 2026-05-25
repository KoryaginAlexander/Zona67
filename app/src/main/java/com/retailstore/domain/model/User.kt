package com.retailstore.domain.model

data class User(
    val id: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val address: String?,
    val role: String
) {
    val isAdmin: Boolean get() = role == "ADMIN"
}
