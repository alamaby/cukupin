package com.alamaby.cukupin.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.data.prefs.OnboardingPreferences
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Menentukan layar awal: onboarding (sekali saja) -> buat target
 * (bila belum ada target aktif) -> dashboard.
 */
class StartupViewModel(
    private val onboardingPreferences: OnboardingPreferences,
    private val targetRepository: BudgetTargetRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    init {
        viewModelScope.launch {
            // Pastikan kategori default tersedia sejak awal.
            categoryRepository.seedDefaults()

            val onboardingDone = onboardingPreferences.isCompleted.first()
            if (!onboardingDone) {
                _startDestination.value = Routes.ONBOARDING
                return@launch
            }
            val active = targetRepository.observeActiveTarget().first()
            _startDestination.value = if (active == null) {
                Routes.CREATE_TARGET
            } else {
                Routes.DASHBOARD
            }
        }
    }
}
