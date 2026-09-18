package com.example.energy.data.repository

import com.example.energy.data.remote.dto.BankAccountDto
import com.example.energy.domain.model.BankAccount
import com.example.energy.domain.repository.BankAccountRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.gotrue.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onStart
import java.util.UUID

/**
 * Concrete implementation of [BankAccountRepository] utilizing Supabase Postgrest client.
 *
 * Target table: `public.bank_accounts`
 * Columns: id UUID, user_id UUID, bank_name TEXT, account_number TEXT, card_type TEXT.
 */
class BankAccountRepositoryImpl(
    private val client: SupabaseClient,
    private val defaultUserId: String? = null
) : BankAccountRepository {

    private val _bankAccountsFlow = MutableStateFlow<List<BankAccount>>(emptyList())

    override fun getBankAccounts(): Flow<List<BankAccount>> {
        return _bankAccountsFlow.asStateFlow().onStart {
            refreshBankAccounts()
        }
    }

    /**
     * Synchronizes and updates the in-memory cache of bank accounts from Supabase.
     */
    suspend fun refreshBankAccounts(): Result<List<BankAccount>> {
        return try {
            val userId = getEffectiveUserId()
            val dtos = client.postgrest["bank_accounts"].select {
                if (!userId.isNullOrBlank()) {
                    filter {
                        eq("user_id", userId)
                    }
                }
            }.decodeList<BankAccountDto>()

            val domainAccounts = dtos.map { it.toDomain() }
            _bankAccountsFlow.value = domainAccounts
            Result.success(domainAccounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addBankAccount(
        bankName: String,
        accountNumber: String,
        cardType: String
    ): Result<Unit> {
        return try {
            val userId = getEffectiveUserId()
            val dto = BankAccountDto(
                id = UUID.randomUUID().toString(),
                userId = userId,
                bankName = bankName.trim(),
                accountNumber = accountNumber.trim(),
                cardType = cardType.trim()
            )

            // Insert into public.bank_accounts
            client.postgrest["bank_accounts"].insert(dto)

            // Attempt to refresh the list from the remote table
            val refreshResult = refreshBankAccounts()
            if (refreshResult.isFailure) {
                // Optimistically append the newly created domain model to the flow
                _bankAccountsFlow.value = _bankAccountsFlow.value + dto.toDomain()
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Resolves the current user ID, favoring defaultUserId or Supabase auth session.
     */
    private fun getEffectiveUserId(): String? {
        val uid = defaultUserId ?: client.auth.currentUserOrNull()?.id
        return if (uid.isNullOrBlank()) null else uid
    }
}
