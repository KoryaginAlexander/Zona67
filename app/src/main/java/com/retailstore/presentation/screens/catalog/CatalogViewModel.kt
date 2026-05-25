package com.retailstore.presentation.screens.catalog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.ProductRepository
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
    val cartMessage: String? = null
)

@OptIn(FlowPreview::class)
@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val cartRepository: CartRepository,
    private val tokenDataStore: TokenDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(CatalogUiState(loading = true))
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private val searchFlow = MutableStateFlow("")
    private var loadJob: Job? = null

    init {
        loadCategories()
        searchFlow.debounce(300).onEach { loadProducts(resetPage = true) }.launchIn(viewModelScope)
        loadProducts(resetPage = true)
    }

    private fun loadCategories() = viewModelScope.launch {
        when (val result = productRepository.getCategories()) {
            is Result.Success -> _uiState.update { it.copy(categories = result.data) }
            else -> {}
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

    fun setFilters(brand: String?, minPrice: Double?, maxPrice: Double?) {
        _uiState.update { it.copy(brand = brand, minPrice = minPrice, maxPrice = maxPrice) }
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
                    val newProducts = if (resetPage) result.data.items
                    else state.products + result.data.items
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
