package com.atom.unimarket.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey
    val productId: String,
    val name: String,
    val price: Double,
    val quantity: Int,
    val imageUrl: String,
    val sellerId: String
)
