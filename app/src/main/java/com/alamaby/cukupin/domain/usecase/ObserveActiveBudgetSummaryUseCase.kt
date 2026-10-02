package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.calculator.BudgetCalculator
import com.alamaby.cukupin.domain.model.AdjustmentData
import com.alamaby.cukupin.domain.model.BudgetCalculationInput
import com.alamaby.cukupin.domain.model.BudgetSummary
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.ExpenseData
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import com.alamaby.cukupin.domain.repository.FundAdjustmentRepository
import com.alamaby.cukupin.domain.time.DateProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate

/**
 * Use case terpenting (PRD §52): menggabungkan target aktif, transaksi,
 * penyesuaian dana, dan tanggal saat ini menjadi ringkasan via BudgetCalculator.
 */
data class ActiveBudgetData(
    val target: BudgetTarget,
    val summary: BudgetSummary
)

class ObserveActiveBudgetSummaryUseCase(
    private val targetRepository: BudgetTargetRepository,
    private val expenseRepository: ExpenseRepository,
    private val adjustmentRepository: FundAdjustmentRepository,
    private val dateProvider: DateProvider
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<ActiveBudgetData?> {
        // Tanggal dibaca setiap kali flow dikumpulkan ulang, sehingga
        // Dashboard menghitung ulang saat dibuka / kembali foreground (PRD §53).
        val targetAndToday: Flow<Pair<BudgetTarget?, LocalDate>> = combine(
            targetRepository.observeActiveTarget(),
            flow { emit(dateProvider.today()) }
        ) { target, today -> target to today }

        return targetAndToday.flatMapLatest { (target, today) ->
            if (target == null) {
                flowOf(null)
            } else {
                combine(
                    expenseRepository.observeByTarget(target.id),
                    adjustmentRepository.observeByTarget(target.id)
                ) { expenses, adjustments ->
                    val summary = BudgetCalculator.calculateBudget(
                        BudgetCalculationInput(
                            initialAmount = target.initialAmount,
                            adjustments = adjustments.map {
                                AdjustmentData(it.amount, it.type)
                            },
                            expenses = expenses.map {
                                ExpenseData(it.amount, it.transactionDate)
                            },
                            startDate = target.startDate,
                            endDate = target.endDate,
                            currentDate = today
                        )
                    )
                    ActiveBudgetData(target, summary)
                }
            }
        }
    }
}
