package com.nearby.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearby.app.data.model.Shop
import com.nearby.app.data.model.Product
import com.nearby.app.data.repository.AuthRepository
import com.nearby.app.data.repository.ShopRepository
import com.nearby.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DashboardUiState(
    val shop: Shop? = null,
    val products: List<Product> = emptyList(),
    val activeTab: Int = 0, // 0=orders, 1=inventory, 2=settings
    val shopName: String = "",
    val isOnline: Boolean = false,
    val imageUrl: String = "",
    val category: String = "",
    val isLoading: Boolean = true,
    val error: String? = null,
    val message: String? = null
)

class DashboardViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        val userId = AuthRepository.getUserId() ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shops = ShopRepository.getShopsByOwner(userId)
                val shop = shops.firstOrNull()
                if (shop != null) {
                    val products = ProductRepository.getProductsByShop(shop.id)
                    _uiState.update { it.copy(
                        shop = shop,
                        products = products,
                        shopName = shop.name,
                        isOnline = shop.isOnline,
                        imageUrl = shop.imageUrl ?: "",
                        category = shop.category,
                        isLoading = false
                    ) }
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    fun selectTab(tab: Int) { _uiState.update { it.copy(activeTab = tab) } }

    fun updateShopName(name: String) { _uiState.update { it.copy(shopName = name) } }
    fun updateCategory(cat: String) { _uiState.update { it.copy(category = cat) } }
    fun updateImageUrl(url: String) { _uiState.update { it.copy(imageUrl = url) } }
    fun toggleOnline() { _uiState.update { it.copy(isOnline = !it.isOnline) } }

    fun saveShopSettings() {
        val state = _uiState.value
        val shopId = state.shop?.id ?: return
        viewModelScope.launch {
            try {
                ShopRepository.updateShop(shopId, mapOf(
                    "name" to state.shopName,
                    "is_online" to state.isOnline,
                    "image_url" to state.imageUrl,
                    "category" to state.category
                ))
                _uiState.update { it.copy(
                    shop = state.shop.copy(
                        name = state.shopName,
                        isOnline = state.isOnline,
                        imageUrl = state.imageUrl,
                        category = state.category
                    ),
                    message = "Shop updated successfully"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            try {
                ProductRepository.deleteProduct(productId)
                _uiState.update { it.copy(
                    products = it.products.filter { p -> p.id != productId },
                    message = "Product deleted"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearMessage() { _uiState.update { it.copy(message = null, error = null) } }
}
