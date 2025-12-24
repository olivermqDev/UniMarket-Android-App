package com.atom.unimarket.presentation.checkout

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.CartItem
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.data.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


// --- MODELOS DE ESTADO (UI) ---
data class OrderGroup(
    val sellerId: String,
    val sellerName: String,
    val sellerPhone: String,
    val items: List<CartItem>,
    val subtotal: Double
)

data class PaymentInputState(
    val yapeCode: String = ""
)

data class CheckoutState(
    val orderGroups: List<OrderGroup> = emptyList(),
    val paymentInputs: Map<String, PaymentInputState> = emptyMap(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val paymentSuccess: Boolean = false
)

class CheckoutViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val productViewModel: com.atom.unimarket.presentation.products.ProductViewModel
) : ViewModel() {

    private val _state = MutableStateFlow(CheckoutState())
    val state = _state.asStateFlow()

    init {
        loadCheckoutData()
    }

    fun loadCheckoutData() {
        val userId = auth.currentUser?.uid ?: return
        _state.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                // 1. Cargar items del carrito
                val cartSnapshot = firestore.collection("users").document(userId)
                    .collection("cart").get().await()

                if (cartSnapshot.isEmpty) {
                    _state.update { it.copy(isLoading = false, orderGroups = emptyList()) }
                    return@launch
                }

                // 2. Cargar detalles de productos
                val productIds = cartSnapshot.documents.map { it.id }
                val products = if (productIds.isNotEmpty()) {
                    firestore.collection("products")
                        .whereIn("id", productIds)
                        .get().await()
                        .toObjects(Product::class.java)
                } else emptyList()

                val quantities = cartSnapshot.documents.associate {
                    it.id to (it.getLong("quantity")?.toInt() ?: 1)
                }

                // 3. Agrupar por Vendedor
                val itemsBySeller = products.groupBy { it.sellerUid }
                val groups = mutableListOf<OrderGroup>()

                for ((sellerId, sellerProducts) in itemsBySeller) {
                    var sellerName = "Vendedor"
                    var sellerPhone = ""

                    try {
                        val sellerDoc = firestore.collection("users").document(sellerId).get().await()
                        sellerName = sellerDoc.getString("displayName") ?: sellerProducts.first().sellerName
                        sellerPhone = sellerDoc.getString("phoneNumber") ?: ""
                    } catch (e: Exception) {  }


                    val groupItems = sellerProducts.map { product ->
                        val qty = quantities[product.id] ?: 1
                        CartItem(
                            id = product.id,
                            productId = product.id,
                            name = product.name,
                            price = product.price,
                            imageUrl = product.imageUrls.firstOrNull() ?: "",
                            quantity = qty,
                            sellerId = sellerId
                        )
                    }

                    val subtotal = groupItems.sumOf { it.price * it.quantity }

                    groups.add(
                        OrderGroup(
                            sellerId = sellerId,
                            sellerName = sellerName,
                            sellerPhone = sellerPhone,
                            items = groupItems,
                            subtotal = subtotal
                        )
                    )
                }

                val inputs = groups.associate { it.sellerId to PaymentInputState() }

                _state.update {
                    it.copy(isLoading = false, orderGroups = groups, paymentInputs = inputs)
                }

            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error al cargar: ${e.message}") }
            }
        }
    }

    fun onYapeCodeChange(sellerId: String, code: String) {
        _state.update { currentState ->
            val newInputs = currentState.paymentInputs.toMutableMap()
            newInputs[sellerId] = newInputs[sellerId]?.copy(yapeCode = code) ?: PaymentInputState(yapeCode = code)
            currentState.copy(paymentInputs = newInputs)
        }
    }

    fun reportarPagoVendedor(sellerId: String) {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val currentUser = auth.currentUser ?: throw Exception("No autenticado")
                val group = state.value.orderGroups.find { it.sellerId == sellerId }
                    ?: throw Exception("Grupo no encontrado")
                val input = state.value.paymentInputs[sellerId]
                    ?: throw Exception("Datos no encontrados")

                if (input.yapeCode.isBlank()) {
                    throw Exception("Ingresa el código de operación")
                }


                val address = productViewModel.getShippingAddress()
                    ?: throw Exception("Dirección no seleccionada")

                val newOrderRef = firestore.collection("orders").document()


                val order = Order(
                    id = newOrderRef.id,
                    buyerId = currentUser.uid,
                    buyerName = currentUser.displayName ?: "Usuario",
                    buyerPhone = address.phoneNumber,
                    shippingAddress = address,
                    items = group.items,
                    totalAmount = group.subtotal,
                    status = "PENDING_VERIFICATION",
                    paymentMethod = "YAPE (Cod: ${input.yapeCode})",


                    sellerId = sellerId,
                    sellerIds = listOf(sellerId),
                    yapeCode = input.yapeCode,
                    deliveryType = "DELIVERY",
                    deliveryAddress = "${address.street}, ${address.city}",
                    pickupPoint = ""
                )

                // Guardar en Firestore
                firestore.runBatch { batch ->
                    batch.set(newOrderRef, order)
                    group.items.forEach { item ->
                        val cartRef = firestore.collection("users").document(currentUser.uid)
                            .collection("cart").document(item.productId)
                        batch.delete(cartRef)
                    }
                }.await()

                // Actualizar UI
                _state.update { currentState ->
                    val remaining = currentState.orderGroups.filter { it.sellerId != sellerId }
                    currentState.copy(isLoading = false, orderGroups = remaining, paymentSuccess = true)
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
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.bcp.innovacxion.yapeapp"))
                context.startActivity(intent)
            } catch (e2: Exception) {}
        }
    }

    fun resetPaymentSuccess() {
        _state.update { it.copy(paymentSuccess = false) }
    }
}