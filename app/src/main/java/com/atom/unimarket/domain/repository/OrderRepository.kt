package com.atom.unimarket.domain.repository

import com.atom.unimarket.domain.model.Order
import kotlinx.coroutines.flow.Flow

interface OrderRepository {
    suspend fun createOrder(order: Order): Result<Unit>
    fun getOrdersBySeller(sellerId: String): Flow<List<Order>>
    suspend fun updateOrderStatus(orderId: String, status: String): Result<Unit>
    fun getOrdersByBuyer(buyerId: String): Flow<List<Order>>
}
