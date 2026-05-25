package com.retailstore.domain.model

data class Product(
    val id: String,
    val categoryId: Int,
    val name: String,
    val description: String?,
    val price: Double,
    val stock: Int,
    val brand: String?,
    val model: String?,
    val imageUrls: List<String>,
    val isActive: Boolean,
    val specs: List<ProductSpec>
) {
    val isInStock: Boolean get() = stock > 0
    val firstImageUrl: String? get() = imageUrls.firstOrNull()
}

data class ProductSpec(val key: String, val value: String)

data class ProductsPage(
    val items: List<Product>,
    val total: Int,
    val page: Int,
    val limit: Int
)
