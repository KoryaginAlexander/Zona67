package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class CategoryRequest(
    val name: String,
    val imageUrl: String? = null
)

@Serializable
data class CategoryResponse(
    val id: Int,
    val name: String,
    val slug: String,
    val imageUrl: String? = null
)
