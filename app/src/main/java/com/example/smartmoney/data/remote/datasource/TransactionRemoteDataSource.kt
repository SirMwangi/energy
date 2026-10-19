package com.example.smartmoney.data.remote.datasource

import com.example.smartmoney.data.remote.dto.TransactionDto
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest

class TransactionRemoteDataSource(private val client: SupabaseClient) {

    /**
     * Fetches transactions strictly bound to the specified [accountIds].
     * Never executes unbounded selects to prevent data leaks from accounts with no relation to the user.
     */
    suspend fun fetchTransactions(accountIds: List<String>): List<TransactionDto> {
        val validIds = accountIds.filter { it.isNotBlank() }
        if (validIds.isEmpty()) return emptyList()

        return client.postgrest["transactions"].select {
            filter {
                isIn("account_id", validIds)
            }
        }.decodeList<TransactionDto>()
    }

    suspend fun fetchTransactions(accountId: String? = null): List<TransactionDto> {
        if (accountId.isNullOrBlank()) return emptyList()
        return fetchTransactions(listOf(accountId))
    }

    suspend fun recordTransaction(transaction: TransactionDto): TransactionDto {
        return client.postgrest["transactions"].insert(transaction) {
            select()
        }.decodeSingle<TransactionDto>()
    }
}
