package com.atom.unimarket.presentation.products

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Product
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue // --- NUEVO ---
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
import org.koin.core.component.KoinComponent // <-- 1. IMPORTAR

// --- NUEVO: Añadimos la lista de favoritos al estado ---
data class ProductState(
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val uploadSuccess: Boolean = false,
    val currentSortOption: SortOption = SortOption.NEWEST_FIRST,
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val favoriteProductIds: Set<String> = emptySet() // Contendrá los IDs de productos favoritos
)

data class CartState(
    val cartItems: Map<String, Int> = emptyMap(), // Map de <ProductID, Cantidad>
    val cartProducts: List<Product> = emptyList(),
    val totalPrice: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null
)

// --- INICIO DE CAMBIOS ---
class ProductViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) : ViewModel(), KoinComponent { // <-- 2. AÑADIR KoinComponent
// --- FIN DE CAMBIOS ---

    // --- ESTAS LÍNEAS SE ELIMINAN ---
    // private val firestore = FirebaseFirestore.getInstance()
    // private val auth = FirebaseAuth.getInstance()
    // private val storage = FirebaseStorage.getInstance()

    private val _productState = MutableStateFlow(ProductState())
    val productState = _productState.asStateFlow()

    private var productListener: ListenerRegistration? = null
    private var allProducts: List<Product> = emptyList()

    // --- NUEVO: Listener para la lista de favoritos ---
    private var favoritesListener: ListenerRegistration? = null

    // Dentro de ProductViewModel, debajo de la declaración de _productState
    private val _cartState = MutableStateFlow(CartState())
    val cartState = _cartState.asStateFlow()

    init {
        listenForProductUpdates()
        // --- NUEVO: Escuchar los favoritos del usuario cuando el ViewModel se inicia ---
        listenForFavoriteChanges()
    }

    // --- NUEVO: Función para escuchar los cambios en la subcolección 'favorites' ---
    private fun listenForFavoriteChanges() {
        val userId = getCurrentUserId() ?: return // Si no hay usuario, no hacemos nada
        favoritesListener?.remove() // Removemos el listener anterior si existe

        favoritesListener = firestore.collection("users").document(userId)
            .collection("favorites")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _productState.update { it.copy(error = "Error al cargar favoritos: ${error.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    // Obtenemos los IDs de todos los documentos en la subcolección
                    val favoriteIds = snapshot.documents.map { it.id }.toSet()
                    _productState.update { it.copy(favoriteProductIds = favoriteIds) }
                }
            }
    }

    // --- NUEVO: Función para añadir o quitar un producto de favoritos ---
    fun toggleFavorite(productId: String) {
        val userId = getCurrentUserId() ?: return // Acción protegida, necesita un usuario logueado
        viewModelScope.launch {
            try {
                val favoritesRef = firestore.collection("users").document(userId)
                    .collection("favorites").document(productId)

                // Comprobamos si el producto ya está en favoritos usando el estado local
                if (productState.value.favoriteProductIds.contains(productId)) {
                    // Si está, lo eliminamos
                    favoritesRef.delete().await()
                } else {
                    // Si no está, lo añadimos. Podemos guardar datos adicionales como la fecha.
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
                    imageUrls = listOf(imageUrl), sellerUid = user.uid, sellerName = user.displayName ?: "Vendedor anónimo", createdAt = null // O FieldValue.serverTimestamp() si el campo es Timestamp
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
        _productState.update { it.copy(isLoading = true) }
        firestore.collection("products").document(productId).get()
            .addOnSuccessListener { document ->
                val product = document.toObject(Product::class.java)
                if (product != null) {
                    _productState.update { it.copy(isLoading = false, products = listOf(product)) }
                } else {
                    _productState.update { it.copy(isLoading = false, error = "Producto no encontrado") }
                }
            }
            .addOnFailureListener { exception ->
                _productState.update { it.copy(isLoading = false, error = "Error al cargar: ${exception.message}") }
            }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    // --- NUEVO: Limpiar los listeners cuando el ViewModel se destruye ---
    override fun onCleared() {
        super.onCleared()
        productListener?.remove()
        favoritesListener?.remove()
    }
    // --- NUEVO: Función para obtener los detalles de los productos favoritos ---
    fun getFavoriteProducts() {
        val userId = getCurrentUserId() ?: return
        _productState.update { it.copy(isLoading = true, products = emptyList()) } // Limpiamos la lista anterior

        viewModelScope.launch {
            try {
                // 1. Obtenemos los IDs de los favoritos desde el estado actual
                val favoriteIds = _productState.value.favoriteProductIds
                if (favoriteIds.isEmpty()) {
                    // Si no hay favoritos, no hay nada que buscar
                    _productState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // 2. Hacemos una consulta a Firestore para obtener los productos cuyos IDs están en nuestra lista
                // Firestore permite hasta 30 IDs en una consulta "in"
                val favoriteProducts = firestore.collection("products")
                    .whereIn("id", favoriteIds.toList())
                    .get()
                    .await()
                    .toObjects(Product::class.java)

                // 3. Actualizamos el estado con los productos encontrados
                _productState.update { it.copy(isLoading = false, products = favoriteProducts) }

            } catch (e: Exception) {
                _productState.update { it.copy(isLoading = false, error = "Error al cargar favoritos: ${e.message}") }
            }
        }
    }

    // --- INICIO: LÓGICA DEL CARRITO DE COMPRAS ---

    fun addToCart(productId: String) {
        val userId = getCurrentUserId() ?: return
        viewModelScope.launch {
            try {
                val cartRef = firestore.collection("users").document(userId).collection("cart").document(productId)
                // Usamos FieldValue.increment para añadir 1 a la cantidad de forma atómica
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
                // 1. Obtenemos los IDs y cantidades del carrito del usuario
                val cartSnapshot = firestore.collection("users").document(userId).collection("cart").get().await()
                val cartItemsMap = cartSnapshot.documents.associate { it.id to (it.getLong("quantity")?.toInt() ?: 0) }
                val productIds = cartItemsMap.keys.toList()

                if (productIds.isEmpty()) {
                    _cartState.update { it.copy(isLoading = false) }
                    return@launch
                }

                // 2. Obtenemos los detalles de esos productos
                val productsList = firestore.collection("products")
                    .whereIn("id", productIds)
                    .get()
                    .await()
                    .toObjects(Product::class.java)

                // 3. Calculamos el precio total
                var total = 0.0
                for (product in productsList) {
                    total += product.price * (cartItemsMap[product.id] ?: 0)
                }

                // 4. Actualizamos el estado
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

// --- FIN: LÓGICA DEL CARRITO DE COMPRAS ---

}

enum class SortOption(val field: String, val direction: Query.Direction) {
    NEWEST_FIRST("createdAt", Query.Direction.DESCENDING),
    PRICE_ASC("price", Query.Direction.ASCENDING),
    PRICE_DESC("price", Query.Direction.DESCENDING)
}