package com.example.energy.domain.repository

import com.example.energy.domain.model.Budget
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun getBudgets(): Flow<List<Budget>>
    suspend fun saveBudget(budget: Budget): Result<Unit>
    suspend fun deleteBudget(id: String): Result<Unit>
}
