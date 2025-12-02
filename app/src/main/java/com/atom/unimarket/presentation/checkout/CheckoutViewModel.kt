package com.atom.unimarket.presentation.checkout

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.domain.model.CartItem
import com.atom.unimarket.domain.model.Order
import com.atom.unimarket.domain.model.OrderStatus
import com.atom.unimarket.domain.repository.CartRepository
import com.atom.unimarket.domain.repository.CheckoutRepository
import com.atom.unimarket.domain.repository.OrderRepository
import com.atom.unimarket.domain.service.NotificationService
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

data class SellerCheckoutState(
    val sellerId: String,
    val sellerName: String,
    val sellerPhone: String,
    val sellerFcmToken: String,
    val items: List<CartItem>,
    val total: Double,
    var yapeCode: String = "",
    var proofUri: Uri? = null,
    var isUploading: Boolean = false,
    var isOrderCreated: Boolean = false,
    var deliveryType: String = "personal", // Default to personal
    var pickupPoint: String = "",
    var deliveryAddress: String = ""
)

sealed class CheckoutUiState {
    object Loading : CheckoutUiState()
    data class Content(val sellerGroups: List<SellerCheckoutState>) : CheckoutUiState()
    data class Error(val message: String) : CheckoutUiState()
    object Success : CheckoutUiState()
}

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val cartRepository: CartRepository,
    private val orderRepository: OrderRepository,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage,
    private val notificationService: NotificationService
) : ViewModel() {

    private val _uiState = MutableStateFlow<CheckoutUiState>(CheckoutUiState.Loading)
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    private var currentSellerGroups = mutableListOf<SellerCheckoutState>()

    init {
        loadCheckoutData()
    }

    private fun loadCheckoutData() {
        viewModelScope.launch {
            _uiState.value = CheckoutUiState.Loading
            try {
                val groupedOrders = checkoutRepository.loadGroupedOrders()

                if (groupedOrders.isEmpty()) {
                    _uiState.value = CheckoutUiState.Content(emptyList())
                    return@launch
                }

                val sellerStates = groupedOrders.map { group ->
                    SellerCheckoutState(
                        sellerId = group.idVendedor,
                        sellerName = group.sellerName,
                        sellerPhone = group.sellerPhone,
                        sellerFcmToken = group.sellerFcmToken,
                        items = group.items,
                        total = group.subtotal,
                        // Initialize with values from OrderGroup if present, or defaults
                        deliveryType = group.deliveryType ?: "personal",
                        pickupPoint = group.pickupPoint ?: "",
                        deliveryAddress = group.deliveryAddress ?: ""
                    )
                }

                currentSellerGroups = sellerStates.toMutableList()
                _uiState.value = CheckoutUiState.Content(currentSellerGroups.toList())

            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error(e.message ?: "Error loading checkout data")
            }
        }
    }

    fun updatePaymentInfo(sellerId: String, code: String, uri: Uri?) {
        updateSellerState(sellerId) { it.copy(yapeCode = code, proofUri = uri ?: it.proofUri) }
    }

    fun updateDeliveryMethod(sellerId: String, method: String) {
        updateSellerState(sellerId) { it.copy(deliveryType = method) }
    }

    fun updatePickupLocation(sellerId: String, location: String) {
        updateSellerState(sellerId) { it.copy(pickupPoint = location) }
    }

    fun updateDeliveryAddress(sellerId: String, address: String) {
        updateSellerState(sellerId) { it.copy(deliveryAddress = address) }
    }

    private fun updateSellerState(sellerId: String, update: (SellerCheckoutState) -> SellerCheckoutState) {
        val index = currentSellerGroups.indexOfFirst { it.sellerId == sellerId }
        if (index != -1) {
            currentSellerGroups[index] = update(currentSellerGroups[index])
            _uiState.value = CheckoutUiState.Content(currentSellerGroups.toList())
        }
    }

    fun confirmAllOrders() {
        viewModelScope.launch {
            val groups = currentSellerGroups.filter { !it.isOrderCreated }
            if (groups.isEmpty()) return@launch

            // Validate all groups
            val invalidGroup = groups.find { it.yapeCode.isBlank() }
            if (invalidGroup != null) {
                _uiState.value = CheckoutUiState.Error("Falta código de operación para ${invalidGroup.sellerName}")
                return@launch
            }
            
            // Validate delivery info
            val invalidDelivery = groups.find { 
                (it.deliveryType == "personal" && it.pickupPoint.isBlank()) ||
                (it.deliveryType == "delivery" && it.deliveryAddress.isBlank())
            }
            if (invalidDelivery != null) {
                 _uiState.value = CheckoutUiState.Error("Falta información de entrega para ${invalidDelivery.sellerName}")
                return@launch
            }

            _uiState.value = CheckoutUiState.Loading

            try {
                val userId = auth.currentUser?.uid ?: throw Exception("User not logged in")

                groups.map { state ->
                    async {
                        try {
                            val orderId = UUID.randomUUID().toString()
                            var proofUrl = ""

                            if (state.proofUri != null) {
                                val ref = storage.reference.child("payments/$orderId.jpg")
                                ref.putFile(state.proofUri!!).await()
                                proofUrl = ref.downloadUrl.await().toString()
                            }

                            val order = Order(
                                idPedido = orderId,
                                idComprador = userId,
                                idVendedor = state.sellerId,
                                productos = state.items,
                                total = state.total,
                                codigoYape = state.yapeCode,
                                urlComprobante = proofUrl,
                                tipoEntrega = state.deliveryType,
                                puntoEntrega = if (state.deliveryType == "personal") state.pickupPoint else "",
                                direccionEntrega = if (state.deliveryType == "delivery") state.deliveryAddress else "",
                                estado = OrderStatus.PagoReportado.value
                            )
                            
                            orderRepository.createOrder(order)
                            
                            if (state.sellerFcmToken.isNotEmpty()) {
                                notificationService.sendOrderNotification(
                                    toUserId = state.sellerId,
                                    title = "Nuevo pedido — Pago reportado",
                                    message = "Código: ${state.yapeCode} | Total: S/${state.total}"
                                )
                            }
                            
                            state.isOrderCreated = true
                        } catch (e: Exception) {
                            throw e
                        }
                    }
                }.awaitAll()

                cartRepository.clearCart()
                _uiState.value = CheckoutUiState.Success

            } catch (e: Exception) {
                _uiState.value = CheckoutUiState.Error("Error al crear pedidos: ${e.message}")
                _uiState.value = CheckoutUiState.Content(currentSellerGroups.toList())
            }
        }
    }
}
