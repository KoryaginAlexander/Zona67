package com.retailstore.domain.model

data class Review(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String?,
    val createdAt: String
)
