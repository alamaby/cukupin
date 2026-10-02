package com.alamaby.cukupin.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.alamaby.cukupin.di.AppContainer
import com.alamaby.cukupin.di.viewModelFactory
import com.alamaby.cukupin.ui.screens.addexpense.AddExpenseScreen
import com.alamaby.cukupin.ui.screens.addexpense.AddExpenseViewModel
import com.alamaby.cukupin.ui.screens.createtarget.CreateTargetScreen
import com.alamaby.cukupin.ui.screens.createtarget.CreateTargetViewModel
import com.alamaby.cukupin.ui.screens.dashboard.DashboardScreen
import com.alamaby.cukupin.ui.screens.dashboard.DashboardViewModel
import com.alamaby.cukupin.ui.screens.history.HistoryScreen
import com.alamaby.cukupin.ui.screens.history.HistoryViewModel
import com.alamaby.cukupin.ui.screens.onboarding.OnboardingScreen
import com.alamaby.cukupin.ui.screens.onboarding.OnboardingViewModel
import com.alamaby.cukupin.ui.screens.statistics.StatisticsScreen
import com.alamaby.cukupin.ui.screens.statistics.StatisticsViewModel
import com.alamaby.cukupin.ui.screens.summary.TargetSummaryScreen
import com.alamaby.cukupin.ui.screens.summary.TargetSummaryViewModel

@Composable
fun CukupinApp(container: AppContainer, startDestination: String) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.ONBOARDING) {
            val vm: OnboardingViewModel = viewModel(
                factory = viewModelFactory {
                    OnboardingViewModel(container.onboardingPreferences)
                }
            )
            OnboardingScreen(
                onFinish = {
                    vm.complete()
                    navController.navigate(Routes.CREATE_TARGET) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.CREATE_TARGET) {
            val vm: CreateTargetViewModel = viewModel(
                factory = viewModelFactory {
                    CreateTargetViewModel(container.createBudgetTarget)
                }
            )
            CreateTargetScreen(
                viewModel = vm,
                onTargetCreated = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.CREATE_TARGET) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.DASHBOARD) {
            val vm: DashboardViewModel = viewModel(
                factory = viewModelFactory {
                    DashboardViewModel(
                        observeActiveBudgetSummary = container.observeActiveBudgetSummary,
                        expenseRepository = container.expenseRepository,
                        targetRepository = container.targetRepository,
                        completeBudgetTarget = container.completeBudgetTarget,
                        categoryRepository = container.categoryRepository,
                        dateProvider = container.dateProvider
                    )
                }
            )
            DashboardScreen(
                viewModel = vm,
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onOpenHistory = { navController.navigate(Routes.HISTORY) },
                onOpenStatistics = { navController.navigate(Routes.STATISTICS) },
                onCreateTarget = {
                    navController.navigate(Routes.CREATE_TARGET) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onTargetCompleted = { targetId ->
                    navController.navigate(Routes.summary(targetId)) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                },
                onOpenSummary = { targetId ->
                    navController.navigate(Routes.summary(targetId))
                }
            )
        }

        composable(Routes.ADD_EXPENSE) {
            val vm: AddExpenseViewModel = viewModel(
                factory = viewModelFactory {
                    AddExpenseViewModel(
                        targetRepository = container.targetRepository,
                        categoryRepository = container.categoryRepository,
                        addExpense = container.addExpense,
                        updateExpense = container.updateExpense,
                        expenseRepository = container.expenseRepository,
                        dateProvider = container.dateProvider,
                        transactionId = null
                    )
                }
            )
            AddExpenseScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.EDIT_EXPENSE,
            arguments = listOf(navArgument("transactionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val transactionId = backStackEntry.arguments?.getString("transactionId")
            val vm: AddExpenseViewModel = viewModel(
                key = "edit_$transactionId",
                factory = viewModelFactory {
                    AddExpenseViewModel(
                        targetRepository = container.targetRepository,
                        categoryRepository = container.categoryRepository,
                        addExpense = container.addExpense,
                        updateExpense = container.updateExpense,
                        expenseRepository = container.expenseRepository,
                        dateProvider = container.dateProvider,
                        transactionId = transactionId
                    )
                }
            )
            AddExpenseScreen(
                viewModel = vm,
                onDone = { navController.popBackStack() },
                onCancel = { navController.popBackStack() }
            )
        }

        composable(Routes.HISTORY) {
            val vm: HistoryViewModel = viewModel(
                factory = viewModelFactory {
                    HistoryViewModel(
                        targetRepository = container.targetRepository,
                        observeExpenseHistory = container.observeExpenseHistory,
                        deleteExpense = container.deleteExpense,
                        categoryRepository = container.categoryRepository
                    )
                }
            )
            HistoryScreen(
                viewModel = vm,
                onEdit = { id -> navController.navigate(Routes.editExpense(id)) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.STATISTICS) {
            val vm: StatisticsViewModel = viewModel(
                factory = viewModelFactory {
                    StatisticsViewModel(
                        observeBudgetStatistics = container.observeBudgetStatistics,
                        categoryRepository = container.categoryRepository
                    )
                }
            )
            StatisticsScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Routes.SUMMARY,
            arguments = listOf(navArgument("targetId") { type = NavType.StringType })
        ) { backStackEntry ->
            val targetId = backStackEntry.arguments?.getString("targetId").orEmpty()
            val vm: TargetSummaryViewModel = viewModel(
                key = "summary_$targetId",
                factory = viewModelFactory {
                    TargetSummaryViewModel(
                        targetId = targetId,
                        targetRepository = container.targetRepository,
                        expenseRepository = container.expenseRepository,
                        categoryRepository = container.categoryRepository,
                        repeatBudgetTarget = container.repeatBudgetTarget
                    )
                }
            )
            TargetSummaryScreen(
                viewModel = vm,
                onRepeatDone = {
                    navController.navigate(Routes.DASHBOARD) {
                        popUpTo(Routes.DASHBOARD) { inclusive = true }
                    }
                }
            )
        }
    }
}
