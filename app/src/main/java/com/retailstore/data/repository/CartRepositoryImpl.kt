package com.retailstore.data.repository

import com.retailstore.data.local.TokenDataStore
import com.retailstore.data.local.cart.GuestCartDao
import com.retailstore.data.local.cart.GuestCartEntity
import com.retailstore.data.remote.api.CartApi
import com.retailstore.data.remote.dto.*
import com.retailstore.domain.model.Cart
import com.retailstore.domain.model.CartItem
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepositoryImpl @Inject constructor(
    private val cartApi: CartApi,
    private val guestCartDao: GuestCartDao,
    private val tokenDataStore: TokenDataStore
) : CartRepository {

    override suspend fun getCart(): Result<Cart> = try {
        val response = cartApi.getCart()
        if (response.isSuccessful) {
            Result.Success(response.body()!!.toDomain())
        } else {
            Result.Error(response.code(), "Failed to load cart")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun addToCart(productId: String, quantity: Int): Result<CartItem> = try {
        val response = cartApi.addItem(AddCartItemRequest(productId, quantity))
        if (response.isSuccessful) {
            Result.Success(response.body()!!.toDomain())
        } else {
            Result.Error(response.code(), "Failed to add item")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun updateCartItem(productId: String, quantity: Int): Result<CartItem> = try {
        val response = cartApi.updateItem(productId, UpdateCartItemRequest(quantity))
        if (response.isSuccessful) {
            Result.Success(response.body()!!.toDomain())
        } else {
            Result.Error(response.code(), "Failed to update cart")
        }
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun removeFromCart(productId: String): Result<Unit> = try {
        cartApi.removeItem(productId)
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun clearCart(): Result<Unit> = try {
        cartApi.clearCart()
        Result.Success(Unit)
    } catch (e: Exception) {
        Result.Error(message = e.message ?: "Unknown error")
    }

    override suspend fun mergeGuestCart(): Result<Cart> {
        return try {
            val guestItems = guestCartDao.getAll().first()
            if (guestItems.isEmpty()) {
                return getCart()
            }
            val mergeItems = guestItems.map { MergeItemDto(it.productId, it.quantity) }
            val response = cartApi.mergeCart(MergeCartRequest(mergeItems))
            if (response.isSuccessful) {
                guestCartDao.deleteAll()
                Result.Success(response.body()!!.toDomain())
            } else {
                Result.Error(response.code(), "Failed to merge cart")
            }
        } catch (e: Exception) {
            Result.Error(message = e.message ?: "Unknown error")
        }
    }

    override fun getGuestCartCount(): Flow<Int> =
        guestCartDao.getTotalQuantity().map { it ?: 0 }

    override suspend fun addToGuestCart(productId: String, productName: String, productPrice: Double, imageUrl: String?, quantity: Int) {
        val existing = guestCartDao.findByProductId(productId)
        val newQty = (existing?.quantity ?: 0) + quantity
        guestCartDao.upsert(GuestCartEntity(productId, productName, productPrice, imageUrl, newQty))
    }

    override suspend fun clearGuestCart() = guestCartDao.deleteAll()
}

private fun CartDto.toDomain() = Cart(
    items = items.map { it.toDomain() },
    total = total
)

private fun CartItemDto.toDomain() = CartItem(
    id = id, productId = productId, productName = productName,
    productPrice = productPrice, productImageUrl = productImageUrl,
    stock = stock, quantity = quantity
)
