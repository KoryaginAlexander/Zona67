package com.retailstore.domain.repository

import com.retailstore.domain.model.Review
import java.util.UUID

interface ReviewRepository {
    suspend fun getReviews(productId: UUID): List<Review>
    suspend fun addReview(productId: UUID, userId: UUID, rating: Int, comment: String?): Review
    suspend fun deleteReview(productId: UUID, userId: UUID): Boolean
    suspend fun hasReviewed(productId: UUID, userId: UUID): Boolean
}
