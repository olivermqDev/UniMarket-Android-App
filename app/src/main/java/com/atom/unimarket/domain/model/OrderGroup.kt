package com.atom.unimarket.domain.model

data class OrderGroup(
    val idVendedor: String = "",
    val sellerName: String = "",
    val sellerPhone: String = "",
    val sellerFcmToken: String = "",
    val items: List<CartItem> = emptyList(),
    val subtotal: Double = 0.0,
    var deliveryType: String? = null,
    var pickupPoint: String? = null,
    var deliveryAddress: String? = null
)
