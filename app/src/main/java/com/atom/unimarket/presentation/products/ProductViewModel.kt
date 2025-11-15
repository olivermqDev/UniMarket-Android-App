package com.atom.unimarket.presentation.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Product
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

// --- INICIO DE CAMBIOS (ProductState) ---
data class ProductState(
    val products: List<Product> = emptyList(), // Esta es la lista del CATÁLOGO
    val selectedProduct: Product? = null, // <-- 1. CAMPO NUEVO para la pantalla de detalle
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadSuccess: Boolean = false,
    val currentSortOption: SortOption = SortOption.NEWEST_FIRST,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val favoriteProductIds: Set<String> = emptySet()
)
// --- FIN DE CAMBIOS (ProductState) ---

data class CartState(
    val cartItems: Map<String, Int> = emptyMap(),
    val cartProducts: List<Product> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
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

    init {
        listenForProductUpdates()
        listenForFavoriteChanges()
    }

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

    private fun filterProducts() {
        val query = _productState.value.searchQuery
        val category = _productState.value.selectedCategory

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
            matchesSearch && matchesCategory
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
                    imageUrls = listOf(imageUrl), sellerUid = user.uid, sellerName = user.displayName ?: "Vendedor anónimo", createdAt = null
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

    // --- INICIO DE CAMBIOS (getProductById) ---
    fun getProductById(productId: String) {
        // 2. Limpiamos el producto anterior y ponemos isLoading
        _productState.update { it.copy(isLoading = true, selectedProduct = null) }

        firestore.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                // 3. Ahora actualizamos el campo 'selectedProduct',
                // ¡NO la lista 'products'!
                _productState.update {
                    it.copy(isLoading = false, selectedProduct = product)
                }
            }
            .addOnFailureListener { exception ->
                _productState.update { it.copy(isLoading = false, error = "Error al cargar: ${exception.message}") }
            }
    }
    // --- FIN DE CAMBIOS (getProductById) ---

    // --- 4. NUEVA FUNCIÓN DE LIMPIEZA ---
    // La llamaremos al salir de la pantalla de detalle
    fun clearSelectedProduct() {
        _productState.update { it.copy(selectedProduct = null) }
    }
    // --- FIN DE NUEVA FUNCIÓN ---

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
        // ¡Esta función SÍ debe modificar la lista 'products'!
        // Porque la usa FavoritesScreen, que no muestra el catálogo.
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

    // --- LÓGICA DEL CARRITO DE COMPRAS (Sin cambios) ---

    fun addToCart(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("users").document(userId).collection("cart").document(productId)
                cartRef.set(mapOf("quantity" to FieldValue.increment(1)), com.google.firebase.firestore.SetOptions.merge()).await()
            } catch (e: Exception) {
                _cartState.update { it.copy(error = "Error al añadir al carrito: ${e.message}") }
            }
        }
    }

    fun getCartContents() {
        val userId = getCurrentUserId() ?: return
        _cartState.update { it.copy(isLoading = true, cartProducts = emptyList(), totalPrice = 0.0) }

        viewModelScope.launch {
            try {
                val cartSnapshot = firestore.collection("users").document(userId).collection("cart").get().await()
                val cartItemsMap = cartSnapshot.documents.associate { it.id to (it.getLong("quantity")?.toInt() ?: 0) }
                val productIds = cartItemsMap.keys.toList()

                if (productIds.isEmpty()) {
                    _cartState.update { it.copy(isLoading = false) }
                    return@launch
                }

                val productsList = firestore.collection("products")
                    .whereIn("id", productIds)
                    .get()
                    .await()
                    .toObjects(Product::class.java)

                var total = 0.0
                for (product in productsList) {
                    total += product.price * (cartItemsMap[product.id] ?: 0)
                }

                _cartState.update {
                    it.copy(
                        isLoading = false,
                        cartItems = cartItemsMap,
                        cartProducts = productsList,
                        totalPrice = total
                    )
                }
            } catch (e: Exception) {
                _cartState.update { it.copy(isLoading = false, error = "Error al cargar el carrito: ${e.message}") }
            }
        }
    }
}

enum class SortOption(val field: String, val direction: Query.Direction) {
    NEWEST_FIRST("createdAt", Query.Direction.DESCENDING),
    PRICE_ASC("price", Query.Direction.ASCENDING),
    PRICE_DESC("price", Query.Direction.DESCENDING)
}