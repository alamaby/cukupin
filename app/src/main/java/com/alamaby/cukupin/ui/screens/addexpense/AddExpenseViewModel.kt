package com.alamaby.cukupin.ui.screens.addexpense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.data.repository.CategoryRepositoryImpl
import com.alamaby.cukupin.domain.model.ExpenseCategory
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import com.alamaby.cukupin.domain.repository.ExpenseRepository
import com.alamaby.cukupin.domain.time.DateProvider
import com.alamaby.cukupin.domain.usecase.AddExpenseUseCase
import com.alamaby.cukupin.domain.usecase.UpdateExpenseUseCase
import com.alamaby.cukupin.ui.util.parseRupiahInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class AddExpenseUiState(
    val amountText: String = "",
    val categoryId: String = CategoryRepositoryImpl.FALLBACK_CATEGORY_ID,
    val date: LocalDate = LocalDate.now(),
    val note: String = "",
    val categories: List<ExpenseCategory> = emptyList(),
    val error: String? = null,
    val isSaving: Boolean = false,
    val done: Boolean = false,
    val isEdit: Boolean = false
) {
    val amount: Long? get() = parseRupiahInput(amountText)
    val isValid: Boolean get() = (amount ?: 0L) > 0
}

class AddExpenseViewModel(
    private val targetRepository: BudgetTargetRepository,
    categoryRepository: CategoryRepository,
    private val addExpense: AddExpenseUseCase,
    private val updateExpense: UpdateExpenseUseCase,
    private val expenseRepository: ExpenseRepository,
    private val dateProvider: DateProvider,
    private val transactionId: String?
) : ViewModel() {

    private val form = MutableStateFlow(
        AddExpenseUiState(date = dateProvider.today(), isEdit = transactionId != null)
    )

    val uiState: StateFlow<AddExpenseUiState> = combine(
        form,
        categoryRepository.observeActiveCategories()
    ) { state, categories ->
        val validCategory = if (categories.any { it.id == state.categoryId }) {
            state.categoryId
        } else {
            categories.firstOrNull()?.id
                ?: CategoryRepositoryImpl.FALLBACK_CATEGORY_ID
        }
        state.copy(categories = categories, categoryId = validCategory)
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        AddExpenseUiState(isEdit = transactionId != null)
    )

    init {
        if (transactionId != null) {
            viewModelScope.launch {
                expenseRepository.getById(transactionId)?.let { tx ->
                    form.value = form.value.copy(
                        amountText = tx.amount.toString(),
                        categoryId = tx.categoryId
                            ?: CategoryRepositoryImpl.FALLBACK_CATEGORY_ID,
                        date = tx.transactionDate,
                        note = tx.note.orEmpty()
                    )
                }
            }
        }
    }

    fun onAmountChange(value: String) {
        form.value = form.value.copy(
            amountText = value.filter { it.isDigit() },
            error = null
        )
    }

    fun onCategoryChange(id: String) {
        form.value = form.value.copy(categoryId = id)
    }

    fun onDateChange(date: LocalDate) {
        form.value = form.value.copy(date = date, error = null)
    }

    fun onNoteChange(value: String) {
        form.value = form.value.copy(note = value)
    }

    fun save() {
        val state = uiState.value
        viewModelScope.launch {
            form.value = form.value.copy(isSaving = true, error = null)
            if (transactionId == null) {
                val target = targetRepository.observeActiveTarget().first()
                if (target == null) {
                    form.value = form.value.copy(
                        isSaving = false,
                        error = "Tidak ada target aktif."
                    )
                    return@launch
                }
                when (val result = addExpense(
                    targetId = target.id,
                    amount = state.amount ?: 0L,
                    categoryId = state.categoryId,
                    date = state.date,
                    note = state.note
                )) {
                    is AddExpenseUseCase.Result.Success ->
                        form.value = form.value.copy(isSaving = false, done = true)
                    is AddExpenseUseCase.Result.Error ->
                        form.value = form.value.copy(isSaving = false, error = result.message)
                }
            } else {
                when (val result = updateExpense(
                    transactionId = transactionId,
                    amount = state.amount ?: 0L,
                    categoryId = state.categoryId,
                    date = state.date,
                    note = state.note
                )) {
                    is UpdateExpenseUseCase.Result.Success ->
                        form.value = form.value.copy(isSaving = false, done = true)
                    is UpdateExpenseUseCase.Result.Error ->
                        form.value = form.value.copy(isSaving = false, error = result.message)
                }
            }
        }
    }
}
