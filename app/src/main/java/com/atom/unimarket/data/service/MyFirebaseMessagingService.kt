package com.atom.unimarket.data.service

import android.util.Log
import com.atom.unimarket.domain.repository.FCMRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

/**
 * Servicio que maneja los eventos de Firebase Cloud Messaging.
 * Se ejecuta en segundo plano cuando llegan notificaciones o cuando el token se actualiza.
 */
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Inyección de dependencias usando Koin
    private val fcmRepository: FCMRepository by inject()

    // CoroutineScope para operaciones asíncronas en el servicio
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "FCMService"
    }

    /**
     * Se llama cuando Firebase genera un nuevo token FCM.
     * Esto puede ocurrir cuando:
     * - La app se instala por primera vez
     * - El usuario desinstala/reinstala la app
     * - El usuario limpia los datos de la app
     * - Firebase rota el token por seguridad
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Nuevo token FCM recibido: $token")

        // Guardar el nuevo token en Firestore de forma asíncrona
        serviceScope.launch {
            val result = fcmRepository.saveFCMToken()
            result.onSuccess {
                Log.d(TAG, "Token actualizado exitosamente en Firestore")
            }.onFailure { error ->
                Log.e(TAG, "Error al actualizar token en Firestore", error)
            }
        }
    }

    /**
     * Se llama cuando llega un mensaje de notificación push mientras la app está en primer plano.
     * Si la app está en segundo plano, el sistema maneja la notificación automáticamente.
     */
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d(TAG, "Mensaje recibido de: ${message.from}")

        // Aquí puedes manejar la notificación personalizada
        message.notification?.let { notification ->
            Log.d(TAG, "Título: ${notification.title}")
            Log.d(TAG, "Cuerpo: ${notification.body}")
            
            // TODO: Mostrar notificación personalizada si la app está en primer plano
            // Puedes usar NotificationManager para crear y mostrar la notificación
        }

        // Manejar datos adicionales si los hay
        if (message.data.isNotEmpty()) {
            Log.d(TAG, "Datos del mensaje: ${message.data}")
            // TODO: Procesar datos personalizados (ej: navegar a una pantalla específica)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancelar todas las coroutines cuando el servicio se destruya
        serviceScope.cancel()
    }
}
