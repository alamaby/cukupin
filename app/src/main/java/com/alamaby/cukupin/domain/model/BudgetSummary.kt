package com.alamaby.cukupin.domain.model

import java.time.LocalDate

/**
 * Hasil perhitungan anggaran. Seluruh nominal dalam Long (satuan mata uang
 * terkecil, mis. rupiah). Tidak ada Float/Double untuk uang, kecuali
 * [estimatedRemainingDays] yang memang bersifat estimasi statistik.
 */
data class BudgetSummary(
    val totalFund: Long,
    val totalExpense: Long,
    val remainingBalance: Long,

    val totalDays: Int,
    val elapsedDays: Int,
    val remainingDays: Int,

    /** totalFund / totalDays, dibulatkan ke bawah. */
    val initialDailyBudget: Long,

    /** remainingBalance / remainingDays, atau null bila tak tersedia. */
    val currentDailyBudget: Long?,

    /** Pengeluaran ideal kumulatif sampai hari ini menurut jalur lurus. */
    val idealExpenseToDate: Long,

    /** idealExpenseToDate - totalExpense. Positif = lebih hemat dari rencana. */
    val differenceFromTarget: Long,

    val averageDailyExpense: Long?,
    val estimatedRemainingDays: Double?,
    val estimatedRunOutDate: LocalDate?,

    val healthStatus: BudgetHealthStatus,
    val predictionConfidence: PredictionConfidence
)
