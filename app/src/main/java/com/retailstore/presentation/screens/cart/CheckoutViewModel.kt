package com.retailstore.presentation.screens.cart

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.CartRepository
import com.retailstore.domain.repository.OrderRepository
import com.retailstore.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CheckoutUiState(
    val loading: Boolean = false,
    val savedAddress: String = "",
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class CheckoutViewModel @Inject constructor(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val cartRepository: CartRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            when (val result = userRepository.getMe()) {
                is Result.Success -> _uiState.update { it.copy(savedAddress = result.data.address ?: "") }
                else -> {}
            }
        }
    }

    fun placeOrder(address: String, comment: String) = viewModelScope.launch {
        _uiState.update { it.copy(loading = true, error = null) }
        when (val result = orderRepository.placeOrder(address.takeIf { it.isNotBlank() }, comment.takeIf { it.isNotBlank() })) {
            is Result.Success -> {
                cartRepository.clearCart()
                _uiState.update { it.copy(loading = false, success = true) }
            }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }
}
