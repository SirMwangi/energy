package com.example.energy.ui.transactions

import com.example.energy.domain.model.Transaction

sealed interface TransactionUiState {
    object Loading : TransactionUiState
    data class Success(val transactions: List<Transaction>) : TransactionUiState
    data class Error(val message: String) : TransactionUiState
}
