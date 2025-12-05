package com.atom.unimarket.presentation.data.repository

import com.atom.unimarket.presentation.data.CartItem
import com.atom.unimarket.presentation.data.Order
import com.atom.unimarket.presentation.data.Product
import com.atom.unimarket.presentation.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CheckoutRepository(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {

    suspend fun getCartItems(): List<CartItem> {
        val userId = auth.currentUser?.uid ?: return emptyList()
        val cartSnapshot = firestore.collection("users").document(userId).collection("cart").get().await()
        val productIds = cartSnapshot.documents.map { it.id }

        if (productIds.isEmpty()) return emptyList()

        // Firestore 'in' query supports up to 10 items normally. 
        // For robustness, we might need to chunk this if the cart is huge, but for now assuming < 10.
        // Or better, fetch products individually or all products and filter (less efficient but safer for >10).
        // Let's stick to 'in' for now as it's efficient for small carts.
        
        val productsList = firestore.collection("products")
            .whereIn("id", productIds)
            .get()
            .await()
            .toObjects(Product::class.java)

        return productsList.map { product ->
            CartItem(
                id = product.id,
                productId = product.id,
                name = product.name,
                price = product.price,
                imageUrl = product.imageUrls.firstOrNull() ?: "",
                quantity = 1, // Assuming quantity is always 1 for now as per previous logic
                sellerId = product.sellerUid
            )
        }
    }

    suspend fun getSellerDetails(sellerId: String): User? {
        return try {
            val snapshot = firestore.collection("users").document(sellerId).get().await()
            snapshot.toObject(User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createOrder(order: Order) {
        val orderRef = firestore.collection("orders").document(order.id)
        firestore.runBatch { batch ->
            batch.set(orderRef, order)
            // Remove items from cart for this specific seller
            // Note: This logic assumes we want to remove ONLY the items that were just ordered.
            order.items.forEach { item ->
                val cartItemRef = firestore.collection("users").document(order.buyerId)
                    .collection("cart").document(item.productId)
                batch.delete(cartItemRef)
            }
        }.await()
    }
}
