package com.example.smartmoney.ui.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.smartmoney.core.coroutine.DefaultDispatcherProvider
import com.example.smartmoney.core.coroutine.DispatcherProvider
import com.example.smartmoney.domain.model.Transaction
import com.example.smartmoney.domain.repository.TransactionRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class TransactionViewModel(
    private val repository: TransactionRepository? = null,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider(),
    externalScope: CoroutineScope? = null
) : ViewModel() {

    private val scope = externalScope ?: viewModelScope

    companion object {
        /**
         * 10 realistic energy and utility transactions populated for testing and prototype preview.
         */
        val SAMPLE_TRANSACTIONS = listOf(
            Transaction(
                id = "txn_001",
                accountId = "ACC-1001",
                amount = BigDecimal("2500.00"),
                type = "CREDIT",
                timestamp = "2026-09-09T10:15:00Z",
                description = "Prepaid Token Purchase via M-Pesa"
            ),
            Transaction(
                id = "txn_002",
                accountId = "ACC-1001",
                amount = BigDecimal("185.40"),
                type = "DEBIT",
                timestamp = "2026-09-08T23:59:00Z",
                description = "Daily Smart Meter Consumption"
            ),
            Transaction(
                id = "txn_003",
                accountId = "ACC-1001",
                amount = BigDecimal("350.00"),
                type = "DEBIT",
                timestamp = "2026-09-07T08:00:00Z",
                description = "Monthly Fixed Grid Standing Charge"
            ),
            Transaction(
                id = "txn_004",
                accountId = "ACC-1001",
                amount = BigDecimal("5000.00"),
                type = "CREDIT",
                timestamp = "2026-09-05T14:20:00Z",
                description = "Direct Bank Top-up (Equity Bank)"
            ),
            Transaction(
                id = "txn_005",
                accountId = "ACC-1001",
                amount = BigDecimal("1420.75"),
                type = "DEBIT",
                timestamp = "2026-09-04T12:30:00Z",
                description = "KCB Auto-Debit Utility Settlement"
            ),
            Transaction(
                id = "txn_006",
                accountId = "ACC-1001",
                amount = BigDecimal("850.00"),
                type = "CREDIT",
                timestamp = "2026-09-03T16:45:00Z",
                description = "NCBA Loop Energy Bill Payment"
            ),
            Transaction(
                id = "txn_007",
                accountId = "ACC-1001",
                amount = BigDecimal("1200.00"),
                type = "DEBIT",
                timestamp = "2026-09-02T19:10:00Z",
                description = "Stanbic Bank Standing Order"
            ),
            Transaction(
                id = "txn_008",
                accountId = "ACC-1001",
                amount = BigDecimal("550.00"),
                type = "DEBIT",
                timestamp = "2026-09-02T19:12:00Z",
                description = "Emergency Token Auto-Recovery Fee"
            ),
            Transaction(
                id = "txn_009",
                accountId = "ACC-1001",
                amount = BigDecimal("210.30"),
                type = "DEBIT",
                timestamp = "2026-09-01T21:00:00Z",
                description = "Peak Hour Surcharge Adjustment"
            ),
            Transaction(
                id = "txn_010",
                accountId = "ACC-1001",
                amount = BigDecimal("320.00"),
                type = "CREDIT",
                timestamp = "2026-08-30T11:05:00Z",
                description = "Utility Overcharge Fuel Cost Rebate"
            )
        )
    }

    private val _errorState = MutableStateFlow<String?>(null)
    private val _selectedFilter = MutableStateFlow("ALL")
    val selectedFilter: StateFlow<String> = _selectedFilter.asStateFlow()

    // Reactive StateFlow with WhileSubscribed(5_000) to stop background database observation when inactive
    val uiState: StateFlow<TransactionUiState> = if (repository != null) {
        combine(
            repository.getTransactionsFlow(),
            _errorState
        ) { cachedTransactions, errorMessage ->
            println("DEBUG_COMBINE_BLOCK: cachedTransactions=${cachedTransactions.size}, error=$errorMessage")
            withContext(dispatchers.default) {
                if (errorMessage != null && cachedTransactions.isEmpty()) {
                    TransactionUiState.Error(errorMessage)
                } else if (cachedTransactions.isNotEmpty()) {
                    TransactionUiState.Success(cachedTransactions)
                } else {
                    TransactionUiState.Success(SAMPLE_TRANSACTIONS)
                }
            }
        }
            .catch { e ->
                println("DEBUG_EXCEPTION: $e")
                e.printStackTrace()
                emit(TransactionUiState.Success(SAMPLE_TRANSACTIONS))
            }
            .stateIn(
                scope = scope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = TransactionUiState.Success(SAMPLE_TRANSACTIONS)
            )
    } else {
        MutableStateFlow(TransactionUiState.Success(SAMPLE_TRANSACTIONS)).asStateFlow()
    }

    /**
     * Filtered transactions computed off the Main Thread on [dispatchers.default]
     * to ensure solid 60 FPS performance without in-composition list processing.
     */
    val filteredTransactions: StateFlow<List<Transaction>> = combine(
        uiState,
        _selectedFilter
    ) { state, filter ->
        withContext(dispatchers.default) {
            val transactions = when (state) {
                is TransactionUiState.Success -> state.transactions
                else -> emptyList()
            }
            when (filter) {
                "CREDIT" -> transactions.filter { it.type.equals("CREDIT", ignoreCase = true) }
                "DEBIT" -> transactions.filter { it.type.equals("DEBIT", ignoreCase = true) }
                else -> transactions
            }
        }
    }.stateIn(
        scope = scope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SAMPLE_TRANSACTIONS
    )

    fun setFilter(filter: String) {
        _selectedFilter.value = filter
    }

    fun refreshTransactions() {
        if (repository != null) {
            scope.launch(dispatchers.main) {
                val result = repository.syncTransactions()
                result.onFailure { error ->
                    _errorState.value = error.localizedMessage ?: "Failed to sync transactions"
                }
                result.onSuccess {
                    _errorState.value = null
                }
            }
        }
    }

    class Factory(
        private val repository: TransactionRepository? = null,
        private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TransactionViewModel(repository, dispatchers) as T
        }
    }
}
