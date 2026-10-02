package com.alamaby.cukupin.domain.model

import java.time.LocalDate

/** Ringkasan data penyesuaian dana untuk kalkulator (tanpa ketergantungan Room). */
data class AdjustmentData(
    val amount: Long,
    val type: FundAdjustmentType
)

/** Ringkasan data pengeluaran untuk kalkulator (tanpa ketergantungan Room). */
data class ExpenseData(
    val amount: Long,
    val transactionDate: LocalDate
)

/**
 * Masukan kalkulator anggaran. Murni data — kalkulator tidak membaca
 * waktu sistem, Room, atau sumber eksternal lainnya.
 */
data class BudgetCalculationInput(
    val initialAmount: Long,
    val adjustments: List<AdjustmentData>,
    val expenses: List<ExpenseData>,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val currentDate: LocalDate
)
