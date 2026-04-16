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

data class AdminUiState(
    val shops: List<Shop> = emptyList(),
    val isLoading: Boolean = true,
    val isAdmin: Boolean = false,
    val error: String? = null,
    val message: String? = null
) {
    val pendingShops: List<Shop> get() = shops.filter { it.status == "pending" }
    val otherShops: List<Shop> get() = shops.filter { it.status != "pending" }
}

class AdminViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AdminUiState())
    val uiState: StateFlow<AdminUiState> = _uiState.asStateFlow()

    init { loadAdmin() }

    fun loadAdmin() {
        _uiState.update { it.copy(isAdmin = AuthRepository.isAdmin.value) }
        if (!AuthRepository.isAdmin.value) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shops = ShopRepository.getAllShops()
                _uiState.update { it.copy(shops = shops, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateStatus(shopId: String, status: String) {
        viewModelScope.launch {
            try {
                ShopRepository.updateShopStatus(shopId, status)
                _uiState.update { it.copy(message = "Shop $status") }
                loadAdmin() // Refresh list
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null, error = null) } }
}
