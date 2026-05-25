package com.retailstore.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.ProductRepository
import com.retailstore.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.*
import javax.inject.Inject

data class HomeUiState(
    val loading: Boolean = true,
    val featuredProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Int? = null,
    val cartMessage: String? = null,
    val wishlistIds: Set<String> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            launch { loadCategories() }
            launch { loadFeatured(null) }
            launch { loadWishlist() }
        }
    }

    private suspend fun loadCategories() {
        when (val r = productRepository.getCategories()) {
            is Result.Success -> _uiState.update { it.copy(categories = r.data) }
            else -> {}
        }
    }

    private suspend fun loadFeatured(categoryId: Int?) {
        _uiState.update { it.copy(loading = true) }
        when (val r = productRepository.getProducts(
            page = 1, limit = 10, categoryId = categoryId,
            activeOnly = true
        )) {
            is Result.Success -> _uiState.update { it.copy(loading = false, featuredProducts = r.data.items) }
            is Result.Error -> _uiState.update { it.copy(loading = false) }
            else -> {}
        }
    }

    private suspend fun loadWishlist() {
        if (!tokenDataStore.isLoggedIn()) return
        when (val r = wishlistRepository.getWishlist()) {
            is Result.Success -> _uiState.update { it.copy(wishlistIds = r.data.map { it.productId }.toSet()) }
            else -> {}
        }
    }

    fun filterByCategory(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
        viewModelScope.launch { loadFeatured(categoryId) }
    }

    fun toggleWishlist(product: Product) = viewModelScope.launch {
        if (!tokenDataStore.isLoggedIn()) {
            _uiState.update { it.copy(cartMessage = "Войдите, чтобы добавить в избранное") }
            return@launch
        }
        val isIn = _uiState.value.wishlistIds.contains(product.id)
        if (isIn) {
            wishlistRepository.removeFromWishlist(product.id)
            _uiState.update { it.copy(wishlistIds = _uiState.value.wishlistIds - product.id) }
        } else {
            wishlistRepository.addToWishlist(product.id)
            _uiState.update { it.copy(wishlistIds = _uiState.value.wishlistIds + product.id) }
        }
    }

    fun addToCart(product: Product) = viewModelScope.launch {
        if (tokenDataStore.isLoggedIn()) {
            cartRepository.addToCart(product.id, 1)
        } else {
            cartRepository.addToGuestCart(product.id, product.name, product.price, product.firstImageUrl, 1)
        }
        _uiState.update { it.copy(cartMessage = "${product.name} добавлен в корзину") }
    }

    fun clearCartMessage() = _uiState.update { it.copy(cartMessage = null) }
}
