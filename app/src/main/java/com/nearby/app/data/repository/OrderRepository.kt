package com.nearby.app.data.repository

import com.nearby.app.data.SupabaseClient
import com.nearby.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
object OrderRepository {

    private val client get() = SupabaseClient.client

    /** Get all orders for the current user */
    suspend fun getUserOrders(userId: String): List<Order> {
        return client.postgrest["orders"]
            .select(columns = Columns.raw("*, shops(name), order_items(id, product_name, quantity, price)")) {
                filter { eq("user_id", userId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Order>()
    }

    /** Get all orders for a specific shop (shopkeeper dashboard) */
    suspend fun getShopOrders(shopId: String): List<Order> {
        return client.postgrest["orders"]
            .select(columns = Columns.raw("*, order_items(id, product_name, quantity, price)")) {
                filter { eq("shop_id", shopId) }
                order("created_at", io.github.jan.supabase.postgrest.query.Order.DESCENDING)
            }
            .decodeList<Order>()
    }

    /** Update order status */
    suspend fun updateOrderStatus(orderId: String, status: String) {
        client.postgrest["orders"]
            .update({ set("status", status) }) {
                filter { eq("id", orderId) }
            }
    }

    /** Create order + order items in a transaction-like manner */
    suspend fun createOrderTransaction(
        userId: String,
        shopId: String,
        total: Double,
        items: List<OrderItemInsert>
    ) {
        // 1. Create the order
        val order = client.postgrest["orders"]
            .insert(OrderInsert(userId = userId, shopId = shopId, total = total)) {
                select()
            }
            .decodeSingle<OrderIdResponse>()

        // 2. Insert order items with the order ID
        val itemsWithOrderId = items.map { it.copy(orderId = order.id) }
        client.postgrest["order_items"]
            .insert(itemsWithOrderId)
    }
}
