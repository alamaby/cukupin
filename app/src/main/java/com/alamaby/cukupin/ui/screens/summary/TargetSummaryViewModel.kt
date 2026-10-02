package com.alamaby.cukupin.ui.screens.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.domain.calculator.BudgetCalculator
import com.alamaby.cukupin.domain.model.AdjustmentData
import com.alamaby.cukupin.domain.model.BudgetCalculationInput
import com.alamaby.cukupin.domain.model.BudgetSummary
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.ExpenseData
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import com.alamaby.cukupin.domain.usecase.RepeatBudgetTargetUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class TargetSummaryUiState(
    val isLoading: Boolean = true,
    val target: BudgetTarget? = null,
    /** Ringkasan final dihitung pada tanggal selesai. */
    val summary: BudgetSummary? = null,
    val biggestCategoryName: String? = null,
    val biggestCategoryTotal: Long = 0L,
    val biggestDay: Pair<LocalDate, Long>? = null,
    val zeroExpenseDays: Int = 0,
    val isRepeating: Boolean = false,
    val repeated: Boolean = false,
    val error: String? = null
) {
    /** Berhasil bila saldo akhir >= 0 (saldo nol tetap berhasil, FR-10). */
    val isSuccess: Boolean get() = (summary?.remainingBalance ?: Long.MIN_VALUE) >= 0
}

class TargetSummaryViewModel(
    private val targetId: String,
    targetRepository: BudgetTargetRepository,
    expenseRepository: ExpenseRepository,
    categoryRepository: CategoryRepository,
    private val repeatBudgetTarget: RepeatBudgetTargetUseCase
) : ViewModel() {

    private val repeatState = MutableStateFlow(Triple(false, false, null as String?))

    val uiState: StateFlow<TargetSummaryUiState> = combine(
        targetRepository.observeTarget(targetId),
        expenseRepository.observeByTarget(targetId),
        categoryRepository.observeActiveCategories(),
        repeatState
    ) { target, expenses, categories, (isRepeating, repeated, error) ->
        if (target == null) {
            TargetSummaryUiState(isLoading = false)
        } else {
            val names = categories.associate { it.id to it.name }
            val inPeriod = expenses.filter {
                !it.transactionDate.isBefore(target.startDate) &&
                    !it.transactionDate.isAfter(target.endDate)
            }
            // Status final: hitung seolah hari ini = tanggal selesai.
            val summary = BudgetCalculator.calculateBudget(
                BudgetCalculationInput(
                    initialAmount = target.initialAmount,
                    adjustments = emptyList<AdjustmentData>(),
                    expenses = inPeriod.map { ExpenseData(it.amount, it.transactionDate) },
                    startDate = target.startDate,
                    endDate = target.endDate,
                    currentDate = target.endDate
                )
            )
            val byCategory = inPeriod.groupBy { it.categoryId }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
            val biggestCategory = byCategory.maxByOrNull { it.value }
            val byDay = inPeriod.groupBy { it.transactionDate }
                .mapValues { (_, list) -> list.sumOf { it.amount } }
            val biggestDay = byDay.maxByOrNull { it.value }?.let { it.key to it.value }

            var date = target.startDate
            var zeroDays = 0
            while (!date.isAfter(target.endDate)) {
                if ((byDay[date] ?: 0L) == 0L) zeroDays++
                date = date.plusDays(1)
            }

            TargetSummaryUiState(
                isLoading = false,
                target = target,
                summary = summary,
                biggestCategoryName = biggestCategory?.key?.let { names[it] } ?: "–",
                biggestCategoryTotal = biggestCategory?.value ?: 0L,
                biggestDay = biggestDay,
                zeroExpenseDays = zeroDays,
                isRepeating = isRepeating,
                repeated = repeated,
                error = error
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        TargetSummaryUiState()
    )

    fun repeatTarget() {
        viewModelScope.launch {
            repeatState.value = Triple(true, false, null)
            when (val result = repeatBudgetTarget(targetId)) {
                is RepeatBudgetTargetUseCase.Result.Success ->
                    repeatState.value = Triple(false, true, null)
                is RepeatBudgetTargetUseCase.Result.Error ->
                    repeatState.value = Triple(false, false, result.message)
            }
        }
    }
}
