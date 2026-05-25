package com.retailstore.domain.model

import java.math.BigDecimal
import java.util.UUID

data class WishlistItem(
    val id: UUID,
    val userId: UUID,
    val productId: UUID,
    val product: WishlistProduct
)

data class WishlistProduct(
    val id: UUID,
    val name: String,
    val price: BigDecimal,
    val imageUrl: String?,
    val stock: Int
)
