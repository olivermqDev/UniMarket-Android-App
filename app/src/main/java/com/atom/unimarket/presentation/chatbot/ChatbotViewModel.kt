package com.atom.unimarket.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Date

// Define quién envía el mensaje: el usuario o el bot
enum class MessageAuthor {
    USER, BOT
}

// Representa un único mensaje en la conversación del chatbot
data class ChatbotMessage(
    val text: String,
    val author: MessageAuthor,
    val timestamp: Date = Date()
)

// Estado de la UI para la pantalla del chatbot
data class ChatbotUiState(
    val messages: List<ChatbotMessage> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class ChatbotViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ChatbotUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Mensaje de bienvenida inicial del bot
        _uiState.update {
            it.copy(
                messages = listOf(
                    ChatbotMessage(
                        text = "¡Hola! Soy tu asistente de compras UniMarket. ¿En qué puedo ayudarte hoy? Puedes preguntarme sobre productos, precios o disponibilidad.",
                        author = MessageAuthor.BOT
                    )
                )
            )
        }
    }

    // Función que se llamará cuando el usuario envíe un mensaje
    fun sendMessageToBot(userInput: String) {
        if (userInput.isBlank()) return

        // 1. Añade inmediatamente el mensaje del usuario a la lista para que se muestre en la UI
        val userMessage = ChatbotMessage(text = userInput, author = MessageAuthor.USER)
        _uiState.update {
            it.copy(
                messages = it.messages + userMessage,
                isLoading = true // Muestra un indicador de que el bot está "pensando"
            )
        }

        // 2. Simula una llamada a la API y una respuesta del bot (AQUÍ IRÁ LA LÓGICA DE LA IA)
        viewModelScope.launch {
            // ----- INICIO DEL SIMULADOR DE IA -----
            // Esto lo reemplazaremos con la llamada real a la API de Gemini
            kotlinx.coroutines.delay(2000) // Simula el tiempo de respuesta de la red
            val botResponseText = "He recibido tu mensaje: \"$userInput\". Estoy procesando tu consulta..."
            // ----- FIN DEL SIMULADOR DE IA -----

            val botMessage = ChatbotMessage(text = botResponseText, author = MessageAuthor.BOT)

            // 3. Añade la respuesta del bot a la lista y oculta el indicador de carga
            _uiState.update {
                it.copy(
                    messages = it.messages + botMessage,
                    isLoading = false
                )
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
