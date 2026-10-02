package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

/** Mengubah transaksi (FR-06); statistik dihitung ulang otomatis via Flow. */
class UpdateExpenseUseCase(
    private val expenseRepository: ExpenseRepository,
    private val targetRepository: BudgetTargetRepository
) {
    sealed interface Result {
        data class Success(val transaction: ExpenseTransaction) : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(
        transactionId: String,
        amount: Long,
        categoryId: String?,
        date: LocalDate,
        note: String?
    ): Result {
        if (amount <= 0) return Result.Error("Nominal harus lebih besar dari nol")

        val existing = expenseRepository.getById(transactionId)
            ?: return Result.Error("Transaksi tidak ditemukan")
        val target = targetRepository.getTarget(existing.targetId)
            ?: return Result.Error("Target tidak ditemukan")

        if (date.isBefore(target.startDate) || date.isAfter(target.endDate)) {
            val fmt = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("id", "ID"))
            return Result.Error(
                "Tanggal transaksi berada di luar periode target " +
                    "${target.startDate.format(fmt)}–${target.endDate.format(fmt)}."
            )
        }

        val updated = existing.copy(
            amount = amount,
            transactionDate = date,
            categoryId = categoryId,
            note = note?.trim().orEmpty().ifEmpty { null },
            updatedAt = Instant.now()
        )
        expenseRepository.update(updated)
        return Result.Success(updated)
    }
}
