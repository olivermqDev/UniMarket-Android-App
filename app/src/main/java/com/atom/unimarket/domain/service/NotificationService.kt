package com.atom.unimarket.domain.service

interface NotificationService {
    suspend fun sendOrderNotification(toUserId: String, title: String, message: String)
}
