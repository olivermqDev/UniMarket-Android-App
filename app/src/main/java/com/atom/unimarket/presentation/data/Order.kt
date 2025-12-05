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
    val buyerName: String = "",           // <-- NUEVO: Nombre del comprador
    val shippingAddress: Address? = null, // <-- NUEVO: Dirección de envío snapshot
    val items: List<CartItem> = emptyList(),
    val totalAmount: Double = 0.0,
    val status: String = "COMPLETED",
    val paymentMethod: String = "UNKNOWN",
    val sellerIds: List<String> = emptyList(), // <-- NUEVO: IDs de los vendedores involucrados (para búsquedas)
    @ServerTimestamp
    val createdAt: Date? = null
)

fun Product.toOrderItem(): CartItem {
    return CartItem(
        id = this.id,
        productId = this.id,
        name = this.name,
        price = this.price,
        imageUrl = this.imageUrls.firstOrNull() ?: "",
        quantity = 1,
        sellerId = this.sellerUid
    )
}