package com.nearby.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearby.app.data.model.Shop
import com.nearby.app.data.repository.AuthRepository
import com.nearby.app.data.repository.ShopRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ProfileUiState(
    val email: String = "",
    val isAdmin: Boolean = false,
    val isShopkeeper: Boolean = false,
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val shops: List<Shop> = emptyList()
)

class ProfileViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        val userId = AuthRepository.getUserId()
        _uiState.update { it.copy(
            isLoggedIn = userId != null,
            email = AuthRepository.currentUser.value?.email ?: "",
            isAdmin = AuthRepository.isAdmin.value,
            isShopkeeper = AuthRepository.isShopkeeper.value
        ) }

        if (userId == null) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            try {
                val shops = ShopRepository.getShopsByOwner(userId)
                _uiState.update { it.copy(shops = shops, isLoading = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun signOut(onDone: () -> Unit) {
        viewModelScope.launch {
            AuthRepository.signOut()
            _uiState.update { ProfileUiState() }
            onDone()
        }
    }
}
