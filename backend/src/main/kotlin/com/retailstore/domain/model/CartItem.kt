package com.retailstore.domain.model

import java.math.BigDecimal
import java.util.UUID

data class CartItem(
    val id: UUID,
    val userId: UUID,
    val productId: UUID,
    val quantity: Int,
    val product: CartProduct
)

data class CartProduct(
    val id: UUID,
    val name: String,
    val price: BigDecimal,
    val imageUrl: String?,
    val stock: Int
)
