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

    /**
     * Container dibangun malas, bukan di [onCreate].
     *
     * `db.xDao()` memverifikasi kelas hasil generate Room saat dipanggil, dan
     * setiap verifikasi bisa memakan ratusan milidetik di perangkat lambat atau
     * emulator. Kalau itu terjadi di [onCreate], proses belum selesai startup
     * ketika sistem sudah kehabisan waktu dan menandai proses ANR, lalu dibunuh
     * sebelum layar pertama sempat tampil. Dengan `by lazy`, biaya itu baru
     * dibayar saat pertama kali benar-benar dibutuhkan.
     */
    val container: AppContainer by lazy {
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "cukupin.db"
        ).build()

        AppContainer(
            context = applicationContext,
            targetRepository = BudgetTargetRepositoryImpl(db.budgetTargetDao()),
            expenseRepository = ExpenseRepositoryImpl(db.expenseDao()),
            categoryRepository = CategoryRepositoryImpl(db.categoryDao()),
            adjustmentRepository = FundAdjustmentRepositoryImpl(db.fundAdjustmentDao()),
            onboardingPreferences = OnboardingPreferences(applicationContext)
        )
    }
}
