package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.calculator.BudgetCalculator
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.time.DateProvider

/**
 * Membuat target baru berdasarkan target sebelumnya (FR-11).
 * Menyalin nama, dana awal, dan panjang periode. Transaksi tidak disalin.
 */
class RepeatBudgetTargetUseCase(
    private val repository: BudgetTargetRepository,
    private val dateProvider: DateProvider,
    private val createUseCase: CreateBudgetTargetUseCase
) {
    sealed interface Result {
        data class Success(val target: BudgetTarget) : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(sourceTargetId: String): Result {
        val source = repository.getTarget(sourceTargetId)
            ?: return Result.Error("Target sebelumnya tidak ditemukan")

        val periodDays = BudgetCalculator.daysInclusive(source.startDate, source.endDate)
        val newStart = dateProvider.today()
        val newEnd = newStart.plusDays(periodDays.toLong() - 1)

        return when (val result = createUseCase(
            name = source.name,
            initialAmount = source.initialAmount,
            startDate = newStart,
            endDate = newEnd
        )) {
            is CreateBudgetTargetUseCase.Result.Success -> Result.Success(result.target)
            is CreateBudgetTargetUseCase.Result.Error -> Result.Error(result.message)
        }
    }
}
