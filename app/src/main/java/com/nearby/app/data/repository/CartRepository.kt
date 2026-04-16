package com.nearby.app.data.repository

import com.nearby.app.data.SupabaseClient
import com.nearby.app.data.model.*
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
object CartRepository {

    private val client get() = SupabaseClient.client

    /** Fetch all cart items for the current user with product and shop details */
    suspend fun getCartItems(userId: String): List<CartItem> {
        val rows = client.postgrest["cart_items"]
            .select(columns = Columns.raw("id, product_id, shop_id, quantity, products(name, price, image_url, stock), shops(id, name)")) {
                filter { eq("user_id", userId) }
            }
            .decodeList<CartItemWithDetails>()

        return rows.map { row ->
            CartItem(
                id = row.id,
                productId = row.productId,
                shopId = row.shopId,
                quantity = row.quantity,
                productName = row.products?.name ?: "Unknown",
                productPrice = row.products?.price ?: 0.0,
                productImageUrl = row.products?.imageUrl,
                productStock = row.products?.stock ?: 0,
                shopName = row.shops?.name ?: "Shop"
            )
        }
    }

    /** Add item to cart (or increment if exists) */
    suspend fun addToCart(userId: String, productId: String, shopId: String) {
        // Check max stock
        val stockResult = client.postgrest["products"]
            .select { filter { eq("id", productId) } }
            .decodeSingleOrNull<Product>()

        val maxStock = stockResult?.stock ?: 0

        // Check for existing cart item
        val existing = client.postgrest["cart_items"]
            .select {
                filter {
                    eq("user_id", userId)
                    eq("product_id", productId)
                }
            }
            .decodeList<CartItemRow>()
            .firstOrNull()

        if (existing != null) {
            if (existing.quantity >= maxStock) {
                throw Exception("Maximum stock level reached!")
            }
            client.postgrest["cart_items"]
                .update({ set("quantity", existing.quantity + 1) }) {
                    filter { eq("id", existing.id) }
                }
        } else {
            if (maxStock <= 0) {
                throw Exception("Out of stock!")
            }
            client.postgrest["cart_items"]
                .insert(CartItemRow(
                    userId = userId,
                    productId = productId,
                    shopId = shopId,
                    quantity = 1
                ))
        }
    }

    /** Remove a cart item */
    suspend fun removeFromCart(itemId: String) {
        client.postgrest["cart_items"]
            .delete { filter { eq("id", itemId) } }
    }

    /** Update cart item quantity */
    suspend fun updateQuantity(itemId: String, quantity: Int) {
        if (quantity <= 0) {
            removeFromCart(itemId)
            return
        }
        client.postgrest["cart_items"]
            .update({ set("quantity", quantity) }) {
                filter { eq("id", itemId) }
            }
    }

    /** Clear entire cart for a user */
    suspend fun clearCart(userId: String) {
        client.postgrest["cart_items"]
            .delete { filter { eq("user_id", userId) } }
    }
}
