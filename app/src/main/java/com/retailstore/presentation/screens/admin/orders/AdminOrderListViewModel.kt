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
    val error: String? = null
)

@HiltViewModel
class AdminOrderListViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminOrderListUiState())
    val uiState: StateFlow<AdminOrderListUiState> = _uiState.asStateFlow()

    init { load(null) }

    fun load(status: String?) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, selectedStatus = status) }
        when (val result = orderRepository.getAllOrders(status)) {
            is Result.Success -> _uiState.update { it.copy(loading = false, orders = result.data) }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }
}
