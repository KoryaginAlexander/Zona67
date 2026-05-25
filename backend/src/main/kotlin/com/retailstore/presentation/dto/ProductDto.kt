package com.retailstore.presentation.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductSpecDto(val key: String, val value: String)

@Serializable
data class CreateProductRequest(
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int = 0,
    val brand: String? = null,
    val model: String? = null,
    val imageUrls: List<String> = emptyList(),
    val specs: List<ProductSpecDto> = emptyList()
)

@Serializable
data class UpdateProductRequest(
    val categoryId: Int? = null,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val stock: Int? = null,
    val brand: String? = null,
    val model: String? = null,
    val imageUrls: List<String>? = null,
    val specs: List<ProductSpecDto>? = null
)

@Serializable
data class ProductResponse(
    val id: String,
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val price: Double,
    val stock: Int,
    val brand: String? = null,
    val model: String? = null,
    val imageUrls: List<String> = emptyList(),
    val isActive: Boolean,
    val specs: List<ProductSpecDto> = emptyList(),
    val averageRating: Double = 0.0,
    val reviewCount: Int = 0
)

@Serializable
data class ProductsPageResponse(
    val items: List<ProductResponse>,
    val total: Int,
    val page: Int,
    val limit: Int
)
