package com.retailstore.data.remote.api

import com.retailstore.data.remote.dto.CreateReviewRequest
import com.retailstore.data.remote.dto.ReviewDto
import retrofit2.Response
import retrofit2.http.*

interface ReviewApi {
    @GET("products/{productId}/reviews")
    suspend fun getReviews(@Path("productId") productId: String): Response<List<ReviewDto>>

    @POST("products/{productId}/reviews")
    suspend fun addReview(
        @Path("productId") productId: String,
        @Body request: CreateReviewRequest
    ): Response<ReviewDto>

    @DELETE("products/{productId}/reviews")
    suspend fun deleteReview(@Path("productId") productId: String): Response<Unit>
}
