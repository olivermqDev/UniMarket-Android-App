package com.atom.unimarket.presentation.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Address
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.data.Product
import com.atom.unimarket.presentation.data.toOrderItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import org.koin.core.component.KoinComponent

data class ProductState(
    val products: List<Product> = emptyList(),
    val selectedProduct: Product? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadSuccess: Boolean = false,
    val currentSortOption: SortOption = SortOption.NEWEST_FIRST,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val favoriteProductIds: Set<String> = emptySet()
)

data class CartState(
    val cartItems: Map<String, Int> = emptyMap(), // productId -> quantity
    val cartProducts: List<Product> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val checkoutSuccess: Boolean = false
)

class ProductViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ViewModel(), KoinComponent {

    private val _productState = MutableStateFlow(ProductState())
    val productState = _productState.asStateFlow()

    private var productListener: ListenerRegistration? = null
    private var allProducts: List<Product> = emptyList()

    private var favoritesListener: ListenerRegistration? = null

    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    private var currentShippingAddress: Address? = null

    init {
        listenForProductUpdates()
        listenForFavoriteChanges()
        getCartContents() // Load cart on init
    }

    fun setShippingAddress(address: Address) {
        currentShippingAddress = address
    }  fun getShippingAddress(): Address? { return currentShippingAddress  }

    private fun listenForFavoriteChanges() {
        val userId = getCurrentUserId() ?: return
        favoritesListener?.remove()

        favoritesListener = firestore.collection("users").document(userId)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _productState.update { it.copy(error = "Error al cargar favoritos: ${error.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val favoriteIds = snapshot.documents.map { it.id }.toSet()
                    _productState.update { it.copy(favoriteProductIds = favoriteIds) }
                }
            }
    }

    fun toggleFavorite(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val favoritesRef = firestore.collection("users").document(userId)
                    .collection("favorites").document(productId)

                if (productState.value.favoriteProductIds.contains(productId)) {
                    favoritesRef.delete().await()
                } else {
                    favoritesRef.set(mapOf("addedAt" to FieldValue.serverTimestamp())).await()
                }
            } catch (e: Exception) {
                _productState.update { it.copy(error = "Error al actualizar favoritos: ${e.message}") }
            }
        }
    }


    private fun listenForProductUpdates() {
        _productState.update { it.copy(isLoading = true) }
        productListener?.remove()

        val sortOption = _productState.value.currentSortOption
        productListener = firestore.collection("products")
            .orderBy(sortOption.field, sortOption.direction)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _productState.update { it.copy(isLoading = false, error = "Error: ${error.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    allProducts = snapshot.toObjects(Product::class.java)
                    filterProducts()
                }
            }
    }

    fun loadUserProducts() {
        val userId = getCurrentUserId() ?: return

        _productState.update { it.copy(isLoading = true) }

        productListener?.remove()
        productListener = firestore.collection("products")
            .whereEqualTo("sellerUid", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _productState.update {
                        it.copy(isLoading = false, error = "Error: ${error.message}")
                    }
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val userProducts = snapshot.toObjects(Product::class.java)
                    _productState.update {
                        it.copy(
                            isLoading = false,
                            products = userProducts,
                            error = null
                        )
                    }
                }
            }
    }

    private fun filterProducts() {
        val query = _productState.value.searchQuery
        val category = _productState.value.selectedCategory
        val currentUserId = getCurrentUserId()

        val filteredList = allProducts.filter { product ->
            val matchesSearch = if (query.isBlank()) {
                true
            } else {
                product.name.contains(query, ignoreCase = true)
            }
            val matchesCategory = if (category == null) {
                true
            } else {
                product.category.equals(category, ignoreCase = true)
            }
            val isNotOwnProduct = product.sellerUid != currentUserId

            matchesSearch && matchesCategory && isNotOwnProduct
        }

        _productState.update { it.copy(isLoading = false, products = filteredList) }
    }

    fun onSearchQueryChange(query: String) {
        _productState.update { it.copy(searchQuery = query) }
        filterProducts()
    }

    fun onCategorySelected(category: String?) {
        _productState.update { it.copy(selectedCategory = category) }
        filterProducts()
    }

    fun changeSortOrder(newSortOption: SortOption) {
        if (_productState.value.currentSortOption == newSortOption) return
        _productState.update { it.copy(currentSortOption = newSortOption) }
        listenForProductUpdates()
    }

    fun addProduct(name: String, description: String, price: Double, category: String, imageUri: Uri?, onComplete: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            _productState.update { it.copy(isLoading = true, error = null) }
            try {
                val user = auth.currentUser ?: throw Exception("Usuario no autenticado")
                val imageUrl = imageUri?.let {
                    val imageRef = storage.reference.child("product_images/${UUID.randomUUID()}.jpg")
                    imageRef.putFile(it).await()
                    imageRef.downloadUrl.await().toString()
                } ?: throw Exception("Debe seleccionar una imagen para el producto.")

                val productId = firestore.collection("products").document().id
                val newProduct = Product(
                    id = productId, name = name, description = description, price = price, category = category,
                    imageUrls = listOf(imageUrl), sellerUid = user.uid, sellerName = user.displayName ?: "Vendedor anÃ³nimo", createdAt = null
                )
                firestore.collection("products").document(productId).set(newProduct).await()

                _productState.update { it.copy(isLoading = false, uploadSuccess = true) }
                onComplete(true, null)
            } catch (e: Exception) {
                _productState.update { it.copy(isLoading = false, error = e.message) }
                onComplete(false, e.message)
            }
        }
    }

    fun resetProductState() {
        _productState.update { it.copy(error = null, uploadSuccess = false) }
    }

    fun getProductById(productId: String) {
        _productState.update { it.copy(isLoading = true, selectedProduct = null) }

        firestore.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                _productState.update {
                    it.copy(isLoading = false, selectedProduct = product)
                }
            }
            .addOnFailureListener { exception ->
                _productState.update { it.copy(isLoading = false, error = "Error al cargar: ${exception.message}") }
            }
    }

    fun clearSelectedProduct() {
        _productState.update { it.copy(selectedProduct = null) }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    override fun onCleared() {
        super.onCleared()
        productListener?.remove()
        favoritesListener?.remove()
    }

    fun getFavoriteProducts() {
        val userId = getCurrentUserId() ?: return
        _productState.update { it.copy(isLoading = true, products = emptyList()) }

        viewModelScope.launch {
            try {
                val favoriteIds = _productState.value.favoriteProductIds
                if (favoriteIds.isEmpty()) {
                    _productState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val favoriteProducts = firestore.collection("products")
                    .whereIn("id", favoriteIds.toList())
                    .get()
                    .await()
                    .toObjects(Product::class.java)

                _productState.update { it.copy(isLoading = false, products = favoriteProducts) }

            } catch (e: Exception) {
                _productState.update { it.copy(isLoading = false, error = "Error al cargar favoritos: ${e.message}") }
            }
        }
    }

    // --- CART LOGIC ---

    fun addToCart(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("users").document(userId).collection("cart").document(productId)
                val snapshot = cartRef.get().await()
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 1
                    cartRef.update("quantity", currentQuantity + 1).await()
                } else {
                    cartRef.set(mapOf("productId" to productId, "quantity" to 1, "addedAt" to FieldValue.serverTimestamp())).await()
                }
                getCartContents()
            } catch (e: Exception) {
                _cartState.update { it.copy(error = "Error al aÃ±adir al carrito: ${e.message}") }
            }
        }
    }

    fun increaseQuantity(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("users").document(userId).collection("cart").document(productId)
                val snapshot = cartRef.get().await()
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 1
                    cartRef.update("quantity", currentQuantity + 1).await()
                    getCartContents()
                }
            } catch (e: Exception) {
                _cartState.update { it.copy(error = "Error al aumentar cantidad: ${e.message}") }
            }
        }
    }

    fun decreaseQuantity(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("users").document(userId).collection("cart").document(productId)
                val snapshot = cartRef.get().await()
                if (snapshot.exists()) {
                    val currentQuantity = snapshot.getLong("quantity")?.toInt() ?: 1
                    if (currentQuantity > 1) {
                        cartRef.update("quantity", currentQuantity - 1).await()
                    } else {
                        cartRef.delete().await()
                    }
                    getCartContents()
                }
            } catch (e: Exception) {
                _cartState.update { it.copy(error = "Error al disminuir cantidad: ${e.message}") }
            }
        }
    }

    fun removeFromCart(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("cart").document(productId)
                    .delete()
                    .await()
                getCartContents()
            } catch (e: Exception) {
                _cartState.update { it.copy(error = "Error al eliminar del carrito: ${e.message}") }
            }
        }
    }

    fun getCartContents() {
        val userId = getCurrentUserId() ?: return
        _cartState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            try {
                val cartSnapshot = firestore.collection("users").document(userId).collection("cart").get().await()
                val cartItems = cartSnapshot.documents.associate { doc ->
                    doc.id to (doc.getLong("quantity")?.toInt() ?: 1)
                }

                if (cartItems.isEmpty()) {
                    _cartState.update { it.copy(isLoading = false, cartProducts = emptyList(), cartItems = emptyMap(), totalPrice = 0.0) }
                    return@launch
                }

                val products = firestore.collection("products")
                    .whereIn("id", cartItems.keys.toList())
                    .get()
                    .await()
                    .toObjects(Product::class.java)

                val totalPrice = products.sumOf { product ->
                    product.price * (cartItems[product.id] ?: 1)
                }

                _cartState.update {
                    it.copy(isLoading = false, cartProducts = products, cartItems = cartItems, totalPrice = totalPrice)
                }
            } catch (e: Exception) {
                _cartState.update { it.copy(isLoading = false, error = "Error al cargar carrito: ${e.message}") }
            }
        }
    }

    fun checkout(paymentMethod: String) {
        val userId = getCurrentUserId() ?: return
        val currentState = _cartState.value

        if (currentState.cartProducts.isEmpty()) return

        if (currentShippingAddress == null) {
            _cartState.update { it.copy(error = "Error: No se ha seleccionado una direcciÃ³n de envÃ­o.") }
            return
        }

        _cartState.update { it.copy(isLoading = true, error = null) }

        viewModelScope.launch {
            try {
                val batch = firestore.batch()
                val orderId = UUID.randomUUID().toString()
                
                // Create Order Items with correct quantity
                val orderItems = currentState.cartProducts.map { product ->
                    val quantity = currentState.cartItems[product.id] ?: 1
                    product.toOrderItem(quantity)
                }

                val order = Order(
                    id = orderId,
                    buyerId = userId,
                    items = orderItems,
                    totalAmount = currentState.totalPrice,
                    status = "Pendiente",
                    createdAt = null, // Server timestamp
                    shippingAddress = currentShippingAddress,
                    paymentMethod = paymentMethod
                )

                val orderRef = firestore.collection("orders").document(orderId)
                batch.set(orderRef, order)
                // Important: Set server timestamp for createdAt
                batch.update(orderRef, "createdAt", FieldValue.serverTimestamp())

                // Clear cart
                currentState.cartProducts.forEach { product ->
                    val cartItemRef = firestore.collection("users").document(userId)
                        .collection("cart").document(product.id)
                    batch.delete(cartItemRef)
                }

                batch.commit().await()

                _cartState.update {
                    it.copy(isLoading = false, cartProducts = emptyList(), cartItems = emptyMap(), totalPrice = 0.0, checkoutSuccess = true)
                }
                currentShippingAddress = null

            } catch (e: Exception) {
                _cartState.update { it.copy(isLoading = false, error = "Error en el checkout: ${e.message}") }
            }
        }
    }

    fun resetCheckoutState() {
        _cartState.update { it.copy(checkoutSuccess = false) }
    }

    fun processPayment(method: PaymentMethod) {
        checkout(method.name)
    }

}

enum class SortOption(val field: String, val direction: Query.Direction) {
    NEWEST_FIRST("createdAt", Query.Direction.DESCENDING),
    PRICE_ASC("price", Query.Direction.ASCENDING),
    PRICE_DESC("price", Query.Direction.DESCENDING)
}

enum class PaymentMethod {
    CARD, YAPE, CASH
}
