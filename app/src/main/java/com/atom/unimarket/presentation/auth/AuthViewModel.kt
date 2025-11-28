package com.atom.unimarket.presentation.auth

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atom.unimarket.presentation.data.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID
import org.koin.core.component.KoinComponent

// Estado que la UI observará
data class AuthState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

// Función de extensión para observar los cambios de estado de auth
fun FirebaseAuth.getAuthState(): Flow<FirebaseAuth> = callbackFlow {
    val listener = FirebaseAuth.AuthStateListener { auth ->
        trySend(auth)
    }
    addAuthStateListener(listener)
    awaitClose { removeAuthStateListener(listener) }
}

// --- INICIO DE CAMBIOS ---
class AuthViewModel(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val storage: FirebaseStorage
) : ViewModel(), KoinComponent {
// --- FIN DE CAMBIOS ---

    // --- ESTAS LÍNEAS SE ELIMINAN ---
    // private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    // private val storage: FirebaseStorage = FirebaseStorage.getInstance()

    private val _authState = MutableStateFlow(AuthState())
    val authState = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            auth.getAuthState().collect { authInstance ->
                val firebaseUser = authInstance.currentUser
                if (firebaseUser != null) {
                    listenToUserData(firebaseUser.uid)
                } else {
                    _authState.value = AuthState()
                }
            }
        }
    }

    private fun listenToUserData(userId: String) {
        firestore.collection("users").document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _authState.value = _authState.value.copy(error = "Error al cargar datos.", isLoading = false)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject<User>()
                    _authState.value = AuthState(user = user) // Actualiza el estado con el usuario de Firestore
                }
            }
    }

    // --- FUNCIÓN DE LOGIN (NUEVA) ---
    fun login(email: String, password: String) {
        if(email.isBlank() || password.isBlank()){
            _authState.value = _authState.value.copy(error = "Correo y contraseña no pueden estar vacíos.")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                auth.signInWithEmailAndPassword(email, password).await()
                // El `init` se encargará de detectar el cambio de estado y cargar los datos
            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    // --- FUNCIÓN DE REGISTRO (NUEVA Y COMPLETA) ---
    fun signUp(email: String, password: String, username: String) {
        if(email.isBlank() || password.isBlank() || username.isBlank()){
            _authState.value = _authState.value.copy(error = "Todos los campos son obligatorios.")
            return
        }

        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            try {
                // 1. Crear usuario en Firebase Auth
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val firebaseUser = authResult.user ?: throw Exception("Error al crear el usuario.")

                // 2. Actualizar el nombre de perfil en Firebase Auth
                val profileUpdates = userProfileChangeRequest {
                    displayName = username
                }
                firebaseUser.updateProfile(profileUpdates).await()

                // 3. Crear nuestro propio objeto User para Firestore
                val newUser = User(
                    uid = firebaseUser.uid,
                    displayName = username,
                    email = email,
                    photoUrl = "" // La foto de perfil empieza vacía
                )

                // 4. Guardar el objeto User en la colección "users" de Firestore
                firestore.collection("users").document(firebaseUser.uid).set(newUser).await()
                // El `init` detectará el nuevo usuario y actualizará el estado automáticamente

            } catch (e: Exception) {
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun uploadProfileImage(imageUri: Uri, onComplete: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            // Obtenemos el ID del usuario de forma segura.
            val userId = auth.currentUser?.uid
            if (userId == null) {
                // Si no hay usuario, llamamos al callback con error y salimos.
                onComplete(false, "Usuario no autenticado.")
                return@launch
            }

            try {
                // Indicador de carga (opcional pero bueno para la UX)
                _authState.value = _authState.value.copy(isLoading = true)

                val imageRef = storage.reference.child("profile_images/${userId}/${UUID.randomUUID()}.jpg")
                val uploadTask = imageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                // Actualizamos el campo en Firestore
                firestore.collection("users").document(userId).update("photoUrl", downloadUrl).await()

                // 2. LLAMAR AL CALLBACK CON ÉXITO
                // El listener de Firestore se encargará de actualizar el estado,
                // pero este callback nos permite mostrar un Toast en el momento preciso.
                onComplete(true, null)

                // Ya no es necesario el isLoading porque el listener actualizará el estado
                // _authState.value = _authState.value.copy(isLoading = false)

            } catch (e: Exception) {
                // 3. LLAMAR AL CALLBACK CON ERROR
                onComplete(false, e.message)
                // Actualizamos el estado para reflejar el error
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }


    fun updateDisplayName(newName: String) {
        // ... (código existente sin cambios)
    }
    fun updateAddress(imageUri: Uri, onComplete: (success: Boolean, error: String?) -> Unit) {
        viewModelScope.launch {
            // Obtenemos el ID del usuario de forma segura.
            val userId = auth.currentUser?.uid
            if (userId == null) {
                // Si no hay usuario, llamamos al callback con error y salimos.
                onComplete(false, "Usuario no autenticado.")
                return@launch
            }

            try {
                // Indicador de carga (opcional pero bueno para la UX)
                _authState.value = _authState.value.copy(isLoading = true)

                val imageRef = storage.reference.child("profile_images/${userId}/${UUID.randomUUID()}.jpg")
                val uploadTask = imageRef.putFile(imageUri).await()
                val downloadUrl = uploadTask.storage.downloadUrl.await().toString()

                // Actualizamos el campo en Firestore
                firestore.collection("users").document(userId).update("photoUrl", downloadUrl).await()

                // 2. LLAMAR AL CALLBACK CON ÉXITO
                // El listener de Firestore se encargará de actualizar el estado,
                // pero este callback nos permite mostrar un Toast en el momento preciso.
                onComplete(true, null)

                // Ya no es necesario el isLoading porque el listener actualizará el estado
                // _authState.value = _authState.value.copy(isLoading = false)

            } catch (e: Exception) {
                // 3. LLAMAR AL CALLBACK CON ERROR
                onComplete(false, e.message)
                // Actualizamos el estado para reflejar el error
                _authState.value = _authState.value.copy(isLoading = false, error = e.message)
            }
        }
    }

    fun signOut() {
        auth.signOut()
    }
}