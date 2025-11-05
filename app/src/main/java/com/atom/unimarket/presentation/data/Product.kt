package com.atom.unimarket.presentation.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

@Keep // <-- ANOTACIÓN IMPORTANTE
data class Product(val id: String = "",
                   val name: String = "",
                   val description: String = "",
                   val price: Double = 0.0,
                   val imageUrls: List<String> = emptyList(),
                   val sellerUid: String = "",
                   val sellerName: String = "",
                   val category: String = "Otros",
                   @ServerTimestamp
                   val createdAt: Timestamp? = null
) {
    // Constructor vacío requerido por Firestore para la deserialización con toObjects()
    constructor() : this("", "", "", 0.0, emptyList(), "", "", "Otros", null)
}
