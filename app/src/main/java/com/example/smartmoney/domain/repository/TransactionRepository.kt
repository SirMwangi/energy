package com.example.smartmoney.domain.repository

import com.example.smartmoney.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

interface TransactionRepository {
    fun getTransactionsFlow(accountId: String? = null): Flow<List<Transaction>>
    suspend fun syncTransactions(accountId: String? = null): Result<Unit>
    suspend fun recordTransaction(transaction: Transaction): Result<Transaction>
}
