package com.atom.unimarket.presentation.sales

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado de la pantalla de ventas
data class SalesState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SalesViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(SalesState())
    val state = _state.asStateFlow()

    init {
        listenForSales()
    }

    private fun listenForSales() {
        val userId = auth.currentUser?.uid ?: return

        _state.update { it.copy(isLoading = true) }

        // Escuchamos cambios en tiempo real en la colección "orders"
        // donde el array "sellerIds" contenga mi ID.
        firestore.collection("orders")
            .whereArrayContains("sellerIds", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _state.update { it.copy(isLoading = false, error = "Error al cargar ventas: ${error.message}") }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val orders = snapshot.toObjects(Order::class.java)

                    // IMPORTANTE: Un pedido puede tener productos de varios vendedores.
                    // Aquí filtramos los items dentro de la orden para que el vendedor
                    // SOLO VEA LOS ITEMS QUE ÉL VENDIÓ.
                    val filteredOrders = orders.map { order ->
                        order.copy(
                            items = order.items.filter { it.sellerId == userId }
                        )
                    }

                    _state.update {
                        it.copy(
                            isLoading = false,
                            orders = filteredOrders,
                            error = null
                        )
                    }
                }
            }
    }
}
