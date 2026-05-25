package com.retailstore.presentation.screens.wishlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.WishlistItem
import com.retailstore.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WishlistUiState(
    val loading: Boolean = false,
    val items: List<WishlistItem> = emptyList(),
    val isGuest: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class WishlistViewModel @Inject constructor(
    private val wishlistRepository: WishlistRepository,
    private val cartRepository: CartRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(WishlistUiState(loading = true))
    val uiState: StateFlow<WishlistUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        if (!tokenDataStore.isLoggedIn()) {
            _uiState.update { it.copy(loading = false, isGuest = true) }
            return@launch
        }
        when (val result = wishlistRepository.getWishlist()) {
            is Result.Success -> _uiState.update { it.copy(loading = false, items = result.data) }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }

    fun remove(productId: String) = viewModelScope.launch {
        wishlistRepository.removeFromWishlist(productId)
        load()
    }

    fun addToCart(item: WishlistItem) = viewModelScope.launch {
        if (tokenDataStore.isLoggedIn()) {
            cartRepository.addToCart(item.productId, 1)
        } else {
            cartRepository.addToGuestCart(item.productId, item.productName, item.productPrice, item.productImageUrl, 1)
        }
        _uiState.update { it.copy(message = "${item.productName} добавлен в корзину") }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
