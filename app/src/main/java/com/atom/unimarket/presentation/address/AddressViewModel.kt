package com.atom.unimarket.presentation.address

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.Address
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AddressState(
    val addresses: List<Address> = emptyList(),
    val selectedAddressId: String? = null, // <-- NUEVO: ID de la dirección seleccionada
    val isLoading: Boolean = false,
    val error: String? = null,
    val saveSuccess: Boolean = false
)

class AddressViewModel(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(AddressState())
    val state = _state.asStateFlow()

    init {
        loadAddresses()
    }

    fun loadAddresses() {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            try {
                val snapshot = firestore.collection("users").document(userId)
                    .collection("addresses")
                    .get()
                    .await()

                val addresses = snapshot.toObjects(Address::class.java)

                // Si no hay seleccionada y hay direcciones, seleccionar la default o la primera
                val currentSelected = _state.value.selectedAddressId
                val defaultSelection = if (currentSelected == null && addresses.isNotEmpty()) {
                    addresses.find { it.isDefault }?.id ?: addresses.first().id
                } else {
                    currentSelected
                }

                _state.update {
                    it.copy(
                        isLoading = false,
                        addresses = addresses,
                        selectedAddressId = defaultSelection
                    )
                }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun selectAddress(addressId: String) {
        _state.update { it.copy(selectedAddressId = addressId) }
    }

    fun addAddress(alias: String, street: String, city: String, zip: String, phone: String) {
        val userId = auth.currentUser?.uid ?: return

        if (alias.isBlank() || street.isBlank() || city.isBlank() || phone.isBlank()) {
            _state.update { it.copy(error = "Por favor completa los campos obligatorios") }
            return
        }

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, saveSuccess = false) }
            try {
                val docRef = firestore.collection("users").document(userId)
                    .collection("addresses").document()

                val newAddress = Address(
                    id = docRef.id,
                    alias = alias,
                    street = street,
                    city = city,
                    zipCode = zip,
                    phoneNumber = phone,
                    isDefault = _state.value.addresses.isEmpty()
                )

                docRef.set(newAddress).await()

                // Al agregar, la seleccionamos automáticamente
                _state.update {
                    it.copy(
                        isLoading = false,
                        saveSuccess = true,
                        selectedAddressId = docRef.id
                    )
                }
                loadAddresses()
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = "Error al guardar: ${e.message}") }
            }
        }
    }

    fun deleteAddress(addressId: String) {
        val userId = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            try {
                firestore.collection("users").document(userId)
                    .collection("addresses").document(addressId)
                    .delete()
                    .await()
                // Si borramos la seleccionada, ponemos null
                if (_state.value.selectedAddressId == addressId) {
                    _state.update { it.copy(selectedAddressId = null) }
                }
                loadAddresses()
            } catch (e: Exception) {
                _state.update { it.copy(error = "Error al eliminar: ${e.message}") }
            }
        }
    }

    fun resetSaveState() {
        _state.update { it.copy(saveSuccess = false, error = null) }
    }

    // Helper para obtener el objeto Address completo seleccionado
    fun getSelectedAddress(): Address? {
        return _state.value.addresses.find { it.id == _state.value.selectedAddressId }
    }
}