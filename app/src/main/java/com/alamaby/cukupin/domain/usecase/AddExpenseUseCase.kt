package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

/**
 * Menambah pengeluaran (FR-04). Menolak tanggal di luar periode target
 * sesuai PRD §50 — pengguna harus mengubah tanggal atau periode dulu.
 */
class AddExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val targetRepository: BudgetTargetRepository
) {
    sealed interface Result {
        data class Success(val transaction: ExpenseTransaction) : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(
        targetId: String,
        amount: Long,
        categoryId: String?,
        date: LocalDate,
        note: String?
    ): Result {
        if (amount <= 0) return Result.Error("Nominal harus lebih besar dari nol")

        val target = targetRepository.getTarget(targetId)
            ?: return Result.Error("Target tidak ditemukan")

        if (date.isBefore(target.startDate) || date.isAfter(target.endDate)) {
            val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", java.util.Locale("id", "ID"))
            return Result.Error(
                "Tanggal transaksi berada di luar periode target " +
                    "${target.startDate.format(fmt)}–${target.endDate.format(fmt)}. " +
                    "Ubah tanggal transaksi atau sesuaikan periode target."
            )
        }

        val now = Instant.now()
        val transaction = ExpenseTransaction(
            id = UUID.randomUUID().toString(),
            targetId = targetId,
            amount = amount,
            transactionDate = date,
            categoryId = categoryId,
            note = note?.trim().orEmpty().ifEmpty { null },
            createdAt = now,
            updatedAt = now
        )
        expenseRepository.create(transaction)
        return Result.Success(transaction)
    }
}
