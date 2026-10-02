package com.alamaby.cukupin.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.onboardingDataStore by preferencesDataStore(name = "onboarding")

/**
 * Menyimpan status onboarding (ditampilkan hanya sekali, PRD FR-01).
 */
class OnboardingPreferences(private val context: Context) {

    private val completedKey = booleanPreferencesKey("onboarding_completed")

    val isCompleted: Flow<Boolean> =
        context.onboardingDataStore.data.map { it[completedKey] == true }

    suspend fun setCompleted() {
        context.onboardingDataStore.edit { it[completedKey] = true }
    }
}
