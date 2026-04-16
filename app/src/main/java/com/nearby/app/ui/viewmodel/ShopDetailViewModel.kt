package com.nearby.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearby.app.data.model.CartItem
import com.nearby.app.data.model.Product
import com.nearby.app.data.model.Shop
import com.nearby.app.data.repository.CartRepository
import com.nearby.app.data.repository.ProductRepository
import com.nearby.app.data.repository.ShopRepository
import com.nearby.app.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ShopDetailUiState(
    val shop: Shop? = null,
    val products: List<Product> = emptyList(),
    val cartItems: List<CartItem> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

class ShopDetailViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ShopDetailUiState())
    val uiState: StateFlow<ShopDetailUiState> = _uiState.asStateFlow()

    fun loadShop(shopId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shop = ShopRepository.getShopById(shopId)
                val products = ProductRepository.getProductsByShop(shopId)
                _uiState.update { it.copy(shop = shop, products = products, isLoading = false) }
                refreshCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun addToCart(productId: String, shopId: String) {
        val userId = AuthRepository.getUserId() ?: return
        viewModelScope.launch {
            try {
                CartRepository.addToCart(userId, productId, shopId)
                refreshCart()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun removeFromCart(productId: String) {
        val userId = AuthRepository.getUserId() ?: return
        viewModelScope.launch {
            val item = _uiState.value.cartItems.find { it.productId == productId } ?: return@launch
            if (item.quantity <= 1) {
                CartRepository.removeFromCart(item.id)
            } else {
                CartRepository.updateQuantity(item.id, item.quantity - 1)
            }
            refreshCart()
        }
    }

    private suspend fun refreshCart() {
        val userId = AuthRepository.getUserId() ?: return
        val items = CartRepository.getCartItems(userId)
        _uiState.update { it.copy(cartItems = items) }
    }

    fun getQuantity(productId: String): Int {
        return _uiState.value.cartItems.find { it.productId == productId }?.quantity ?: 0
    }

    val cartCount: Int get() = _uiState.value.cartItems.sumOf { it.quantity }
    val cartTotal: Double get() = _uiState.value.cartItems.sumOf { it.productPrice * it.quantity }

    fun clearError() { _uiState.update { it.copy(error = null) } }
}
