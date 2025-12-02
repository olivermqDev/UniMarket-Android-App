package com.atom.unimarket.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.domain.model.Order
import com.atom.unimarket.domain.repository.OrderRepository
import com.atom.unimarket.domain.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class OrderWithSeller(
    val order: Order,
    val sellerName: String
)

sealed class HistoryUiState {
    object Loading : HistoryUiState()
    data class Success(val orders: List<OrderWithSeller>) : HistoryUiState()
    data class Error(val message: String) : HistoryUiState()
}

class HistoryViewModel(
    private val orderRepository: OrderRepository,
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            _uiState.value = HistoryUiState.Error("Usuario no autenticado")
            return
        }

        viewModelScope.launch {
            try {
                orderRepository.getOrdersByBuyer(currentUser.uid)
                    .collectLatest { orders ->
                        val ordersWithSellers = orders.map { order ->
                            val sellerName = try {
                                val userResult = userRepository.getUser(order.idVendedor)
                                userResult.getOrNull()?.displayName ?: "Vendedor Desconocido"
                            } catch (e: Exception) {
                                "Vendedor Desconocido"
                            }
                            OrderWithSeller(order, sellerName)
                        }
                        _uiState.value = HistoryUiState.Success(ordersWithSellers)
                    }
            } catch (e: Exception) {
                _uiState.value = HistoryUiState.Error(e.message ?: "Error al cargar historial")
            }
        }
    }
}
