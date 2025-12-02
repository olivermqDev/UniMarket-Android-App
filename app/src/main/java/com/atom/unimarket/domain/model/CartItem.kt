package com.atom.unimarket.domain.model

data class CartItem(
    val productId: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val quantity: Int = 0,
    val imageUrl: String = "",
    val sellerId: String = ""
)
