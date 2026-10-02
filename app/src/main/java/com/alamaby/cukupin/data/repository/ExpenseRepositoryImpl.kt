package com.alamaby.cukupin.data.repository

import com.alamaby.cukupin.data.local.dao.ExpenseDao
import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ExpenseRepositoryImpl(
    private val dao: ExpenseDao
) : ExpenseRepository {

    override fun observeByTarget(targetId: String): Flow<List<ExpenseTransaction>> =
        dao.observeByTarget(targetId).map { list -> list.map { it.toDomain() } }

    override suspend fun getById(transactionId: String): ExpenseTransaction? =
        dao.getById(transactionId)?.toDomain()

    override suspend fun create(transaction: ExpenseTransaction) {
        dao.upsert(transaction.toEntity())
    }

    override suspend fun update(transaction: ExpenseTransaction) {
        dao.update(transaction.toEntity())
    }

    override suspend fun delete(transactionId: String) {
        dao.deleteById(transactionId)
    }
}
