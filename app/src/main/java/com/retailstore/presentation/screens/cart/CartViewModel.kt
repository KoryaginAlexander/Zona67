package com.retailstore.presentation.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.model.Cart
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CartUiState(
    val loading: Boolean = false,
    val cart: Cart? = null,
    val isGuest: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState(loading = true))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { loadCart() }

    fun loadCart() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        val isLoggedIn = tokenDataStore.isLoggedIn()
        if (isLoggedIn) {
            when (val result = cartRepository.getCart()) {
                is Result.Success -> _uiState.update { it.copy(loading = false, cart = result.data, isGuest = false) }
                is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
                else -> {}
            }
        } else {
            _uiState.update { it.copy(loading = false, isGuest = true) }
        }
    }

    fun updateQuantity(productId: String, quantity: Int) = viewModelScope.launch {
        if (quantity <= 0) {
            cartRepository.removeFromCart(productId)
        } else {
            cartRepository.updateCartItem(productId, quantity)
        }
        loadCart()
    }

    fun removeItem(productId: String) = viewModelScope.launch {
        cartRepository.removeFromCart(productId)
        loadCart()
    }

    fun clearCart() = viewModelScope.launch {
        cartRepository.clearCart()
        loadCart()
    }
}
