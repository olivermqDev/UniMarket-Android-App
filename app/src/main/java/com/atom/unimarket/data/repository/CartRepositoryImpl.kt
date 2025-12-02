package com.atom.unimarket.data.repository

import com.atom.unimarket.data.local.dao.CartDao
import com.atom.unimarket.data.local.entity.CartEntity
import com.atom.unimarket.domain.model.CartItem
import com.atom.unimarket.domain.repository.CartRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CartRepositoryImpl(
    private val cartDao: CartDao
) : CartRepository {

    override fun getCartItems(): Flow<List<CartItem>> {
        return cartDao.getCartItems().map { entities ->
            entities.map { entity ->
                CartItem(
                    productId = entity.productId,
                    name = entity.name,
                    price = entity.price,
                    quantity = entity.quantity,
                    imageUrl = entity.imageUrl,
                    sellerId = entity.sellerId
                )
            }
        }
    }

    override suspend fun addToCart(cartItem: CartItem) {
        cartDao.insertCartItem(
            CartEntity(
                productId = cartItem.productId,
                name = cartItem.name,
                price = cartItem.price,
                quantity = cartItem.quantity,
                imageUrl = cartItem.imageUrl,
                sellerId = cartItem.sellerId
            )
        )
    }

    override suspend fun removeFromCart(cartItem: CartItem) {
        cartDao.deleteCartItem(
            CartEntity(
                productId = cartItem.productId,
                name = cartItem.name,
                price = cartItem.price,
                quantity = cartItem.quantity,
                imageUrl = cartItem.imageUrl,
                sellerId = cartItem.sellerId
            )
        )
    }

    override suspend fun clearCart() {
        cartDao.clearCart()
    }

    override fun getTotalPrice(): Flow<Double> {
        return cartDao.getTotalPrice().map { it ?: 0.0 }
    }
}
