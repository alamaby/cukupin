package com.alamaby.cukupin.di

import android.content.Context
import com.alamaby.cukupin.data.prefs.OnboardingPreferences
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import com.alamaby.cukupin.domain.repository.FundAdjustmentRepository
import com.alamaby.cukupin.domain.time.DateProvider
import com.alamaby.cukupin.domain.time.SystemDateProvider
import com.alamaby.cukupin.domain.usecase.AddExpenseUseCase
import com.alamaby.cukupin.domain.usecase.CompleteBudgetTargetUseCase
import com.alamaby.cukupin.domain.usecase.CreateBudgetTargetUseCase
import com.alamaby.cukupin.domain.usecase.DeleteExpenseUseCase
import com.alamaby.cukupin.domain.usecase.ObserveActiveBudgetSummaryUseCase
import com.alamaby.cukupin.domain.usecase.ObserveBudgetStatisticsUseCase
import com.alamaby.cukupin.domain.usecase.ObserveExpenseHistoryUseCase
import com.alamaby.cukupin.domain.usecase.RepeatBudgetTargetUseCase
import com.alamaby.cukupin.domain.usecase.UpdateExpenseUseCase

/**
 * Manual dependency injection (tanpa Hilt) agar proyek tetap ringan.
 * Semua dependensi dibuat sekali di [CukupinApplication].
 */
class AppContainer(
    val context: Context,
    val targetRepository: BudgetTargetRepository,
    val expenseRepository: ExpenseRepository,
    val categoryRepository: CategoryRepository,
    val adjustmentRepository: FundAdjustmentRepository,
    val onboardingPreferences: OnboardingPreferences,
    val dateProvider: DateProvider = SystemDateProvider()
) {
    val createBudgetTarget = CreateBudgetTargetUseCase(targetRepository, dateProvider)
    val observeActiveBudgetSummary = ObserveActiveBudgetSummaryUseCase(
        targetRepository, expenseRepository, adjustmentRepository, dateProvider
    )
    val addExpense = AddExpenseUseCase(expenseRepository, targetRepository)
    val updateExpense = UpdateExpenseUseCase(expenseRepository, targetRepository)
    val deleteExpense = DeleteExpenseUseCase(expenseRepository)
    val observeExpenseHistory = ObserveExpenseHistoryUseCase(expenseRepository)
    val observeBudgetStatistics = ObserveBudgetStatisticsUseCase(
        targetRepository, expenseRepository, adjustmentRepository, dateProvider
    )
    val completeBudgetTarget = CompleteBudgetTargetUseCase(targetRepository)
    val repeatBudgetTarget = RepeatBudgetTargetUseCase(
        targetRepository, dateProvider, createBudgetTarget
    )
}
