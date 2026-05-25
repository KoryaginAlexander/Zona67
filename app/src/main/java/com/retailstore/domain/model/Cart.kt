package com.retailstore.domain.model

data class CartItem(
    val id: String,
    val productId: String,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String?,
    val stock: Int,
    val quantity: Int
)

data class Cart(
    val items: List<CartItem>,
    val total: Double
) {
    val itemCount: Int get() = items.sumOf { it.quantity }
}
