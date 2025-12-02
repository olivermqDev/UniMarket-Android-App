package com.atom.unimarket.domain.repository

import com.atom.unimarket.domain.model.OrderGroup

interface CheckoutRepository {
    suspend fun loadGroupedOrders(): List<OrderGroup>
}
