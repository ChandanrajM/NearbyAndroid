package com.nearby.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearby.app.data.model.CartItem
import com.nearby.app.data.model.OrderItemInsert
import com.nearby.app.data.repository.AuthRepository
import com.nearby.app.data.repository.CartRepository
import com.nearby.app.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CheckoutUiState(
    val items: List<CartItem> = emptyList(),
    val showSummary: Boolean = false,
    val showReceipt: Boolean = false,
    val showSuccess: Boolean = false,
    val isProcessing: Boolean = false,
    val error: String? = null
) {
    val total: Double get() = items.sumOf { it.productPrice * it.quantity }
    val shopName: String get() = items.firstOrNull()?.shopName ?: "Nearby Shop"
    val shopId: String get() = items.firstOrNull()?.shopId ?: ""
}

class CheckoutViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState())
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init { loadCart() }

    private fun loadCart() {
        val userId = AuthRepository.getUserId() ?: return
        viewModelScope.launch {
            try {
                val items = CartRepository.getCartItems(userId)
                _uiState.update { it.copy(items = items) }
            } catch (_: Exception) {}
        }
    }

    fun toggleSummary() { _uiState.update { it.copy(showSummary = !it.showSummary) } }
    fun showReceipt() { _uiState.update { it.copy(showReceipt = true) } }
    fun hideReceipt() { _uiState.update { it.copy(showReceipt = false) } }
    fun hideSuccess() { _uiState.update { it.copy(showSuccess = false) } }

    fun placeOrder(onNotLoggedIn: () -> Unit) {
        val userId = AuthRepository.getUserId()
        if (userId == null) {
            onNotLoggedIn()
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null) }
            try {
                val state = _uiState.value
                val orderItems = state.items.map { item ->
                    OrderItemInsert(
                        orderId = "", // will be set by repository
                        productId = item.productId,
                        productName = item.productName,
                        quantity = item.quantity,
                        price = item.productPrice
                    )
                }
                OrderRepository.createOrderTransaction(
                    userId = userId,
                    shopId = state.shopId,
                    total = state.total,
                    items = orderItems
                )
                CartRepository.clearCart(userId)
                _uiState.update { it.copy(showSuccess = true, isProcessing = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isProcessing = false) }
            }
        }
    }
}
