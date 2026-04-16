package com.nearby.app.data.repository

import com.nearby.app.data.SupabaseClient
import com.nearby.app.data.model.Product
import com.nearby.app.data.model.ProductWithShop
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
object ProductRepository {

    private val client get() = SupabaseClient.client

    /** Get all products for a specific shop */
    suspend fun getProductsByShop(shopId: String): List<Product> {
        return client.postgrest["products"]
            .select { filter { eq("shop_id", shopId) } }
            .decodeList<Product>()
    }

    /** Get trending products globally (with shop name) */
    suspend fun getTrending(limit: Int = 6): List<ProductWithShop> {
        return client.postgrest["products"]
            .select(columns = Columns.raw("*, shops!inner(name, status)")) {
                filter {
                    eq("is_trending", true)
                    eq("shops.status", "approved")
                }
                limit(limit.toLong())
            }
            .decodeList<ProductWithShop>()
    }

    /** Get deal products globally */
    suspend fun getDeals(limit: Int = 6): List<ProductWithShop> {
        return client.postgrest["products"]
            .select(columns = Columns.raw("*, shops!inner(name, status)")) {
                filter {
                    eq("is_deal", true)
                    eq("shops.status", "approved")
                }
                limit(limit.toLong())
            }
            .decodeList<ProductWithShop>()
    }

    /** Create a product */
    suspend fun createProduct(product: Product): Product {
        return client.postgrest["products"]
            .insert(product) { select() }
            .decodeSingle<Product>()
    }

    /** Update a product */
    suspend fun updateProduct(id: String, updates: Map<String, Any?>) {
        client.postgrest["products"]
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

    /** Delete a product */
    suspend fun deleteProduct(id: String) {
        client.postgrest["products"]
            .delete { filter { eq("id", id) } }
    }
}
