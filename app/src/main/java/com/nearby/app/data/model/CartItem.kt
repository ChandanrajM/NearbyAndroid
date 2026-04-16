package com.nearby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CartItemRow(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("shop_id") val shopId: String = "",
    val quantity: Int = 1,
    @SerialName("created_at") val createdAt: String = "",
    @SerialName("updated_at") val updatedAt: String = ""
)

/**
 * Cart item with joined product and shop data for the UI.
 */
@Serializable
data class CartItemWithDetails(
    val id: String = "",
    @SerialName("product_id") val productId: String = "",
    @SerialName("shop_id") val shopId: String = "",
    val quantity: Int = 1,
    val products: CartProduct? = null,
    val shops: CartShop? = null
)

@Serializable
data class CartProduct(
    val name: String = "",
    val price: Double = 0.0,
    @SerialName("image_url") val imageUrl: String? = null,
    val stock: Int = 0
)

@Serializable
data class CartShop(
    val id: String = "",
    val name: String = ""
)

/**
 * UI-ready model combining parsed data.
 */
data class CartItem(
    val id: String,
    val productId: String,
    val shopId: String,
    val quantity: Int,
    val productName: String,
    val productPrice: Double,
    val productImageUrl: String?,
    val productStock: Int,
    val shopName: String
)
