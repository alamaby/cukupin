package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate

/** Satu kelompok riwayat: semua transaksi pada tanggal yang sama. */
data class ExpenseDayGroup(
    val date: LocalDate,
    val total: Long,
    val transactions: List<ExpenseTransaction>
)

/** Riwayat transaksi dikelompokkan per tanggal, terbaru dulu (FR-05). */
class ObserveExpenseHistoryUseCase(
    private val expenseRepository: ExpenseRepository
) {
    operator fun invoke(targetId: String): Flow<List<ExpenseDayGroup>> =
        expenseRepository.observeByTarget(targetId).map { transactions ->
            transactions
                .groupBy { it.transactionDate }
                .map { (date, list) ->
                    ExpenseDayGroup(
                        date = date,
                        total = list.sumOf { it.amount },
                        transactions = list
                    )
                }
                .sortedByDescending { it.date }
        }
}
