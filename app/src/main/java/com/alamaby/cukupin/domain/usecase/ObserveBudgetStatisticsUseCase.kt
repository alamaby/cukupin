package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.calculator.BudgetCalculator
import com.alamaby.cukupin.domain.model.AdjustmentData
import com.alamaby.cukupin.domain.model.BudgetCalculationInput
import com.alamaby.cukupin.domain.model.BudgetSummary
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.ExpenseData
import com.alamaby.cukupin.domain.model.ExpenseTransaction
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

/** Satu titik pada grafik kumulatif. */
data class CumulativePoint(
    val date: LocalDate,
    /** Total pengeluaran kumulatif sampai tanggal ini. */
    val actualCumulative: Long,
    /** Jalur ideal kumulatif sampai tanggal ini. */
    val idealCumulative: Long
)

/** Agregat statistik untuk layar Statistik (FR-08). */
data class BudgetStatistics(
    val target: BudgetTarget,
    val summary: BudgetSummary,
    val todayExpense: Long,
    val perCategory: List<CategoryTotal>,
    val biggestDay: Pair<LocalDate, Long>?,
    val zeroExpenseDays: Int,
    val cumulative: List<CumulativePoint>,
    val recentTransactions: List<ExpenseTransaction>
)

data class CategoryTotal(
    val categoryId: String?,
    val total: Long
)

class ObserveBudgetStatisticsUseCase(
    private val targetRepository: BudgetTargetRepository,
    private val expenseRepository: ExpenseRepository,
    private val adjustmentRepository: FundAdjustmentRepository,
    private val dateProvider: DateProvider
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(): Flow<BudgetStatistics?> {
        val targetAndToday = combine(
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
                    buildStatistics(target, expenses, adjustments, today)
                }
            }
        }
    }

    private fun buildStatistics(
        target: BudgetTarget,
        expenses: List<ExpenseTransaction>,
        adjustments: List<com.alamaby.cukupin.domain.model.FundAdjustment>,
        today: LocalDate
    ): BudgetStatistics {
        val summary = BudgetCalculator.calculateBudget(
            BudgetCalculationInput(
                initialAmount = target.initialAmount,
                adjustments = adjustments.map { AdjustmentData(it.amount, it.type) },
                expenses = expenses.map { ExpenseData(it.amount, it.transactionDate) },
                startDate = target.startDate,
                endDate = target.endDate,
                currentDate = today
            )
        )

        val inPeriod = expenses.filter {
            !it.transactionDate.isBefore(target.startDate) &&
                !it.transactionDate.isAfter(target.endDate)
        }

        val todayExpense = inPeriod
            .filter { it.transactionDate == today }
            .sumOf { it.amount }

        val perCategory = inPeriod
            .groupBy { it.categoryId }
            .map { (categoryId, list) -> CategoryTotal(categoryId, list.sumOf { it.amount }) }
            .sortedByDescending { it.total }

        val byDay = inPeriod.groupBy { it.transactionDate }
            .mapValues { (_, list) -> list.sumOf { it.amount } }
        val biggestDay = byDay.maxByOrNull { it.value }?.let { it.key to it.value }

        // Hari tanpa pengeluaran: hari yang sudah lewat dalam periode tanpa catatan.
        val elapsedEnd = when {
            today.isBefore(target.startDate) -> null
            today.isAfter(target.endDate) -> target.endDate
            else -> today
        }
        val zeroExpenseDays = if (elapsedEnd == null) {
            0
        } else {
            var date = target.startDate
            var count = 0
            while (!date.isAfter(elapsedEnd)) {
                if ((byDay[date] ?: 0L) == 0L) count++
                date = date.plusDays(1)
            }
            count
        }

        // Grafik kumulatif: aktual vs jalur ideal per hari.
        val cumulative = buildList {
            var date = target.startDate
            var running = 0L
            val dayIndex = generateSequence(1) { it + 1 }.iterator()
            while (!date.isAfter(target.endDate)) {
                running += byDay[date] ?: 0L
                val ideal = if (summary.totalDays > 0) {
                    // Aman dari overflow: hitung via BigDecimal seperti kalkulator.
                    java.math.BigDecimal(summary.totalFund)
                        .multiply(java.math.BigDecimal(dayIndex.next()))
                        .divide(
                            java.math.BigDecimal(summary.totalDays),
                            0,
                            java.math.RoundingMode.FLOOR
                        ).toLong()
                } else 0L
                add(CumulativePoint(date, running, ideal))
                date = date.plusDays(1)
            }
        }

        return BudgetStatistics(
            target = target,
            summary = summary,
            todayExpense = todayExpense,
            perCategory = perCategory,
            biggestDay = biggestDay,
            zeroExpenseDays = zeroExpenseDays,
            cumulative = cumulative,
            recentTransactions = inPeriod.sortedByDescending { it.createdAt }.take(5)
        )
    }
}
