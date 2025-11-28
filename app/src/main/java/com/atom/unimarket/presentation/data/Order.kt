package com.atom.unimarket.presentation.data

import com.atom.unimarket.presentation.data.Product
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa un item dentro de la orden.
 */
data class OrderItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 1,
    val imageUrl: String = "",
    val sellerId: String = ""
)

/**
 * Representa la orden final de compra.
 */
data class Order(
    val id: String = "",
    val buyerId: String = "",
    val items: List<OrderItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "COMPLETED",
    @ServerTimestamp
    val createdAt: Date? = null
)

fun Product.toOrderItem(): OrderItem {
    return OrderItem(
        productId = this.id,
        name = this.name,
        price = this.price,
        quantity = 1, // Lógica simplificada
        imageUrl = this.imageUrls.firstOrNull() ?: "",
        sellerId = this.sellerUid
    )
}