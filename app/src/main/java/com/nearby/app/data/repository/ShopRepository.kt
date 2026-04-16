package com.nearby.app.data.repository

import com.nearby.app.data.SupabaseClient
import com.nearby.app.data.model.Shop
import io.github.jan.supabase.postgrest.postgrest

object ShopRepository {

    private val client get() = SupabaseClient.client

    /** Fetch a single shop by ID */
    suspend fun getShopById(id: String): Shop? {
        return client.postgrest["shops"]
            .select { filter { eq("id", id) } }
            .decodeSingleOrNull<Shop>()
    }

    /** Fetch shops owned by a specific user */
    suspend fun getShopsByOwner(ownerId: String): List<Shop> {
        return client.postgrest["shops"]
            .select { filter { eq("owner_id", ownerId) } }
            .decodeList<Shop>()
    }

    /** Fetch all pending shops (admin) */
    suspend fun getPendingShops(): List<Shop> {
        return client.postgrest["shops"]
            .select { filter { eq("status", "pending") } }
            .decodeList<Shop>()
    }

    /** Fetch all approved shops */
    suspend fun getApprovedShops(): List<Shop> {
        return client.postgrest["shops"]
            .select { filter { eq("status", "approved") } }
            .decodeList<Shop>()
    }

    /** Fetch ALL shops for admin panel */
    suspend fun getAllShops(): List<Shop> {
        return client.postgrest["shops"]
            .select()
            .decodeList<Shop>()
    }

    /** Update shop status */
    suspend fun updateShopStatus(shopId: String, status: String) {
        client.postgrest["shops"]
            .update({ set("status", status) }) { filter { eq("id", shopId) } }
    }

    /** Create a new shop */
    suspend fun createShop(shop: Shop): Shop {
        return client.postgrest["shops"]
            .insert(shop) { select() }
            .decodeSingle<Shop>()
    }

    /** Update an existing shop */
    suspend fun updateShop(id: String, updates: Map<String, Any?>) {
        client.postgrest["shops"]
            .update({
                updates.forEach { (key, value) ->
                    when (value) {
                        is String -> set(key, value)
                        is Boolean -> set(key, value)
                        is Int -> set(key, value)
                        is Double -> set(key, value)
                        null -> setToNull(key)
                    }
                }
            }) {
                filter { eq("id", id) }
            }
    }
}
