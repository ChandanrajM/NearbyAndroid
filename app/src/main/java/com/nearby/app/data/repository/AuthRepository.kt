package com.nearby.app.data.repository

import com.nearby.app.data.SupabaseClient
import com.nearby.app.data.model.UserRole
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.user.UserInfo
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

object AuthRepository {

    private val _currentUser = MutableStateFlow<UserInfo?>(null)
    val currentUser: StateFlow<UserInfo?> = _currentUser

    private val _isAdmin = MutableStateFlow(false)
    val isAdmin: StateFlow<Boolean> = _isAdmin

    private val _isShopkeeper = MutableStateFlow(false)
    val isShopkeeper: StateFlow<Boolean> = _isShopkeeper

    private val client get() = SupabaseClient.client

    suspend fun initialize() {
        val session = client.auth.currentSessionOrNull()
        _currentUser.value = session?.user
        session?.user?.id?.let { checkRoles(it) }
    }

    suspend fun signUp(email: String, password: String) {
        client.auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    suspend fun signIn(email: String, password: String) {
        client.auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
        val user = client.auth.currentUserOrNull()
        _currentUser.value = user
        user?.id?.let { checkRoles(it) }
    }

    suspend fun signOut() {
        client.auth.signOut()
        _currentUser.value = null
        _isAdmin.value = false
        _isShopkeeper.value = false
    }

    private suspend fun checkRoles(userId: String) {
        try {
            val roles = client.postgrest["user_roles"]
                .select { filter { eq("user_id", userId) } }
                .decodeList<UserRole>()
            _isAdmin.value = roles.any { it.role == "admin" }
            _isShopkeeper.value = roles.any { it.role == "shopkeeper" }
        } catch (_: Exception) {
            _isAdmin.value = false
            _isShopkeeper.value = false
        }
    }

    fun getUserId(): String? = _currentUser.value?.id
}
