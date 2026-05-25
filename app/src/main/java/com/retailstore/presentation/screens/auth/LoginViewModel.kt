package com.retailstore.presentation.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.retailstore.domain.model.Result
import com.retailstore.domain.repository.AuthRepository
import com.retailstore.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val cartRepository: CartRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) = viewModelScope.launch {
        _uiState.value = LoginUiState(loading = true)
        try {
            val firebaseResult = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val idToken = firebaseResult.user?.getIdToken(false)?.await()?.token
            if (idToken == null) {
                _uiState.value = LoginUiState(error = "Не удалось получить токен Firebase")
                return@launch
            }
            when (val result = authRepository.login(idToken)) {
                is Result.Success -> {
                    cartRepository.mergeGuestCart()
                    _uiState.value = LoginUiState(success = true)
                }
                is Result.Error -> _uiState.value = LoginUiState(error = result.message)
                else -> {}
            }
        } catch (e: Exception) {
            _uiState.value = LoginUiState(error = e.message ?: "Ошибка входа")
        }
    }
}
