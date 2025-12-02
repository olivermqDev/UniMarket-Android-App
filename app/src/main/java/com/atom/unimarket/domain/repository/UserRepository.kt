package com.atom.unimarket.domain.repository

import com.atom.unimarket.presentation.data.User

interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
}
