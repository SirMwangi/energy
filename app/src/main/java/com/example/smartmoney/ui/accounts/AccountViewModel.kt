package com.example.smartmoney.ui.accounts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartmoney.data.repository.BankAccountRepositoryImpl
import com.example.smartmoney.domain.model.Account
import com.example.smartmoney.domain.model.BankAccount
import com.example.smartmoney.domain.repository.AccountRepository
import com.example.smartmoney.domain.repository.BankAccountRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class AccountViewModel(
    private val repository: AccountRepository,
    private val bankAccountRepository: BankAccountRepository,
    private val userId: String
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isAddingBankAccount = MutableStateFlow(false)
    val isAddingBankAccount: StateFlow<Boolean> = _isAddingBankAccount.asStateFlow()

    private val _bankAccountError = MutableStateFlow<String?>(null)
    val bankAccountError: StateFlow<String?> = _bankAccountError.asStateFlow()

    val accounts: StateFlow<List<Account>> = repository.getAccountsFlow(userId)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val bankAccounts: StateFlow<List<BankAccount>> = bankAccountRepository.getBankAccounts()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val totalBalance: StateFlow<BigDecimal> = accounts.map { list ->
        withContext(Dispatchers.Default) {
            list.fold(BigDecimal.ZERO) { acc, account -> acc.add(account.availableBalance) }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BigDecimal.ZERO
    )

    init {
        refreshAccounts()
    }

    fun refreshAccounts() {
        if (userId.isNotBlank()) {
            viewModelScope.launch {
                _isLoading.value = true
                repository.syncAccounts(userId)
                _isLoading.value = false
            }
        }
    }

    /**
     * Adds a new bank account via [BankAccountRepository] and handles success/error UI states.
     */
    fun addBankAccount(
        bankName: String,
        accountNumber: String,
        cardType: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isAddingBankAccount.value = true
            _bankAccountError.value = null

            val result = bankAccountRepository.addBankAccount(
                bankName = bankName,
                accountNumber = accountNumber,
                cardType = cardType
            )

            _isAddingBankAccount.value = false

            result.onSuccess {
                _bankAccountError.value = null
                onSuccess?.invoke()
            }.onFailure { error ->
                val message = error.localizedMessage ?: "Failed to link bank account"
                _bankAccountError.value = message
                onError?.invoke(message)
            }
        }
    }

    fun clearBankAccountError() {
        _bankAccountError.value = null
    }

    /**
     * Removes an account permanently from the remote backend and local database.
     */
    fun removeAccount(
        accountId: String,
        onSuccess: (() -> Unit)? = null,
        onError: ((String) -> Unit)? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true
            val result = repository.deleteAccount(accountId)
            bankAccountRepository.removeBankAccount(accountId)
            _isLoading.value = false

            result.onSuccess {
                onSuccess?.invoke()
            }.onFailure { error ->
                onError?.invoke(error.localizedMessage ?: "Failed to remove account")
            }
        }
    }

    class Factory(
        private val repository: AccountRepository,
        private val bankAccountRepository: BankAccountRepository,
        private val userId: String
    ) : ViewModelProvider.Factory {

        constructor(
            repository: AccountRepository,
            userId: String
        ) : this(
            repository = repository,
            bankAccountRepository = BankAccountRepositoryImpl(
                userId = userId
            ),
            userId = userId
        )

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AccountViewModel(repository, bankAccountRepository, userId) as T
        }
    }
}
