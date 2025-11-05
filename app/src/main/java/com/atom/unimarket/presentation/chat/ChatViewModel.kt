package com.atom.unimarket.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Chat
import com.atom.unimarket.presentation.data.LastMessage
import com.atom.unimarket.presentation.data.Message
import com.atom.unimarket.presentation.data.Product
import com.atom.unimarket.presentation.data.ProductInChat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Estado para la pantalla de chat individual
data class ChatUiState(
    val messages: List<Message> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val chatPartnerName: String? = null
)

// --- INICIO DE CAMBIOS ---

// Estado para la pantalla de la lista de conversaciones
data class ConversationsUiState(
    val chats: List<Chat> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // StateFlow para la pantalla de chat individual
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState = _uiState.asStateFlow()

    // StateFlow para la lista de conversaciones
    private val _conversationsUiState = MutableStateFlow(ConversationsUiState())
    val conversationsUiState = _conversationsUiState.asStateFlow()

    private var messagesListener: ListenerRegistration? = null
    private var conversationsListener: ListenerRegistration? = null // Listener para la lista

    // --- NUEVA FUNCIÓN ---
    // Función para empezar a escuchar la lista de conversaciones del usuario
    fun listenForConversations() {
        val currentUserUid = auth.currentUser?.uid ?: return
        _conversationsUiState.update { it.copy(isLoading = true) }

        // Limpiamos el listener anterior para evitar duplicados
        conversationsListener?.remove()

        conversationsListener = firestore.collection("chats")
            // Busca todos los chats donde el array 'participants' contiene el ID del usuario actual
            .whereArrayContains("participants", currentUserUid)
            // Ordena los chats por el timestamp del último mensaje, para mostrar los más recientes primero
            .orderBy("lastMessage.timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _conversationsUiState.update { it.copy(isLoading = false, error = "Error al cargar conversaciones.") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val chats = snapshot.toObjects(Chat::class.java)
                    _conversationsUiState.update { it.copy(isLoading = false, chats = chats) }
                }
            }
    }

    // --- FIN DE CAMBIOS ---

    // Función para empezar a escuchar los mensajes de un chat específico
    fun listenForMessages(chatId: String) {
        _uiState.update { it.copy(isLoading = true) }
        messagesListener?.remove()
        messagesListener = firestore.collection("chats").document(chatId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _uiState.update { it.copy(isLoading = false, error = "Error al cargar mensajes: ${error.message}") }
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val messages = snapshot.toObjects(Message::class.java)
                    _uiState.update { it.copy(isLoading = false, messages = messages) }
                }
            }
    }

    // Función para enviar un mensaje
    fun sendMessage(chatId: String, text: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        if (text.isBlank()) return

        val message = Message(
            senderId = currentUserId,
            text = text,
            timestamp = null
        )

        val chatRef = firestore.collection("chats").document(chatId)

        firestore.runBatch { batch ->
            val newMessageRef = chatRef.collection("messages").document()
            batch.set(newMessageRef, message)

            val lastMessageData = mapOf(
                "text" to text,
                "senderId" to currentUserId,
                "timestamp" to FieldValue.serverTimestamp()
            )
            batch.update(chatRef, "lastMessage", lastMessageData)

        }.addOnFailureListener {
            _uiState.update { it.copy(error = "Error al enviar el mensaje.") }
        }
    }

    // Función para crear o encontrar un chat y navegar
    fun findOrCreateChat(product: Product, onComplete: (chatId: String) -> Unit) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val currentUserUid = auth.currentUser?.uid ?: throw Exception("Usuario no autenticado")
                // No permitimos que un vendedor inicie un chat consigo mismo
                if (currentUserUid == product.sellerUid) {
                    // Podríamos mostrar un Toast o un mensaje en la UI
                    _uiState.update { it.copy(isLoading = false, error = "No puedes iniciar un chat sobre tu propio producto.") }
                    return@launch
                }
                val sellerUid = product.sellerUid

                val participants = listOf(currentUserUid, sellerUid).sorted()
                val chatId = participants.joinToString(separator = "_")
                val chatRef = firestore.collection("chats").document(chatId)

                val chatDocument = chatRef.get().await()

                if (!chatDocument.exists()) {
                    // El chat NO existe, lo creamos por primera vez.
                    val newChat = Chat(
                        id = chatId,
                        participants = participants,
                        productInvolved = ProductInChat(
                            productId = product.id,
                            productName = product.name,
                            productImageUrl = product.imageUrls.firstOrNull()
                        ),
                        lastMessage = LastMessage()
                    )
                    chatRef.set(newChat).await()

                    // Opcional: Enviamos un mensaje automático la primera vez que se crea.
                    val initialMessage = "Hola, estoy interesado en tu producto: ${product.name}"
                    sendMessage(chatId, initialMessage)

                } else {
                    // El chat YA EXISTE.
                    // Aquí podrías añadir una lógica si quisieras. Por ahora, solo navegamos.
                    // Una mejora avanzada sería comprobar si el último mensaje es sobre este
                    // mismo producto para no enviar mensajes repetidos.
                    // Por simplicidad, por ahora no hacemos nada extra.
                }

                _uiState.update { it.copy(isLoading = false) }
                onComplete(chatId)

            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = "Error al iniciar el chat: ${e.message}") }
            }
        }
    }

    // Al cerrar el ViewModel, es importante limpiar TODOS los listeners
    override fun onCleared() {
        super.onCleared()
        messagesListener?.remove()
        conversationsListener?.remove() // <-- CAMBIO: Limpiar también el listener de conversaciones
    }
}
