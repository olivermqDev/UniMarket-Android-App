package com.atom.unimarket.data.service

import android.util.Log
import com.atom.unimarket.domain.service.NotificationService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class NotificationServiceImpl(
    private val firestore: FirebaseFirestore
) : NotificationService {

    override suspend fun sendOrderNotification(toUserId: String, title: String, message: String) {
        // In a real app, this would call a Cloud Function or your backend API
        // which would then send the FCM message to the user's device using their token.
        // For now, we'll log it and maybe save a notification document in Firestore.
        
        Log.d("NotificationService", "Sending to $toUserId: $title - $message")
        
        val notification = hashMapOf(
            "userId" to toUserId,
            "title" to title,
            "message" to message,
            "timestamp" to System.currentTimeMillis(),
            "read" to false
        )
        
        try {
            firestore.collection("notifications").add(notification).await()
        } catch (e: Exception) {
            Log.e("NotificationService", "Error saving notification", e)
        }
    }
}
