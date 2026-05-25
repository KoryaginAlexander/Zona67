package com.retailstore.presentation.screens.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Category
import com.retailstore.domain.model.Product
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminProductListUiState(
    val loading: Boolean = true,
    val allProducts: List<Product> = emptyList(),
    val filteredProducts: List<Product> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategoryId: Int? = null,
    val inStockOnly: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class AdminProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductListUiState())
    val uiState: StateFlow<AdminProductListUiState> = _uiState.asStateFlow()

    init {
        load()
        loadCategories()
    }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        when (val result = productRepository.getProducts(page = 1, limit = 200, activeOnly = false)) {
            is Result.Success -> _uiState.update { s ->
                val products = result.data.items
                s.copy(loading = false, allProducts = products, filteredProducts = applyFilters(products, s.searchQuery, s.selectedCategoryId, s.inStockOnly))
            }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }

    private fun loadCategories() = viewModelScope.launch {
        when (val result = productRepository.getCategories()) {
            is Result.Success -> _uiState.update { it.copy(categories = result.data) }
            else -> {}
        }
    }

    fun setSearch(query: String) = _uiState.update { s ->
        s.copy(searchQuery = query, filteredProducts = applyFilters(s.allProducts, query, s.selectedCategoryId, s.inStockOnly))
    }

    fun setCategory(id: Int?) = _uiState.update { s ->
        s.copy(selectedCategoryId = id, filteredProducts = applyFilters(s.allProducts, s.searchQuery, id, s.inStockOnly))
    }

    fun setInStockOnly(value: Boolean) = _uiState.update { s ->
        s.copy(inStockOnly = value, filteredProducts = applyFilters(s.allProducts, s.searchQuery, s.selectedCategoryId, value))
    }

    private fun applyFilters(products: List<Product>, query: String, categoryId: Int?, inStockOnly: Boolean): List<Product> {
        return products
            .filter { if (categoryId != null) it.categoryId == categoryId else true }
            .filter { if (inStockOnly) it.stock > 0 else true }
            .filter { if (query.isBlank()) true else it.name.contains(query, ignoreCase = true) || it.brand?.contains(query, ignoreCase = true) == true }
    }
}
