package com.example.smartmoney.data.repository

import com.example.smartmoney.core.coroutine.DefaultDispatcherProvider
import com.example.smartmoney.core.coroutine.DispatcherProvider
import com.example.smartmoney.data.local.dao.AccountDao
import com.example.smartmoney.data.local.entity.AccountEntity
import com.example.smartmoney.data.remote.RetrofitClient
import com.example.smartmoney.data.remote.api.CreateAccountRequest
import com.example.smartmoney.data.remote.api.LinkBankRequest
import com.example.smartmoney.data.remote.datasource.AccountRemoteDataSource
import com.example.smartmoney.domain.model.Account
import com.example.smartmoney.domain.model.BankAccount
import com.example.smartmoney.domain.repository.BankAccountRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.util.UUID

/**
 * Concrete implementation of [BankAccountRepository] utilizing the Spring Boot accounts-service
 * and optional local Room cache [AccountDao].
 */
class BankAccountRepositoryImpl(
    private val remoteDataSource: AccountRemoteDataSource = AccountRemoteDataSource(),
    private val localDao: AccountDao? = null,
    private val userId: String,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : BankAccountRepository {

    private val _bankAccountsFlow = MutableStateFlow<List<BankAccount>>(emptyList())

    override fun getBankAccounts(): Flow<List<BankAccount>> {
        return if (localDao != null) {
            localDao.getAccountsForUser(userId).map { list ->
                list.map { it.toBankAccount() }
            }.flowOn(dispatchers.default).onStart {
                refreshBankAccounts()
            }
        } else {
            _bankAccountsFlow.asStateFlow().onStart {
                refreshBankAccounts()
            }
        }
    }

    /**
     * Synchronizes and updates bank accounts from the Spring Boot accounts-service.
     */
    suspend fun refreshBankAccounts(): Result<List<BankAccount>> = withContext(dispatchers.io) {
        try {
            val remoteAccounts = remoteDataSource.fetchAccounts(userId)
            val bankAccounts = remoteAccounts.map { it.toBankAccount() }
            _bankAccountsFlow.value = bankAccounts

            if (localDao != null) {
                val entities = remoteAccounts.map { AccountEntity.fromDomain(it) }
                localDao.upsertAccounts(entities)
            }

            Result.success(bankAccounts)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun addBankAccount(
        bankName: String,
        accountNumber: String,
        cardType: String
    ): Result<Unit> = withContext(dispatchers.io) {
        try {
            val effectiveUserId = try {
                UUID.fromString(userId).toString()
            } catch (_: Exception) {
                UUID.nameUUIDFromBytes(userId.toByteArray()).toString()
            }

            val trimmedNumber = accountNumber.trim()
            val masked = if (trimmedNumber.length > 4) {
                "**** " + trimmedNumber.takeLast(4)
            } else {
                "**** $trimmedNumber"
            }

            val accountType = if (cardType.trim().equals("Credit", ignoreCase = true)) {
                "CREDIT"
            } else {
                "DEPOSIT"
            }

            // 1. If KCB is selected, attempt linking via bank-integration-service (:8090)
            if (bankName.trim().equals("KCB", ignoreCase = true)) {
                try {
                    val linkReq = LinkBankRequest(
                        userId = effectiveUserId,
                        accountNumber = trimmedNumber,
                        cardType = cardType.trim(),
                        bankId = "kcb"
                    )
                    val response = RetrofitClient.bankIntegrationApi.linkKcbAccount(linkReq)
                    if (response.isSuccessful && response.body()?.success == true) {
                        val body = response.body()!!
                        val linkedAccount = BankAccount(
                            id = body.accountId,
                            bankName = body.institution,
                            accountNumber = body.accountNumber,
                            cardType = body.cardType
                        )
                        if (localDao != null) {
                            val entity = AccountEntity(
                                id = body.accountId,
                                userId = effectiveUserId,
                                accountId = body.accountNumber,
                                accountName = body.accountName,
                                institution = body.institution,
                                accountType = if (body.cardType.equals("Credit", ignoreCase = true)) "CREDIT" else "DEPOSIT",
                                maskedIdentifier = body.maskedIdentifier,
                                currency = "KES",
                                ledgerBalance = BigDecimal.ZERO,
                                availableBalance = BigDecimal.ZERO,
                                creditOutstanding = BigDecimal.ZERO,
                                creditLimit = BigDecimal.ZERO,
                                availableCredit = BigDecimal.ZERO,
                                accountStatus = "ACTIVE",
                                connectionStatus = "CONNECTED",
                                lastUpdated = null,
                                dataSource = "BANK_API"
                            )
                            localDao.upsertAccount(entity)
                        }
                        _bankAccountsFlow.value = _bankAccountsFlow.value + linkedAccount
                        return@withContext Result.success(Unit)
                    }
                } catch (_: Exception) {
                    // Fall back to direct accounts-service creation below if bank-integration-service is unreachable
                }
            }

            // 2. Direct accounts-service fallback (MANUAL)
            val request = CreateAccountRequest(
                userId = effectiveUserId,
                providerAccountId = trimmedNumber,
                accountName = "${bankName.trim()} ${cardType.trim()} Account",
                institution = bankName.trim(),
                accountType = accountType,
                maskedIdentifier = masked,
                currency = "KES",
                initialBalance = BigDecimal.ZERO,
                creditLimit = BigDecimal.ZERO,
                dataSource = "MANUAL"
            )

            val created = remoteDataSource.createAccount(request)

            if (localDao != null) {
                localDao.upsertAccount(AccountEntity.fromDomain(created))
            }

            val createdBankAccount = created.toBankAccount()
            _bankAccountsFlow.value = _bankAccountsFlow.value + createdBankAccount

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun removeBankAccount(id: String): Result<Unit> = withContext(dispatchers.io) {
        try {
            remoteDataSource.deleteAccount(id)
            if (localDao != null) {
                localDao.deleteAccountById(id)
            }
            _bankAccountsFlow.value = _bankAccountsFlow.value.filterNot { it.id == id || it.accountNumber == id }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Account.toBankAccount(): BankAccount = BankAccount(
        id = this.id,
        bankName = this.institution,
        accountNumber = this.accountId,
        cardType = if (this.accountType.equals("CREDIT", ignoreCase = true)) "Credit" else "Debit"
    )

    private fun AccountEntity.toBankAccount(): BankAccount = BankAccount(
        id = this.id,
        bankName = this.institution,
        accountNumber = this.accountId,
        cardType = if (this.accountType.equals("CREDIT", ignoreCase = true)) "Credit" else "Debit"
    )
}
