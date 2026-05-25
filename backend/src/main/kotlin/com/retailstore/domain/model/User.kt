package com.retailstore.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class User(
    val id: UUID,
    val firebaseUid: String,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val address: String?,
    val role: String,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)
