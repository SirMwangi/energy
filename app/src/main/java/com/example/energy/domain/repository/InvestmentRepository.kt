package com.example.energy.domain.repository

import com.example.energy.domain.model.Investment
import kotlinx.coroutines.flow.Flow

interface InvestmentRepository {
    fun getInvestments(): Flow<List<Investment>>
    suspend fun saveInvestment(investment: Investment): Result<Unit>
    suspend fun deleteInvestment(id: String): Result<Unit>
}
