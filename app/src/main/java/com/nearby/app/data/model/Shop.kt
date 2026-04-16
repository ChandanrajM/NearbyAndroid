package com.nearby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Shop(
    val id: String = "",
    @SerialName("owner_id") val ownerId: String = "",
    val name: String = "",
    val category: String = "",
    val phone: String = "",
    @SerialName("gst_number") val gstNumber: String? = null,
    @SerialName("owner_name") val ownerName: String = "",
    val description: String? = null,
    val address: String = "",
    @SerialName("location_lat") val locationLat: Double? = null,
    @SerialName("location_lng") val locationLng: Double? = null,
    @SerialName("image_url") val imageUrl: String? = null,
    val status: String = "pending",
    @SerialName("is_online") val isOnline: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)
