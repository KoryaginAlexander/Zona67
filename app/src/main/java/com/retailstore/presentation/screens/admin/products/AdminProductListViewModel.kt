package com.retailstore.presentation.screens.admin.products

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.remote.api.ProductApi
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
    val products: List<Product> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class AdminProductListViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductListUiState())
    val uiState: StateFlow<AdminProductListUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        when (val result = productRepository.getProducts(page = 1, limit = 100, activeOnly = false)) {
            is Result.Success -> _uiState.update { it.copy(loading = false, products = result.data.items) }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }
}
