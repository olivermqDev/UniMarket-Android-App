package com.atom.unimarket.presentation.seller.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.domain.model.Order
import com.atom.unimarket.domain.model.OrderStatus
import com.atom.unimarket.domain.repository.OrderRepository
import com.atom.unimarket.domain.service.NotificationService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SellerOrdersViewModel(
    private val orderRepository: OrderRepository,
    private val auth: FirebaseAuth,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        fetchOrders()
    }

    private fun fetchOrders() {
        val sellerId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _isLoading.value = true
            orderRepository.getOrdersBySeller(sellerId).collect {
                _orders.value = it
                _isLoading.value = false
            }
        }
    }

    fun approveOrder(orderId: String) {
        viewModelScope.launch {
            val order = _orders.value.find { it.idPedido == orderId }
            orderRepository.updateOrderStatus(orderId, OrderStatus.PagoConfirmado.value)
            
            // Send notification to buyer
            order?.let {
                notificationService.sendOrderNotification(
                    toUserId = it.idComprador,
                    title = "Pago confirmado",
                    message = "El vendedor está preparando tu pedido."
                )
            }
        }
    }

    fun rejectOrder(orderId: String) {
        viewModelScope.launch {
            val order = _orders.value.find { it.idPedido == orderId }
            orderRepository.updateOrderStatus(orderId, OrderStatus.PagoRechazado.value)
            
            // Send notification to buyer
            order?.let {
                notificationService.sendOrderNotification(
                    toUserId = it.idComprador,
                    title = "Pago rechazado",
                    message = "El vendedor rechazó la verificación del pago del pedido ${it.idPedido.take(8)}. Si ya pagaste, vuelve a enviar el comprobante."
                )
            }
        }
    }

    fun updateStatus(orderId: String, status: OrderStatus) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status.value)
            
            // Optional: Send notification for other status changes
             val order = _orders.value.find { it.idPedido == orderId }
             order?.let {
                 val message = when(status) {
                     OrderStatus.EnPreparacion -> "Tu pedido está siendo preparado."
                     OrderStatus.EnCamino -> "Tu pedido está en camino."
                     OrderStatus.Entregado -> "Tu pedido ha sido entregado."
                     else -> "El estado de tu pedido ha cambiado."
                 }
                 notificationService.sendOrderNotification(
                     toUserId = it.idComprador,
                     title = "Actualización de pedido",
                     message = message
                 )
             }
        }
    }
}
