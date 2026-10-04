package com.alamaby.cukupin.ui.navigation

/**
 * Rute navigasi aplikasi.
 *
 * Penting: setiap konstanta di sini wajib punya pasangan `composable(...)` di
 * CukupinNavGraph. Rute tanpa destination akan membuat NavHost gagal saat
 * runtime, jadi jangan menambah konstanta tanpa mendaftarkannya.
 */
object Routes {
    const val ONBOARDING = "onboarding"
    const val CREATE_TARGET = "create_target"
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add_expense"
    const val EDIT_EXPENSE = "edit_expense/{transactionId}"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val SUMMARY = "summary/{targetId}"
    const val ABOUT = "about"

    fun editExpense(transactionId: String) = "edit_expense/$transactionId"
    fun summary(targetId: String) = "summary/$targetId"
}
