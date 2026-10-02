package com.alamaby.cukupin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alamaby.cukupin.di.AppContainer
import com.alamaby.cukupin.di.viewModelFactory
import com.alamaby.cukupin.ui.navigation.CukupinApp
import com.alamaby.cukupin.ui.navigation.StartupViewModel
import com.alamaby.cukupin.ui.theme.CukupinTheme

class MainActivity : ComponentActivity() {

    private val container: AppContainer
        get() = (application as CukupinApplication).container

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CukupinTheme {
                val startupViewModel: StartupViewModel = viewModel(
                    factory = viewModelFactory {
                        StartupViewModel(
                            onboardingPreferences = container.onboardingPreferences,
                            targetRepository = container.targetRepository,
                            categoryRepository = container.categoryRepository
                        )
                    }
                )
                val startDestination by startupViewModel.startDestination.collectAsState()
                startDestination?.let { CukupinApp(container, it) }
            }
        }
    }
}
