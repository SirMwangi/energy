package com.example.energy.domain.repository

import com.example.energy.domain.model.Account
import kotlinx.coroutines.flow.Flow

interface AccountRepository {
    fun getAccountsFlow(userId: String): Flow<List<Account>>
    suspend fun syncAccounts(userId: String): Result<Unit>
    suspend fun createAccount(account: Account): Result<Account>
}
