package com.retailstore.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.retailstore.data.local.TokenDataStore
import com.retailstore.domain.repository.CartRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val tokenDataStore: TokenDataStore,
    private val cartRepository: CartRepository
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = tokenDataStore.accessToken
        .map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)

    val cartCount: StateFlow<Int> = cartRepository.observeCartCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)
}
