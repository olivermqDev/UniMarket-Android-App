package com.atom.unimarket.domain.repository

/**
 * Repositorio para gestionar el token FCM del dispositivo.
 * Este token es necesario para enviar notificaciones push al usuario.
 */
interface FCMRepository {
    /**
     * Obtiene el token FCM del dispositivo actual y lo guarda en Firestore
     * en el documento del usuario autenticado.
     * 
     * @return Result<String> con el token si fue exitoso, o un error si falló
     */
    suspend fun saveFCMToken(): Result<String>
    
    /**
     * Elimina el token FCM del usuario en Firestore (útil al cerrar sesión)
     */
    suspend fun deleteFCMToken(): Result<Unit>
}
