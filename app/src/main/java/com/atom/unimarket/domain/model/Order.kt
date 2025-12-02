package com.atom.unimarket.domain.model

data class Order(
    val idPedido: String = "",
    val idComprador: String = "",
    val idVendedor: String = "",
    val productos: List<CartItem> = emptyList(),
    val total: Double = 0.0,
    val codigoYape: String = "",
    val urlComprobante: String = "",
    val tipoEntrega: String = "", // "personal" or "delivery"
    val puntoEntrega: String = "",
    val direccionEntrega: String = "",
    val fechaCreado: Long = System.currentTimeMillis(),
    val estado: String = "pago_reportado"
)
