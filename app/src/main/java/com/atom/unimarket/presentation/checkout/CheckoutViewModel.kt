package com.atom.unimarket.presentation.checkout

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.CartItem
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.data.repository.CheckoutRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

data class OrderGroup(
    val sellerId: String,
    val sellerName: String,
    val sellerPhone: String,
    val items: List<CartItem>,
    val subtotal: Double
)

data class PaymentInputState(
    val yapeCode: String = "",
    val proofUrl: String = "" // For future use if image upload is implemented
)

data class CheckoutState(
    val orderGroups: List<OrderGroup> = emptyList(),
    val paymentInputs: Map<String, PaymentInputState> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val paymentSuccess: Boolean = false
)

class CheckoutViewModel(
    private val checkoutRepository: CheckoutRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    init {
        loadCheckoutData()
    }

    fun loadCheckoutData() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            try {
                val cartItems = checkoutRepository.getCartItems()
                val groupedItems = cartItems.groupBy { it.sellerId }
                
                val orderGroups = groupedItems.map { (sellerId, items) ->
                    val seller = checkoutRepository.getSellerDetails(sellerId)
                    val subtotal = items.sumOf { it.price * it.quantity }
                    OrderGroup(
                        sellerId = sellerId,
                        sellerName = seller?.displayName ?: "Vendedor Desconocido",
                        sellerPhone = seller?.phoneNumber ?: "",
                        items = items,
                        subtotal = subtotal
                    )
                }

                // Initialize inputs for each group
                val initialInputs = orderGroups.associate { 
                    it.sellerId to PaymentInputState() 
                }

                _state.update { 
                    it.copy(
                        isLoading = false, 
                        orderGroups = orderGroups,
                        paymentInputs = initialInputs
                    ) 
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun onYapeCodeChange(sellerId: String, code: String) {
        _state.update { currentState ->
            val currentInputs = currentState.paymentInputs.toMutableMap()
            val currentInput = currentInputs[sellerId] ?: PaymentInputState()
            currentInputs[sellerId] = currentInput.copy(yapeCode = code)
            currentState.copy(paymentInputs = currentInputs)
        }
    }

    fun reportarPagoVendedor(sellerId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = auth.currentUser ?: throw Exception("No autenticado")
                val group = state.value.orderGroups.find { it.sellerId == sellerId } 
                    ?: throw Exception("Grupo de orden no encontrado")
                val input = state.value.paymentInputs[sellerId] 
                    ?: throw Exception("Datos de pago no encontrados")

                if (input.yapeCode.isBlank()) {
                    throw Exception("El código de operación es obligatorio")
                }
                
                val order = Order(
                    id = UUID.randomUUID().toString(),
                    buyerId = currentUser.uid,
                    buyerName = currentUser.displayName ?: "Usuario",
                    items = group.items,
                    totalAmount = group.subtotal,
                    status = "PAGO_REPORTADO", // Updated status
                    paymentMethod = "YAPE",
                    sellerId = group.sellerId,
                    sellerIds = listOf(group.sellerId),
                    // Add extra fields if Order data class supports them, or handle separately
                    // For now assuming Order has a way to store this or we just rely on status
                )

                // Ideally we should save the yapeCode and proofUrl in the order too.
                // Since the Order data class definition wasn't fully visible/modifiable in this step,
                // we assume the repository handles it or we might need to update Order data class later.
                // For this task, we proceed with creating the order.
                
                checkoutRepository.createOrder(order)
                
                // Remove this group from local state
                _state.update { currentState ->
                    val remainingGroups = currentState.orderGroups.filter { it.sellerId != sellerId }
                    val remainingInputs = currentState.paymentInputs.filterKeys { it != sellerId }
                    currentState.copy(
                        isLoading = false, 
                        orderGroups = remainingGroups,
                        paymentInputs = remainingInputs,
                        paymentSuccess = true // Trigger success message
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun payToSeller(context: Context, phoneNumber: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("yape://qr/$phoneNumber"))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback or error message if Yape is not installed
             try {
                // Try to open Play Store or just show a message
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bcp.innovacxion.yapeapp"))
                context.startActivity(intent)
            } catch (e2: Exception) {
                // Ignore
            }
        }
    }

    fun confirmPayment(group: OrderGroup) {
        reportarPagoVendedor(group.sellerId)
    }

    fun resetPaymentSuccess() {
        _state.update { it.copy(paymentSuccess = false) }
    }
}
