package com.retailstore.presentation.screens.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.ProductRepository
import com.retailstore.domain.repository.WishlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val loading: Boolean = false,
    val products: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val selectedCategory: Int? = null,
    val searchQuery: String = "",
    val brand: String? = null,
    val minPrice: Double? = null,
    val maxPrice: Double? = null,
    val sortBy: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
    val isLastPage: Boolean = false,
    val error: String? = null,
    val cartMessage: String? = null,
    val wishlistIds: Set<String> = emptySet(),
    val inStockOnly: Boolean = false
)

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val wishlistRepository: WishlistRepository,
    private val tokenDataStore: TokenDataStore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState(loading = true))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        val initialQuery = savedStateHandle.get<String>("query") ?: ""
        if (initialQuery.isNotBlank()) {
            _uiState.update { it.copy(searchQuery = initialQuery) }
            searchFlow.value = initialQuery
        }
        loadCategories()
        loadWishlist()
        searchFlow.debounce(300).onEach { loadProducts(resetPage = true) }.launchIn(viewModelScope)
        loadProducts(resetPage = true)
    }

    private fun loadCategories() = viewModelScope.launch {
        when (val result = productRepository.getCategories()) {
            is Result.Success -> _uiState.update { it.copy(categories = result.data) }
            else -> {}
        }
    }

    private fun loadWishlist() = viewModelScope.launch {
        if (!tokenDataStore.isLoggedIn()) return@launch
        when (val r = wishlistRepository.getWishlist()) {
            is Result.Success -> _uiState.update { it.copy(wishlistIds = r.data.map { it.productId }.toSet()) }
            else -> {}
        }
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

    fun setSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchFlow.value = query
    }

    fun setCategory(categoryId: Int?) {
        _uiState.update { it.copy(selectedCategory = categoryId) }
        loadProducts(resetPage = true)
    }

    fun setSortBy(sort: String?) {
        _uiState.update { it.copy(sortBy = sort) }
        loadProducts(resetPage = true)
    }

    fun setFilters(brand: String?, minPrice: Double?, maxPrice: Double?, inStockOnly: Boolean = false, sortBy: String? = null) {
        _uiState.update { it.copy(brand = brand, minPrice = minPrice, maxPrice = maxPrice, inStockOnly = inStockOnly, sortBy = sortBy) }
        loadProducts(resetPage = true)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (!state.isLastPage && !state.loading) {
            loadProducts(resetPage = false)
        }
    }

    fun loadProducts(resetPage: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val state = _uiState.value
            val page = if (resetPage) 1 else state.currentPage + 1
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = productRepository.getProducts(
                page = page, limit = 20,
                categoryId = state.selectedCategory,
                brand = state.brand,
                minPrice = state.minPrice,
                maxPrice = state.maxPrice,
                search = state.searchQuery.takeIf { it.isNotBlank() },
                sortBy = state.sortBy,
                activeOnly = true
            )) {
                is Result.Success -> {
                    val filtered = if (state.inStockOnly)
                        result.data.items.filter { it.stock > 0 }
                    else result.data.items
                    val newProducts = if (resetPage) filtered else state.products + filtered
                    val totalPages = (result.data.total + 19) / 20
                    _uiState.update {
                        it.copy(
                            loading = false,
                            products = newProducts,
                            currentPage = page,
                            totalPages = totalPages,
                            isLastPage = page >= totalPages
                        )
                    }
                }
                is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
                else -> {}
            }
        }
    }

    fun addToCart(product: Product) = viewModelScope.launch {
        if (tokenDataStore.isLoggedIn()) {
            cartRepository.addToCart(product.id, 1)
        } else {
            cartRepository.addToGuestCart(
                product.id, product.name, product.price, product.firstImageUrl, 1
            )
        }
        _uiState.update { it.copy(cartMessage = "${product.name} добавлен в корзину") }
    }

    fun clearCartMessage() = _uiState.update { it.copy(cartMessage = null) }
}
