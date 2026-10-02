package com.alamaby.cukupin.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import com.alamaby.cukupin.domain.time.DateProvider
import com.alamaby.cukupin.domain.usecase.ActiveBudgetData
import com.alamaby.cukupin.domain.usecase.CompleteBudgetTargetUseCase
import com.alamaby.cukupin.domain.usecase.ObserveActiveBudgetSummaryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val isLoading: Boolean = true,
    /** Target aktif beserta ringkasannya; null bila tidak ada target aktif. */
    val active: ActiveBudgetData? = null,
    /** Target terbaru (untuk status selesai / kosong). */
    val latestTarget: BudgetTarget? = null,
    val todayExpense: Long = 0L,
    val recentTransactions: List<ExpenseTransaction> = emptyList(),
    val categoryNames: Map<String, String> = emptyMap()
)

class DashboardViewModel(
    observeActiveBudgetSummary: ObserveActiveBudgetSummaryUseCase,
    expenseRepository: ExpenseRepository,
    private val targetRepository: BudgetTargetRepository,
    private val completeBudgetTarget: CompleteBudgetTargetUseCase,
    categoryRepository: CategoryRepository,
    private val dateProvider: DateProvider
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<DashboardUiState> =
        combine(
            observeActiveBudgetSummary(),
            targetRepository.observeLatestTarget(),
            categoryRepository.observeActiveCategories()
        ) { active, latest, categories ->
            Triple(active, latest, categories.associate { it.id to it.name })
        }.flatMapLatest { (active, latest, names) ->
            if (active == null) {
                flowOf(
                    DashboardUiState(
                        isLoading = false,
                        active = null,
                        latestTarget = latest,
                        categoryNames = names
                    )
                )
            } else {
                expenseRepository.observeByTarget(active.target.id).map { expenses ->
                    val today = dateProvider.today()
                    DashboardUiState(
                        isLoading = false,
                        active = active,
                        latestTarget = latest,
                        todayExpense = expenses
                            .filter { it.transactionDate == today }
                            .sumOf { it.amount },
                        recentTransactions = expenses
                            .sortedByDescending { it.createdAt }
                            .take(5),
                        categoryNames = names
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            DashboardUiState()
        )

    /** True bila periode sudah lewat dan target masih aktif. */
    fun isPastDue(): Boolean {
        val target = uiState.value.active?.target ?: return false
        return dateProvider.today().isAfter(target.endDate)
    }

    fun completeTarget(onDone: (String) -> Unit) {
        val targetId = uiState.value.active?.target?.id ?: return
        viewModelScope.launch {
            completeBudgetTarget(targetId)
            onDone(targetId)
        }
    }
}
