package com.atom.unimarket.presentation.data

// Modelo de datos que coincide con la estructura de tu documento de usuario en Firestore.
data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String = "",
    val direccion: String = "",
    val phoneNumber: String = "",
    val fcmToken: String = ""
)
