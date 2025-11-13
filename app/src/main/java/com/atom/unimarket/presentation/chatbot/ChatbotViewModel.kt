package com.atom.unimarket.presentation.chatbot

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.repository.ChatbotRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class BotMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

class ChatbotViewModel : ViewModel() {
    private val chatbotRepository = ChatbotRepository()

    private val _messages = MutableStateFlow<List<BotMessage>>(
        listOf(
            BotMessage(
                text = "¡Hola! Soy el asistente virtual de UniMarket. ¿En qué puedo ayudarte hoy?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<BotMessage>> = _messages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun sendMessage(message: String) {
        if (message.isBlank()) return

        viewModelScope.launch {
            // Agregar mensaje del usuario
            _messages.value = _messages.value + BotMessage(text = message, isUser = true)

            // Mostrar indicador de carga
            _isLoading.value = true

            // Obtener respuesta del chatbot
            chatbotRepository.getChatbotResponse(message)
                .onSuccess { response ->
                    _messages.value = _messages.value + BotMessage(
                        text = response,
                        isUser = false
                    )
                }
                .onFailure { exception ->
                    _messages.value = _messages.value + BotMessage(
                        text = "Lo siento, ocurrió un error: ${exception.message}",
                        isUser = false
                    )
                }

            _isLoading.value = false
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            BotMessage(
                text = "¡Hola! Soy el asistente virtual de UniMarket. ¿En qué puedo ayudarte hoy?",
                isUser = false
            )
        )
    }
}
