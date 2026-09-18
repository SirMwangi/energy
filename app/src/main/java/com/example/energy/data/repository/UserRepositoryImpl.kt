package com.example.energy.data.repository

import com.example.energy.data.local.dao.UserDao
import com.example.energy.data.local.entity.UserEntity
import com.example.energy.data.remote.datasource.UserRemoteDataSource
import com.example.energy.data.remote.dto.UserDto
import com.example.energy.domain.model.User
import com.example.energy.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val remoteDataSource: UserRemoteDataSource,
    private val localDao: UserDao
) : UserRepository {

    override fun getUserProfile(userId: String): Flow<User?> {
        return localDao.getUser(userId).map { it?.toDomain() }
    }

    override suspend fun syncUserProfile(userId: String): Result<User> {
        return try {
            val remoteUserDto = remoteDataSource.fetchUserProfile(userId)
                ?: return Result.failure(NoSuchElementException("User profile not found"))
            val domainUser = remoteUserDto.toDomain()
            localDao.upsertUser(UserEntity.fromDomain(domainUser))
            Result.success(domainUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateUserProfile(user: User): Result<Unit> {
        return try {
            remoteDataSource.updateUserProfile(UserDto.fromDomain(user))
            localDao.upsertUser(UserEntity.fromDomain(user))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
