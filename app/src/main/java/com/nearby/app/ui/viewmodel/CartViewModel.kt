package com.nearby.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearby.app.data.model.CartItem
import com.nearby.app.data.repository.AuthRepository
import com.nearby.app.data.repository.CartRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CartUiState(
    val items: List<CartItem> = emptyList(),
    val isLoading: Boolean = true,
    val isLoggedIn: Boolean = false,
    val error: String? = null
) {
    val total: Double get() = items.sumOf { it.productPrice * it.quantity }
    val count: Int get() = items.sumOf { it.quantity }

    /** Group cart items by shop */
    val groupedByShop: Map<String, List<CartItem>>
        get() = items.groupBy { it.shopName }
}

class CartViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CartUiState())
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init { loadCart() }

    fun loadCart() {
        val userId = AuthRepository.getUserId()
        _uiState.update { it.copy(isLoggedIn = userId != null) }
        if (userId == null) {
            _uiState.update { it.copy(items = emptyList(), isLoading = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val items = CartRepository.getCartItems(userId)
                _uiState.update { it.copy(items = items, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun updateQuantity(itemId: String, newQuantity: Int) {
        viewModelScope.launch {
            try {
                CartRepository.updateQuantity(itemId, newQuantity)
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeItem(itemId: String) {
        viewModelScope.launch {
            try {
                CartRepository.removeFromCart(itemId)
                loadCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
