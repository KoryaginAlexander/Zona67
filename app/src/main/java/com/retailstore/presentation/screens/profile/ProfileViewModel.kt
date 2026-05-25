package com.retailstore.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.domain.model.Result
import com.retailstore.domain.model.User
import com.retailstore.domain.repository.AuthRepository
import com.retailstore.domain.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileUiState(
    val loading: Boolean = true,
    val user: User? = null,
    val editing: Boolean = false,
    val error: String? = null,
    val message: String? = null,
    val loggedOut: Boolean = false
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { loadProfile() }

    fun loadProfile() = viewModelScope.launch {
        _uiState.update { it.copy(loading = true) }
        when (val result = userRepository.getMe()) {
            is Result.Success -> _uiState.update { it.copy(loading = false, user = result.data) }
            is Result.Error -> _uiState.update { it.copy(loading = false, error = result.message) }
            else -> {}
        }
    }

    fun updateProfile(fullName: String, address: String) = viewModelScope.launch {
        when (val result = userRepository.updateMe(fullName.takeIf { it.isNotBlank() }, address.takeIf { it.isNotBlank() })) {
            is Result.Success -> _uiState.update { it.copy(user = result.data, editing = false, message = "Профиль обновлён") }
            is Result.Error -> _uiState.update { it.copy(error = result.message) }
            else -> {}
        }
    }

    fun logout() = viewModelScope.launch {
        authRepository.logout()
        _uiState.update { it.copy(loggedOut = true) }
    }

    fun setEditing(editing: Boolean) = _uiState.update { it.copy(editing = editing) }
    fun clearMessage() = _uiState.update { it.copy(message = null) }
}
