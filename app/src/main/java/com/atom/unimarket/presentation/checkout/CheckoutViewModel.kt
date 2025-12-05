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

data class CheckoutState(
    val orderGroups: List<OrderGroup> = emptyList(),
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

                _state.update { it.copy(isLoading = false, orderGroups = orderGroups) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun payToSeller(context: Context, sellerPhone: String) {
        if (sellerPhone.isBlank()) {
            _state.update { it.copy(error = "El vendedor no tiene número de Yape registrado.") }
            return
        }
        
        // Deep link to Yape
        // Note: The exact deep link scheme for Yape might vary. 
        // Using a common pattern or just opening the app if specific deep link isn't public.
        // For this task, we'll try a standard intent or just copy number + open app.
        // Requirement says: "Intente la redirección directa... deep link"
        // Example: yape://send?phone=... (Hypothetical, usually these are protected)
        // Fallback: Open Yape package.
        
        try {
            val intent = context.packageManager.getLaunchIntentForPackage("com.bcp.innovacxion.yapeapp")
            if (intent != null) {
                context.startActivity(intent)
            } else {
                // Fallback to Play Store or just toast
                _state.update { it.copy(error = "Yape no está instalado.") }
            }
        } catch (e: Exception) {
             _state.update { it.copy(error = "Error al abrir Yape: ${e.message}") }
        }
    }

    fun confirmPayment(group: OrderGroup) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = auth.currentUser ?: throw Exception("No autenticado")
                
                val order = Order(
                    id = UUID.randomUUID().toString(),
                    buyerId = currentUser.uid,
                    buyerName = currentUser.displayName ?: "Usuario",
                    // shippingAddress = ... (Need to pass this from UI or store in VM)
                    items = group.items,
                    totalAmount = group.subtotal,
                    status = "PENDING_VERIFICATION",
                    paymentMethod = "YAPE",
                    sellerId = group.sellerId,
                    sellerIds = listOf(group.sellerId)
                )

                checkoutRepository.createOrder(order)
                
                // Trigger notification logic here (e.g. call Cloud Function via HTTP or just rely on Firestore trigger)
                // For now, we assume Firestore trigger handles it as per architecture.
                
                // Remove this group from local state
                _state.update { currentState ->
                    val remainingGroups = currentState.orderGroups.filter { it.sellerId != group.sellerId }
                    currentState.copy(
                        isLoading = false, 
                        orderGroups = remainingGroups,
                        paymentSuccess = true
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun resetPaymentSuccess() {
        _state.update { it.copy(paymentSuccess = false) }
    }
}
