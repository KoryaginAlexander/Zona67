package com.retailstore.domain.model

import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.UUID

data class Product(
    val id: UUID,
    val categoryId: Int,
    val name: String,
    val description: String?,
    val price: BigDecimal,
    val stock: Int,
    val brand: String?,
    val model: String?,
    val imageUrls: List<String>,
    val isActive: Boolean,
    val specs: List<ProductSpec>,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
)

data class ProductSpec(
    val key: String,
    val value: String
)
