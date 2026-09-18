package com.example.energy.data.repository

import com.example.energy.data.local.dao.AccountDao
import com.example.energy.data.local.entity.AccountEntity
import com.example.energy.data.remote.datasource.AccountRemoteDataSource
import com.example.energy.data.remote.dto.AccountDto
import com.example.energy.domain.model.Account
import com.example.energy.domain.repository.AccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AccountRepositoryImpl(
    private val remoteDataSource: AccountRemoteDataSource,
    private val localDao: AccountDao
) : AccountRepository {

    override fun getAccountsFlow(userId: String): Flow<List<Account>> {
        return localDao.getAccountsForUser(userId).map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun syncAccounts(userId: String): Result<Unit> {
        return try {
            val remoteAccounts = remoteDataSource.fetchAccounts(userId)
            val domainAccounts = remoteAccounts.map { it.toDomain() }
            val entities = domainAccounts.map { AccountEntity.fromDomain(it) }
            localDao.upsertAccounts(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun createAccount(account: Account): Result<Account> {
        return try {
            val createdDto = remoteDataSource.createAccount(AccountDto.fromDomain(account))
            val domainAccount = createdDto.toDomain()
            localDao.upsertAccount(AccountEntity.fromDomain(domainAccount))
            Result.success(domainAccount)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
