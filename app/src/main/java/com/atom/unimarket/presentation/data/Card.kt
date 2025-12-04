package com.atom.unimarket.presentation.data

data class SavedCard(
    val id: String = "",
    val last4: String = "",     // Solo guardamos los últimos 4 dígitos por seguridad
    val cardHolder: String = "",
    val expiryDate: String = "",
    val brand: String = "VISA"  // VISA, MASTERCARD, etc.
)