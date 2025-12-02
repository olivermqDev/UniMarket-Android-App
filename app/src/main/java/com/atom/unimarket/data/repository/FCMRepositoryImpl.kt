package com.atom.unimarket.data.repository

import android.util.Log
import com.atom.unimarket.domain.repository.FCMRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

/**
 * Implementación del repositorio FCM que gestiona el token de notificaciones push.
 */
class FCMRepositoryImpl(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) : FCMRepository {

    companion object {
        private const val TAG = "FCMRepository"
        private const val USERS_COLLECTION = "users"
        private const val FCM_TOKEN_FIELD = "fcmToken"
    }

    override suspend fun saveFCMToken(): Result<String> {
        return try {
            // Verificar que hay un usuario autenticado
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "No hay usuario autenticado, no se puede guardar el token FCM")
                return Result.failure(Exception("Usuario no autenticado"))
            }

            // Obtener el token FCM del dispositivo
            val token = messaging.token.await()
            Log.d(TAG, "Token FCM obtenido: $token")

            // Guardar el token en Firestore en el documento del usuario
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .update(FCM_TOKEN_FIELD, token)
                .await()

            Log.d(TAG, "Token FCM guardado exitosamente en Firestore para el usuario: ${currentUser.uid}")
            Result.success(token)

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar el token FCM", e)
            Result.failure(e)
        }
    }

    override suspend fun deleteFCMToken(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser == null) {
                Log.w(TAG, "No hay usuario autenticado, no se puede eliminar el token FCM")
                return Result.failure(Exception("Usuario no autenticado"))
            }

            // Eliminar el token de Firestore
            firestore.collection(USERS_COLLECTION)
                .document(currentUser.uid)
                .update(FCM_TOKEN_FIELD, null)
                .await()

            // Eliminar el token del dispositivo
            messaging.deleteToken().await()

            Log.d(TAG, "Token FCM eliminado exitosamente")
            Result.success(Unit)

        } catch (e: Exception) {
            Log.e(TAG, "Error al eliminar el token FCM", e)
            Result.failure(e)
        }
    }
}
