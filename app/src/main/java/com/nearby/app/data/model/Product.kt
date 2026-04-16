package com.nearby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Product(
    val id: String = "",
    @SerialName("shop_id") val shopId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    val description: String? = null,
    val stock: Int = 0,
    @SerialName("is_deal") val isDeal: Boolean = false,
    @SerialName("is_trending") val isTrending: Boolean = false,
    val category: String? = null,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Used when fetching trending products that include joined shop data.
 */
@Serializable
data class ProductWithShop(
    val id: String = "",
    @SerialName("shop_id") val shopId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    val description: String? = null,
    val stock: Int = 0,
    @SerialName("is_deal") val isDeal: Boolean = false,
    @SerialName("is_trending") val isTrending: Boolean = false,
    val category: String? = null,
    val shops: ShopRef? = null
)

@Serializable
data class ShopRef(
    val name: String = "",
    val status: String = ""
)
