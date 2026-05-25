package com.retailstore.data.repository

import com.retailstore.data.remote.api.ReviewApi
import com.retailstore.data.remote.dto.CreateReviewRequest
import com.retailstore.domain.model.Result
import com.retailstore.domain.model.Review
import com.retailstore.domain.repository.ReviewRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepositoryImpl @Inject constructor(
    private val reviewApi: ReviewApi
) : ReviewRepository {

    override suspend fun getReviews(productId: String): Result<List<Review>> = try {
        val response = reviewApi.getReviews(productId)
        if (response.isSuccessful) {
            Result.Success(response.body()!!.map { it.toDomain() })
        } else {
            Result.Error(response.code(), "Не удалось загрузить отзывы")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun addReview(productId: String, rating: Int, comment: String?): Result<Review> = try {
        val response = reviewApi.addReview(productId, CreateReviewRequest(rating, comment?.takeIf { it.isNotBlank() }))
        if (response.isSuccessful) {
            Result.Success(response.body()!!.toDomain())
        } else if (response.code() == 409) {
            Result.Error(409, "Вы уже оставили отзыв на этот товар")
        } else {
            Result.Error(response.code(), "Не удалось сохранить отзыв")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun deleteReview(productId: String): Result<Unit> = try {
        reviewApi.deleteReview(productId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }
}

private fun com.retailstore.data.remote.dto.ReviewDto.toDomain() = Review(
    id = id, productId = productId, userId = userId,
    userName = userName, rating = rating, comment = comment, createdAt = createdAt
)
