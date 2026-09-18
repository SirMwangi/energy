package com.example.energy.domain.repository

import com.example.energy.domain.model.AccountConnection
import kotlinx.coroutines.flow.Flow

interface AccountConnectionRepository {
    fun getConnectionsFlow(accountId: String): Flow<List<AccountConnection>>
    suspend fun syncConnections(accountId: String): Result<Unit>
}
