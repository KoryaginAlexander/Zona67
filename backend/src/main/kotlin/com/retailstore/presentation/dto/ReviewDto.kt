package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class ReviewResponse(
    val id: String,
    val productId: String,
    val userId: String,
    val userName: String,
    val rating: Int,
    val comment: String? = null,
    val createdAt: String
)

@Serializable
data class CreateReviewRequest(
    val rating: Int,
    val comment: String? = null
)
