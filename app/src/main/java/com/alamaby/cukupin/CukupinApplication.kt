package com.alamaby.cukupin

import android.app.Application
import androidx.room.Room
import com.alamaby.cukupin.data.local.AppDatabase
import com.alamaby.cukupin.data.prefs.OnboardingPreferences
import com.alamaby.cukupin.data.repository.BudgetTargetRepositoryImpl
import com.alamaby.cukupin.data.repository.CategoryRepositoryImpl
import com.alamaby.cukupin.data.repository.ExpenseRepositoryImpl
import com.alamaby.cukupin.data.repository.FundAdjustmentRepositoryImpl
import com.alamaby.cukupin.di.AppContainer

class CukupinApplication : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cukupin.db"
        ).build()

        val targetRepo = BudgetTargetRepositoryImpl(db.budgetTargetDao())
        val expenseRepo = ExpenseRepositoryImpl(db.expenseDao())
        val categoryRepo = CategoryRepositoryImpl(db.categoryDao())
        val adjustmentRepo = FundAdjustmentRepositoryImpl(db.fundAdjustmentDao())

        container = AppContainer(
            context = applicationContext,
            targetRepository = targetRepo,
            expenseRepository = expenseRepo,
            categoryRepository = categoryRepo,
            adjustmentRepository = adjustmentRepo,
            onboardingPreferences = OnboardingPreferences(applicationContext)
        )
    }
}
