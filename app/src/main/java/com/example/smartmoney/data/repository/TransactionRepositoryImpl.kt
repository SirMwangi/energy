package com.example.smartmoney.data.repository

import com.example.smartmoney.data.local.dao.TransactionDao
import com.example.smartmoney.data.local.entity.TransactionEntity
import com.example.smartmoney.data.remote.datasource.TransactionRemoteDataSource
import com.example.smartmoney.data.remote.dto.TransactionDto
import com.example.smartmoney.domain.model.Transaction
import com.example.smartmoney.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TransactionRepositoryImpl(
    private val remoteDataSource: TransactionRemoteDataSource,
    private val localDao: TransactionDao
) : TransactionRepository {

    override fun getTransactionsFlow(accountId: String?): Flow<List<Transaction>> {
        val flow = if (accountId != null) {
            localDao.getTransactionsForAccount(accountId)
        } else {
            localDao.getAllTransactions()
        }
        return flow.map { list -> list.map { it.toDomain() } }
    }

    override suspend fun syncTransactions(accountId: String?): Result<Unit> {
        return try {
            val remoteDtos = remoteDataSource.fetchTransactions(accountId)
            val domainTransactions = remoteDtos.map { it.toDomain() }
            val entities = domainTransactions.map { TransactionEntity.fromDomain(it) }
            localDao.upsertTransactions(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recordTransaction(transaction: Transaction): Result<Transaction> {
        return try {
            val remoteDto = remoteDataSource.recordTransaction(TransactionDto.fromDomain(transaction))
            val domainTx = remoteDto.toDomain()
            localDao.upsertTransaction(TransactionEntity.fromDomain(domainTx))
            Result.success(domainTx)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
