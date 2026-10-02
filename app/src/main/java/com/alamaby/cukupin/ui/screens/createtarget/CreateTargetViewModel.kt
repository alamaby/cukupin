package com.alamaby.cukupin.ui.screens.createtarget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.domain.calculator.BudgetCalculator
import com.alamaby.cukupin.domain.usecase.CreateBudgetTargetUseCase
import com.alamaby.cukupin.domain.time.DateProvider
import com.alamaby.cukupin.domain.time.SystemDateProvider
import com.alamaby.cukupin.ui.util.parseRupiahInput
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate

data class CreateTargetUiState(
    val name: String = "",
    val amountText: String = "",
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(29),
    val error: String? = null,
    val isSaving: Boolean = false,
    val created: Boolean = false
) {
    val amount: Long? get() = parseRupiahInput(amountText)
    val totalDays: Int? get() = runCatching {
        BudgetCalculator.daysInclusive(startDate, endDate)
    }.getOrNull()
    val initialDailyBudget: Long? get() {
        val a = amount ?: return null
        val days = totalDays ?: return null
        if (a <= 0 || days <= 0) return null
        return a / days
    }
    val isValid: Boolean
        get() = name.isNotBlank() && (amount ?: 0L) > 0 && totalDays != null
}

class CreateTargetViewModel(
    private val createUseCase: CreateBudgetTargetUseCase,
    private val dateProvider: DateProvider = SystemDateProvider()
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        CreateTargetUiState(
            startDate = dateProvider.today(),
            endDate = dateProvider.today().plusDays(29)
        )
    )
    val uiState: StateFlow<CreateTargetUiState> = _uiState.asStateFlow()

    fun onNameChange(value: String) = _uiState.update {
        it.copy(name = value, error = null)
    }

    fun onAmountChange(value: String) {
        // Hanya digit yang disimpan; format tampilan diurus UI.
        val digits = value.filter { ch -> ch.isDigit() }
        _uiState.update { it.copy(amountText = digits, error = null) }
    }

    fun onStartDateChange(date: LocalDate) = _uiState.update {
        it.copy(startDate = date, error = null)
    }

    fun onEndDateChange(date: LocalDate) = _uiState.update {
        it.copy(endDate = date, error = null)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            when (val result = createUseCase(
                name = state.name,
                initialAmount = state.amount ?: 0L,
                startDate = state.startDate,
                endDate = state.endDate
            )) {
                is CreateBudgetTargetUseCase.Result.Success ->
                    _uiState.update { it.copy(isSaving = false, created = true) }
                is CreateBudgetTargetUseCase.Result.Error ->
                    _uiState.update { it.copy(isSaving = false, error = result.message) }
            }
        }
    }
}
