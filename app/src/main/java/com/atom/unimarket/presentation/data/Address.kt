package com.atom.unimarket.presentation.data

data class Address(
    val id: String = "",
    val alias: String = "", // Ej: "Casa", "Oficina"
    val street: String = "",
    val city: String = "",
    val zipCode: String = "",
    val phoneNumber: String = "",
    val isDefault: Boolean = false
)