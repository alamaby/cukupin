package com.alamaby.cukupin.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alamaby.cukupin.data.prefs.OnboardingPreferences
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val prefs: OnboardingPreferences
) : ViewModel() {

    fun complete() {
        viewModelScope.launch { prefs.setCompleted() }
    }
}
