package com.nearby.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val id: String = "",
    @SerialName("user_id") val userId: String = "",
    @SerialName("shop_id") val shopId: String = "",
    val total: Double = 0.0,
    val status: String = "pending",
    @SerialName("created_at") val createdAt: String = "",
    val shops: OrderShopRef? = null,
    @SerialName("order_items") val orderItems: List<OrderItem>? = null
)

@Serializable
data class OrderShopRef(
    val name: String = ""
)

@Serializable
data class OrderItem(
    val id: String = "",
    @SerialName("order_id") val orderId: String = "",
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_name") val productName: String = "",
    val quantity: Int = 1,
    val price: Double = 0.0
)

@Serializable
data class OrderInsert(
    @SerialName("user_id") val userId: String,
    @SerialName("shop_id") val shopId: String,
    val total: Double,
    val status: String = "pending"
)

@Serializable
data class OrderItemInsert(
    @SerialName("order_id") val orderId: String,
    @SerialName("product_id") val productId: String? = null,
    @SerialName("product_name") val productName: String,
    val quantity: Int,
    val price: Double
)

@Serializable
data class OrderIdResponse(
    val id: String
)
