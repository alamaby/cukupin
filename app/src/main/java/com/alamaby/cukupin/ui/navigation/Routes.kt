package com.alamaby.cukupin.ui.navigation

/** Rute navigasi aplikasi. */
object Routes {
    const val STARTUP = "startup"
    const val ONBOARDING = "onboarding"
    const val CREATE_TARGET = "create_target"
    const val DASHBOARD = "dashboard"
    const val ADD_EXPENSE = "add_expense"
    const val EDIT_EXPENSE = "edit_expense/{transactionId}"
    const val HISTORY = "history"
    const val STATISTICS = "statistics"
    const val SUMMARY = "summary/{targetId}"

    fun editExpense(transactionId: String) = "edit_expense/$transactionId"
    fun summary(targetId: String) = "summary/$targetId"
}
