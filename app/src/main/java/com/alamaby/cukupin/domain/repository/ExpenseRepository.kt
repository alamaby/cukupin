package com.alamaby.cukupin.domain.repository

import com.alamaby.cukupin.domain.model.ExpenseTransaction
import kotlinx.coroutines.flow.Flow

interface ExpenseRepository {
    fun observeByTarget(targetId: String): Flow<List<ExpenseTransaction>>
    suspend fun getById(transactionId: String): ExpenseTransaction?
    suspend fun create(transaction: ExpenseTransaction)
    suspend fun update(transaction: ExpenseTransaction)
    suspend fun delete(transactionId: String)
}
