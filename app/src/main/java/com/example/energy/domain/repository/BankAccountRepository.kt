package com.example.energy.domain.repository

import com.example.energy.domain.model.BankAccount
import kotlinx.coroutines.flow.Flow

/**
 * Domain repository interface for managing linked bank accounts.
 */
interface BankAccountRepository {

    /**
     * Adds a new bank account to the remote backend.
     *
     * @param bankName Name of the bank (e.g., "NCBA", "Equity").
     * @param accountNumber The account or card number.
     * @param cardType Card type classification ("Debit", "Credit").
     * @return [Result.success] if insertion succeeded, or [Result.failure] with error details.
     */
    suspend fun addBankAccount(
        bankName: String,
        accountNumber: String,
        cardType: String
    ): Result<Unit>

    /**
     * Observes the list of linked bank accounts.
     *
     * @return A cold/hot [Flow] emitting the latest list of [BankAccount] models.
     */
    fun getBankAccounts(): Flow<List<BankAccount>>
}
