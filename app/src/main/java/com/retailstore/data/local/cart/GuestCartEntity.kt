package com.retailstore.data.local.cart

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "guest_cart")
data class GuestCartEntity(
    @PrimaryKey val productId: String,
    val productName: String,
    val productPrice: Double,
    val imageUrl: String?,
    val quantity: Int
)
