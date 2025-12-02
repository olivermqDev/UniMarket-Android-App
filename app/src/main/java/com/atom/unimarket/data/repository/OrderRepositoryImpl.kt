package com.atom.unimarket.data.repository

import com.atom.unimarket.domain.model.Order
import com.atom.unimarket.domain.repository.OrderRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class OrderRepositoryImpl(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    override suspend fun createOrder(order: Order): Result<Unit> {
        return try {
            firestore.collection("pedidos").document(order.idPedido).set(order).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getOrdersBySeller(sellerId: String): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("pedidos")
            .whereEqualTo("idVendedor", sellerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit> {
        return try {
            firestore.collection("pedidos").document(orderId)
                .update("estado", status).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getOrdersByBuyer(buyerId: String): Flow<List<Order>> = callbackFlow {
        val listener = firestore.collection("pedidos")
            .whereEqualTo("idComprador", buyerId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val orders = snapshot?.toObjects(Order::class.java) ?: emptyList()
                trySend(orders)
            }
        awaitClose { listener.remove() }
    }
}
