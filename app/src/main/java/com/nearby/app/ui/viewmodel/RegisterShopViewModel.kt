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

data class RegisterShopUiState(
    val name: String = "",
    val ownerName: String = "",
    val category: String = "",
    val phone: String = "",
    val gstNumber: String = "",
    val address: String = "",
    val description: String = "",
    val isLoading: Boolean = false,
    val isSubmitted: Boolean = false,
    val error: String? = null,
    val isLoggedIn: Boolean = false
)

class RegisterShopViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterShopUiState())
    val uiState: StateFlow<RegisterShopUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isLoggedIn = AuthRepository.getUserId() != null) }
    }

    fun updateField(field: String, value: String) {
        _uiState.update {
            when (field) {
                "name" -> it.copy(name = value)
                "ownerName" -> it.copy(ownerName = value)
                "category" -> it.copy(category = value)
                "phone" -> it.copy(phone = value)
                "gstNumber" -> it.copy(gstNumber = value)
                "address" -> it.copy(address = value)
                "description" -> it.copy(description = value)
                else -> it
            }
        }
    }

    fun submit() {
        val state = _uiState.value
        val userId = AuthRepository.getUserId()
        if (userId == null) {
            _uiState.update { it.copy(error = "Please sign in first") }
            return
        }
        if (state.name.isBlank() || state.ownerName.isBlank() || state.category.isBlank() ||
            state.phone.isBlank() || state.address.isBlank()) {
            _uiState.update { it.copy(error = "Please fill all required fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                ShopRepository.createShop(Shop(
                    ownerId = userId,
                    name = state.name,
                    ownerName = state.ownerName,
                    category = state.category,
                    phone = state.phone,
                    gstNumber = state.gstNumber.ifBlank { null },
                    address = state.address,
                    description = state.description.ifBlank { null }
                ))
                _uiState.update { it.copy(isSubmitted = true, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message, isLoading = false) }
            }
        }
    }

    companion object {
        val CATEGORIES = listOf(
            "Bakery", "Electronics", "Grocery", "Fashion", "Books",
            "Food & Restaurant", "Services", "Pharmacy", "Hardware", "Stationery", "Other"
        )
    }
}
