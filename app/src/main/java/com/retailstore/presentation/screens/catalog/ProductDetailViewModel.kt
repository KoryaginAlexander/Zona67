package com.retailstore.presentation.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.ProductRepository
import com.retailstore.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProductDetailUiState(
    val loading: Boolean = true,
    val product: Product? = null,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(id: String) = viewModelScope.launch {
        _uiState.value = ProductDetailUiState(loading = true)
        when (val result = productRepository.getProductById(id)) {
            is Result.Success -> _uiState.value = ProductDetailUiState(loading = false, product = result.data)
            is Result.Error -> _uiState.value = ProductDetailUiState(loading = false, error = result.message)
            else -> {}
        }
    }

    fun addToCart() = viewModelScope.launch {
        val product = _uiState.value.product ?: return@launch
        if (tokenDataStore.isLoggedIn()) {
            cartRepository.addToCart(product.id, 1)
        } else {
            cartRepository.addToGuestCart(product.id, product.name, product.price, product.firstImageUrl, 1)
        }
        _uiState.update { it.copy(message = "Добавлено в корзину") }
    }

    fun toggleWishlist() = viewModelScope.launch {
        val product = _uiState.value.product ?: return@launch
        if (!tokenDataStore.isLoggedIn()) {
            _uiState.update { it.copy(message = "Войдите, чтобы добавить в избранное") }
            return@launch
        }
        wishlistRepository.addToWishlist(product.id)
        _uiState.update { it.copy(message = "Добавлено в избранное") }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
