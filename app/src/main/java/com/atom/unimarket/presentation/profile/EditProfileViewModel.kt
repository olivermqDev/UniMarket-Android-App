package com.atom.unimarket.presentation.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.User
import com.atom.unimarket.presentation.data.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent

data class EditProfileState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false,
    val user: User = User()
)

class EditProfileViewModel(
    private val userRepository: UserRepository,
    private val auth: FirebaseAuth
) : ViewModel(), KoinComponent {

    private val _state = MutableStateFlow(EditProfileState())
    val state = _state.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        val uid = auth.currentUser?.uid ?: return
        _state.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            userRepository.getUserProfile(uid).collect { user ->
                _state.update { it.copy(isLoading = false, user = user) }
            }
        }
    }

    fun updateUserProfile(displayName: String, direccion: String, phoneNumber: String) {
        val uid = auth.currentUser?.uid ?: return
        
        if (displayName.isBlank()) {
            _state.update { it.copy(error = "El nombre no puede estar vacío") }
            return
        }

        _state.update { it.copy(isLoading = true, error = null, isSuccess = false) }

        viewModelScope.launch {
            try {
                val updates = mapOf(
                    "displayName" to displayName,
                    "direccion" to direccion,
                    "phoneNumber" to phoneNumber
                )
                userRepository.saveUserProfile(uid, updates)
                _state.update { it.copy(isLoading = false, isSuccess = true) }
            } catch (e: Exception) {
                _state.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun resetSuccessState() {
        _state.update { it.copy(isSuccess = false) }
    }
}
