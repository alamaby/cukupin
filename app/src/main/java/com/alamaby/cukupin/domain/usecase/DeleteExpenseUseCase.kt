package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.ExpenseRepository

/**
 * Menghapus transaksi (FR-07). Mengembalikan data transaksi agar UI
 * dapat menyediakan aksi "Urungkan" (sisipkan kembali).
 */
class DeleteExpenseUseCase(
    private val expenseRepository: ExpenseRepository
) {
    suspend operator fun invoke(transactionId: String): ExpenseTransaction? {
        val existing = expenseRepository.getById(transactionId) ?: return null
        expenseRepository.delete(transactionId)
        return existing
    }

    suspend fun undo(transaction: ExpenseTransaction) {
        expenseRepository.create(transaction)
    }
}
