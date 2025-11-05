package com.atom.unimarket.presentation.data

import com.google.firebase.firestore.ServerTimestamp
import java.util.Date

/**
 * Representa la información básica de un producto dentro de una conversación.
 */
data class ProductInChat(
    val productId: String = "",
    val productName: String = "",
    val productImageUrl: String? = null
) {
    // Constructor vacío requerido por Firestore para la deserialización
    constructor() : this("", "", null)
}


/**
 * Representa el último mensaje en una conversación, para mostrar en la lista de chats.
 */
data class LastMessage(
    val text: String = "",
    val senderId: String = "",
    @ServerTimestamp val timestamp: Date? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", null)
}


/**
 * Representa un documento en la colección 'chats'. Es una conversación completa.
 */
data class Chat(
    val id: String = "",
    val participants: List<String> = emptyList(),
    val productInvolved: ProductInChat = ProductInChat(),
    val lastMessage: LastMessage = LastMessage()
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", emptyList(), ProductInChat(), LastMessage())
}


/**
 * Representa un documento en la subcolección 'messages'. Es un mensaje individual.
 */
data class Message(
    val id: String = "",
    val senderId: String = "",
    val text: String = "",
    @ServerTimestamp val timestamp: Date? = null
) {
    // Constructor vacío requerido por Firestore
    constructor() : this("", "", "", null)
}
