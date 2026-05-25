package com.retailstore.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ReviewDto(
    @SerializedName("id") val id: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("userName") val userName: String,
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?,
    @SerializedName("createdAt") val createdAt: String
)

data class CreateReviewRequest(
    @SerializedName("rating") val rating: Int,
    @SerializedName("comment") val comment: String?
)
