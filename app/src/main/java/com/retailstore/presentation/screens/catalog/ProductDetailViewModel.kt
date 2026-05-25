package com.retailstore.presentation.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.domain.model.Review
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.ProductRepository
import com.retailstore.domain.repository.ReviewRepository
import com.retailstore.domain.repository.UserRepository
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
    val message: String? = null,
    val isInWishlist: Boolean = false,
    val reviews: List<Review> = emptyList(),
    val currentUserId: String? = null
)

@HiltViewModel
class ProductDetailViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
    private val reviewRepository: ReviewRepository,
    private val userRepository: UserRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductDetailUiState())
    val uiState: StateFlow<ProductDetailUiState> = _uiState.asStateFlow()

    fun loadProduct(id: String) = viewModelScope.launch {
        _uiState.value = ProductDetailUiState(loading = true)
        when (val result = productRepository.getProductById(id)) {
            is Result.Success -> {
                _uiState.value = ProductDetailUiState(loading = false, product = result.data)
                checkWishlistStatus(id)
                loadReviews(id)
                loadCurrentUser()
            }
            is Result.Error -> _uiState.value = ProductDetailUiState(loading = false, error = result.message)
            else -> {}
        }
    }

    private fun checkWishlistStatus(productId: String) = viewModelScope.launch {
        if (!tokenDataStore.isLoggedIn()) return@launch
        when (val r = wishlistRepository.getWishlist()) {
            is Result.Success -> _uiState.update { it.copy(isInWishlist = r.data.any { item -> item.productId == productId }) }
            else -> {}
        }
    }

    private fun loadReviews(productId: String) = viewModelScope.launch {
        when (val r = reviewRepository.getReviews(productId)) {
            is Result.Success -> _uiState.update { it.copy(reviews = r.data) }
            else -> {}
        }
    }

    private fun loadCurrentUser() = viewModelScope.launch {
        if (!tokenDataStore.isLoggedIn()) return@launch
        when (val r = userRepository.getMe()) {
            is Result.Success -> _uiState.update { it.copy(currentUserId = r.data.id) }
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
        val isIn = _uiState.value.isInWishlist
        if (isIn) {
            wishlistRepository.removeFromWishlist(product.id)
            _uiState.update { it.copy(isInWishlist = false, message = "Удалено из избранного") }
        } else {
            wishlistRepository.addToWishlist(product.id)
            _uiState.update { it.copy(isInWishlist = true, message = "Добавлено в избранное") }
        }
    }

    fun deleteReview() = viewModelScope.launch {
        val product = _uiState.value.product ?: return@launch
        when (reviewRepository.deleteReview(product.id)) {
            is Result.Success -> {
                val currentUserId = _uiState.value.currentUserId
                _uiState.update { it.copy(
                    reviews = it.reviews.filter { r -> r.userId != currentUserId },
                    message = "Отзыв удалён"
                ) }
            }
            else -> {}
        }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
