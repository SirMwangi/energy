package com.example.energy.domain.repository

import com.example.energy.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUserProfile(userId: String): Flow<User?>
    suspend fun syncUserProfile(userId: String): Result<User>
    suspend fun updateUserProfile(user: User): Result<Unit>
}
