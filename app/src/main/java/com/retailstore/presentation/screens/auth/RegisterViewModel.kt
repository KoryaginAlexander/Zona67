package com.retailstore.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegisterUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun register(email: String, password: String, fullName: String) = viewModelScope.launch {
        _uiState.value = RegisterUiState(loading = true)
        when (val result = authRepository.register(email, password, fullName)) {
            is Result.Success -> _uiState.value = RegisterUiState(success = true)
            is Result.Error -> _uiState.value = RegisterUiState(error = result.message)
            else -> {}
        }
    }
}
