package com.alamaby.cukupin.ui.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.usecase.DeleteExpenseUseCase
import com.alamaby.cukupin.domain.usecase.ExpenseDayGroup
import com.alamaby.cukupin.domain.usecase.ObserveExpenseHistoryUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class HistoryUiState(
    val isLoading: Boolean = true,
    val hasTarget: Boolean = false,
    val groups: List<ExpenseDayGroup> = emptyList(),
    val categoryNames: Map<String, String> = emptyMap()
)

class HistoryViewModel(
    targetRepository: BudgetTargetRepository,
    observeExpenseHistory: ObserveExpenseHistoryUseCase,
    private val deleteExpense: DeleteExpenseUseCase,
    categoryRepository: CategoryRepository
) : ViewModel() {

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> =
        combine(
            targetRepository.observeActiveTarget(),
            categoryRepository.observeActiveCategories()
        ) { target, categories ->
            target to categories.associate { it.id to it.name }
        }.flatMapLatest { (target, names) ->
            if (target == null) {
                flowOf(HistoryUiState(isLoading = false, hasTarget = false))
            } else {
                observeExpenseHistory(target.id).combine(
                    flowOf(names)
                ) { groups, n ->
                    HistoryUiState(
                        isLoading = false,
                        hasTarget = true,
                        groups = groups,
                        categoryNames = n
                    )
                }
            }
        }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            HistoryUiState()
        )

    private val _lastDeleted = MutableStateFlow<ExpenseTransaction?>(null)

    /** Menghapus dan mengembalikan data untuk snackbar "Urungkan". */
    fun delete(transactionId: String, onDeleted: (String) -> Unit) {
        viewModelScope.launch {
            val deleted = deleteExpense(transactionId)
            if (deleted != null) {
                _lastDeleted.value = deleted
                onDeleted("Transaksi dihapus")
            }
        }
    }

    fun undoDelete() {
        val tx = _lastDeleted.value ?: return
        _lastDeleted.value = null
        viewModelScope.launch {
            deleteExpense.undo(tx)
        }
    }
}
