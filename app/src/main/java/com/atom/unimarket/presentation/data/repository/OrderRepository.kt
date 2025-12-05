package com.atom.unimarket.presentation.data.repository

import com.atom.unimarket.presentation.data.Order
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

interface OrderRepository {
    fun getOrdersByBuyer(buyerId: String): Flow<List<Order>>
}

class OrderRepositoryImpl(
    private val firestore: FirebaseFirestore
) : OrderRepository {

    override fun getOrdersByBuyer(buyerId: String): Flow<List<Order>> = callbackFlow {
        val query = firestore.collection("orders")
            .whereEqualTo("buyerId", buyerId)
            .orderBy("createdAt", Query.Direction.DESCENDING)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }

            if (snapshot != null) {
                val orders = snapshot.toObjects(Order::class.java)
                trySend(orders)
            }
        }

        awaitClose { listener.remove() }
    }
}
