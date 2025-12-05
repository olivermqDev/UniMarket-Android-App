package com.atom.unimarket.presentation.data.repository

import com.atom.unimarket.presentation.data.User
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

interface UserRepository {
    fun getUserProfile(uid: String): Flow<User>
    suspend fun saveUserProfile(uid: String, updates: Map<String, Any>)
}

class UserRepositoryImpl(
    private val firestore: FirebaseFirestore
) : UserRepository {

    override fun getUserProfile(uid: String): Flow<User> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    if (user != null) {
                        trySend(user)
                    }
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun saveUserProfile(uid: String, updates: Map<String, Any>) {
        firestore.collection("users").document(uid).update(updates).await()
    }
}
