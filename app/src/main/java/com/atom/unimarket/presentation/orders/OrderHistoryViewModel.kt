package com.atom.unimarket.presentation.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.data.repository.OrderRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class OrderHistoryState(
    val orders: List<Order> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class OrderHistoryViewModel(
    private val orderRepository: OrderRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore // <-- Agregamos Firestore para la actualización directa
) : ViewModel() {

    private val _state = MutableStateFlow(OrderHistoryState())
    val state = _state.asStateFlow()

    init {
        loadOrders()
    }

    fun loadOrders() {
        val userId = auth.currentUser?.uid ?: return
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            orderRepository.getOrdersByBuyer(userId)
                .catch { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
                .collect { orders ->
                    _state.update { it.copy(isLoading = false, orders = orders) }
                }
        }
    }

    // --- NUEVA FUNCIÓN: Verificar Pedido ---
    fun verifyOrder(orderId: String) {
        viewModelScope.launch {
            try {
                // Actualizamos directamente en Firestore el estado a "DELIVERED"
                firestore.collection("orders").document(orderId)
                    .update("status", "DELIVERED")
                    .await()

                // Recargamos la lista para ver el cambio reflejado (o confiamos en el flow en tiempo real si el repo lo soporta)
                loadOrders()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al verificar: ${e.message}") }
            }
        }
    }
}