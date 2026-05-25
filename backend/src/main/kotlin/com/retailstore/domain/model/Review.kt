package com.retailstore.domain.model

import java.time.LocalDateTime
import java.util.UUID

data class Review(
    val id: UUID,
    val productId: UUID,
    val userId: UUID,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: LocalDateTime
)
