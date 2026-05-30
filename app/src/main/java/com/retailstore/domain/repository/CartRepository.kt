package com.retailstore.domain.repository

import com.retailstore.domain.model.Cart
import com.retailstore.domain.model.CartItem
import com.retailstore.domain.model.Result

interface CartRepository {
    suspend fun getCart(): Result<Cart>
    suspend fun addToCart(productId: String, quantity: Int): Result<CartItem>
    suspend fun updateCartItem(productId: String, quantity: Int): Result<CartItem>
    suspend fun removeFromCart(productId: String): Result<Unit>
    suspend fun clearCart(): Result<Unit>
    suspend fun mergeGuestCart(): Result<Cart>
    fun getGuestCartCount(): kotlinx.coroutines.flow.Flow<Int>
    fun observeCartCount(): kotlinx.coroutines.flow.Flow<Int>
    suspend fun addToGuestCart(productId: String, productName: String, productPrice: Double, imageUrl: String?, quantity: Int)
    suspend fun updateGuestCartItem(productId: String, quantity: Int)
    suspend fun removeFromGuestCart(productId: String)
    suspend fun clearGuestCart()
    fun getGuestCartFlow(): kotlinx.coroutines.flow.Flow<Cart>
}
