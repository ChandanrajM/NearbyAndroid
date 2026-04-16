package com.nearby.app.data.model

import kotlinx.serialization.Serializable

@Serializable
data class UserRole(
    val id: String = "",
    val user_id: String = "",
    val role: String = ""
)
