package com.retailstore.domain.repository

import com.retailstore.domain.model.Result
import com.retailstore.domain.model.Review

interface ReviewRepository {
    suspend fun getReviews(productId: String): Result<List<Review>>
    suspend fun addReview(productId: String, rating: Int, comment: String?): Result<Review>
    suspend fun deleteReview(productId: String): Result<Unit>
}
