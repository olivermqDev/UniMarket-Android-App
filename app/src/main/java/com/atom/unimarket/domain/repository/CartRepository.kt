package com.atom.unimarket.domain.repository

import com.atom.unimarket.domain.model.CartItem
import kotlinx.coroutines.flow.Flow

interface CartRepository {
    fun getCartItems(): Flow<List<CartItem>>
    suspend fun addToCart(cartItem: CartItem)
    suspend fun removeFromCart(cartItem: CartItem)
    suspend fun clearCart()
    fun getTotalPrice(): Flow<Double>
}
