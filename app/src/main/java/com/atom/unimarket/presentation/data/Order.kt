package com.atom.unimarket.presentation.data

import com.atom.unimarket.presentation.data.Product
import com.google.firebase.firestore.ServerTimestamp
import java.util.Date
import com.atom.unimarket.presentation.data.Address

/**
 * Representa un item dentro de la orden.
 */
data class CartItem(
    val id: String = "",
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val quantity: Int = 1,
    val sellerId: String = ""
)

data class Order(
    val id: String = "",
    val buyerId: String = "",
    val buyerName: String = "",
    val shippingAddress: Address? = null,
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "PENDING_VERIFICATION",
    val paymentMethod: String = "UNKNOWN",
    val sellerId: String = "", // <-- NUEVO: ID del vendedor único para esta orden
    val sellerIds: List<String> = emptyList(), // <-- MANTENER: Por compatibilidad
    val yapeCode: String = "",
    val paymentProofUrl: String = "",
    val deliveryType: String = "", // "PICKUP" or "DELIVERY"
    val deliveryAddress: String = "",
    val pickupPoint: String = "",
    @ServerTimestamp
    val createdAt: Date? = null
)

fun Product.toOrderItem(quantity: Int = 1): CartItem {
    return CartItem(
        id = this.id,
        productId = this.id,
        name = this.name,
        price = this.price,
        imageUrl = this.imageUrls.firstOrNull() ?: "",
        quantity = quantity,
        sellerId = this.sellerUid
    )
}