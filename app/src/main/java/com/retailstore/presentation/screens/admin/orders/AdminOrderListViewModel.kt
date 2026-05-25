package com.retailstore.presentation.screens.admin.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Order
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.OrderRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminOrderListUiState(
    val loading: Boolean = true,
    val orders: List<Order> = emptyList(),
    val selectedStatus: String? = null,
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class AdminOrderListViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderListUiState())
    val uiState: StateFlow<AdminOrderListUiState> = _uiState.asStateFlow()

    private var allOrders: List<Order> = emptyList()

    init { load() }

    fun load() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        when (val result = orderRepository.getAllOrders(null)) {
            is Result.Success -> {
                allOrders = result.data
                _uiState.update { it.copy(loading = false, orders = applyFilters(allOrders, it.selectedStatus, it.searchQuery)) }
            }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }

    fun setStatus(status: String?) {
        _uiState.update { it.copy(selectedStatus = status, orders = applyFilters(allOrders, status, it.searchQuery)) }
    }

    fun setSearch(query: String) {
        _uiState.update { it.copy(searchQuery = query, orders = applyFilters(allOrders, it.selectedStatus, query)) }
    }

    private fun applyFilters(orders: List<Order>, status: String?, query: String): List<Order> {
        return orders
            .filter { if (status != null) it.status == status else true }
            .filter {
                if (query.isBlank()) true
                else it.id.takeLast(8).contains(query, ignoreCase = true)
                        || it.userEmail.contains(query, ignoreCase = true)
            }
    }
}
