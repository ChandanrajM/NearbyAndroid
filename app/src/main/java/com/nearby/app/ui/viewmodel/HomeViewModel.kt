package com.nearby.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nearby.app.data.model.Shop
import com.nearby.app.data.model.ProductWithShop
import com.nearby.app.data.repository.ShopRepository
import com.nearby.app.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val shops: List<Shop> = emptyList(),
    val trendingProducts: List<ProductWithShop> = emptyList(),
    val locationName: String = "Your Location",
    val searchQuery: String = "",
    val selectedCategory: String = "",
    val isLoading: Boolean = true
)

class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadData() }

    fun loadData() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val shops = ShopRepository.getApprovedShops()
                val trending = ProductRepository.getTrending(6)
                _uiState.update { it.copy(
                    shops = shops,
                    trendingProducts = trending,
                    isLoading = false
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun updateSearch(query: String) { _uiState.update { it.copy(searchQuery = query) } }

    fun selectCategory(category: String) {
        _uiState.update {
            it.copy(selectedCategory = if (it.selectedCategory == category) "" else category)
        }
    }

    fun updateLocation(name: String) { _uiState.update { it.copy(locationName = name) } }

    fun filteredShops(): List<Shop> {
        val state = _uiState.value
        return state.shops.filter { shop ->
            val matchSearch = shop.name.contains(state.searchQuery, ignoreCase = true)
            val matchCat = state.selectedCategory.isEmpty() ||
                shop.category.equals(state.selectedCategory, ignoreCase = true)
            matchSearch && matchCat
        }
    }
}
