package com.alamaby.cukupin.ui.screens.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.domain.model.ExpenseCategory
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.usecase.BudgetStatistics
import com.alamaby.cukupin.domain.usecase.ObserveBudgetStatisticsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class StatisticsUiState(
    val isLoading: Boolean = true,
    val stats: BudgetStatistics? = null,
    val categories: Map<String, ExpenseCategory> = emptyMap()
)

class StatisticsViewModel(
    observeBudgetStatistics: ObserveBudgetStatisticsUseCase,
    categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<StatisticsUiState> = combine(
        observeBudgetStatistics(),
        categoryRepository.observeActiveCategories()
    ) { stats, categories ->
        StatisticsUiState(
            isLoading = false,
            stats = stats,
            categories = categories.associateBy { it.id }
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        StatisticsUiState()
    )
}
