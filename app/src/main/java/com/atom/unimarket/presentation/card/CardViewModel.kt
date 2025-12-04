package com.atom.unimarket.presentation.card

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.SavedCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class CardState(
    val cards: List<SavedCard> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class CardViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(CardState())
    val state = _state.asStateFlow()

    init {
        loadCards()
    }

    fun loadCards() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("cards")
                    .get()
                    .await()

                val cards = snapshot.toObjects(SavedCard::class.java)
                _state.update { it.copy(isLoading = false, cards = cards) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun saveCard(number: String, holder: String, expiry: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                val docRef = firestore.collection("users").document(userId)
                    .collection("cards").document()

                // Determinamos la marca por el primer dígito (lógica simple)
                val brand = if (number.startsWith("4")) "VISA" else "MASTERCARD"

                val newCard = SavedCard(
                    id = docRef.id,
                    last4 = number.takeLast(4), // Guardamos solo lo necesario
                    cardHolder = holder,
                    expiryDate = expiry,
                    brand = brand
                )

                docRef.set(newCard).await()
                loadCards() // Recargamos la lista
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al guardar tarjeta: ${e.message}") }
            }
        }
    }

    fun deleteCard(cardId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("cards").document(cardId)
                    .delete()
                    .await()
                loadCards()
            } catch (e: Exception) {
                // Manejar error silenciosamente o notificar
            }
        }
    }
}