package com.atom.unimarket.data.repository

import com.atom.unimarket.domain.model.OrderGroup
import com.atom.unimarket.domain.repository.CartRepository
import com.atom.unimarket.domain.repository.CheckoutRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

class CheckoutRepositoryImpl(
    private val cartRepository: CartRepository,
    private val firestore: FirebaseFirestore
) : CheckoutRepository {

    override suspend fun loadGroupedOrders(): List<OrderGroup> {
        // 1. Obtener items del carrito (snapshot actual)
        val cartItems = cartRepository.getCartItems().first()

        if (cartItems.isEmpty()) return emptyList()

        // 2. Agrupar por idVendedor
        val groupedItems = cartItems.groupBy { it.sellerId }

        val orderGroups = mutableListOf<OrderGroup>()

        // 3. Iterar grupos y obtener info del vendedor
        for ((sellerId, items) in groupedItems) {
            // Calcular subtotal del grupo
            val subtotal = items.sumOf { it.price * it.quantity }

            // Obtener datos del vendedor desde Firestore
            // Asumimos que la colección de usuarios es "users"
            val sellerDoc = firestore.collection("users").document(sellerId).get().await()

            val sellerName = sellerDoc.getString("firstName") ?: "Vendedor Desconocido"
            val sellerPhone = sellerDoc.getString("phoneNumber") ?: ""
            val sellerFcmToken = sellerDoc.getString("fcmToken") ?: ""

            orderGroups.add(
                OrderGroup(
                    idVendedor = sellerId,
                    sellerName = sellerName,
                    sellerPhone = sellerPhone,
                    sellerFcmToken = sellerFcmToken,
                    items = items,
                    subtotal = subtotal
                )
            )
        }

        return orderGroups
    }
}
