package com.alamaby.cukupin.domain.calculator

import com.alamaby.cukupin.domain.model.BudgetCalculationInput
import com.alamaby.cukupin.domain.model.BudgetHealthStatus
import com.alamaby.cukupin.domain.model.BudgetSummary
import com.alamaby.cukupin.domain.model.ExpenseData
import com.alamaby.cukupin.domain.model.FundAdjustmentType
import com.alamaby.cukupin.domain.model.PredictionConfidence
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.floor

/**
 * Kalkulator anggaran yang murni dan deterministik.
 *
 * - Tidak mengakses Room, waktu sistem, atau format mata uang.
 * - Tidak menghasilkan teks UI.
 * - Semua tanggal keuangan bersifat inklusif.
 * - Uang selalu Long; Float/Double tidak pernah dipakai untuk menyimpan uang.
 * - Perkalian nominal × jumlah hari memakai BigDecimal agar aman dari overflow.
 */
object BudgetCalculator {

    fun calculateBudget(input: BudgetCalculationInput): BudgetSummary {
        require(input.initialAmount > 0) { "initialAmount harus lebih besar dari nol" }
        require(!input.endDate.isBefore(input.startDate)) {
            "endDate tidak boleh sebelum startDate"
        }

        val totalDays = daysInclusive(input.startDate, input.endDate)

        val elapsedDays = when {
            input.currentDate.isBefore(input.startDate) -> 0
            input.currentDate.isAfter(input.endDate) -> totalDays
            else -> daysInclusive(input.startDate, input.currentDate)
        }

        val remainingDays = when {
            input.currentDate.isBefore(input.startDate) -> totalDays
            input.currentDate.isAfter(input.endDate) -> 0
            else -> daysInclusive(input.currentDate, input.endDate)
        }

        // Total penyesuaian dana (aman dari overflow via BigDecimal).
        val adjustmentTotal = input.adjustments.fold(BigDecimal.ZERO) { acc, adj ->
            val signed = when (adj.type) {
                FundAdjustmentType.ADD -> BigDecimal(adj.amount)
                FundAdjustmentType.SUBTRACT -> BigDecimal(adj.amount).negate()
            }
            acc + signed
        }

        val totalFund = (BigDecimal(input.initialAmount) + adjustmentTotal)
            .toLongSaturated()

        // Hanya transaksi di dalam periode yang masuk kalkulasi (PRD §50:
        // transaksi luar periode tidak disimpan diam-diam).
        val validExpenses = input.expenses.filter {
            !it.transactionDate.isBefore(input.startDate) &&
                !it.transactionDate.isAfter(input.endDate)
        }

        val totalExpense = validExpenses
            .fold(BigDecimal.ZERO) { acc, e -> acc + BigDecimal(e.amount) }
            .toLongSaturated()

        val remainingBalance = BigDecimal(totalFund)
            .subtract(BigDecimal(totalExpense))
            .toLongSaturated()

        // Batas awal: totalFund / totalDays, dibulatkan ke bawah.
        val initialDailyBudget =
            if (totalDays > 0 && totalFund > 0) {
                totalFund / totalDays
            } else {
                0L
            }

        // Batas aman terbaru: remainingBalance / remainingDays, atau null.
        val currentDailyBudget =
            if (remainingDays > 0 && remainingBalance > 0) {
                remainingBalance / remainingDays
            } else {
                null
            }

        // Pengeluaran ideal kumulatif sampai hari ini (jalur lurus),
        // dihitung aman dari overflow lalu dibulatkan ke bawah.
        val idealExpenseToDate =
            if (totalDays > 0 && elapsedDays > 0) {
                BigDecimal(totalFund)
                    .multiply(BigDecimal(elapsedDays))
                    .divide(BigDecimal(totalDays), 0, RoundingMode.FLOOR)
                    .toLongSaturated()
            } else {
                0L
            }

        val differenceFromTarget = idealExpenseToDate - totalExpense

        val averageDailyExpense =
            if (elapsedDays > 0 && totalExpense > 0) {
                totalExpense / elapsedDays
            } else {
                null
            }

        val estimatedRemainingDays =
            if (averageDailyExpense != null &&
                averageDailyExpense > 0 &&
                remainingBalance > 0
            ) {
                remainingBalance.toDouble() / averageDailyExpense.toDouble()
            } else {
                null
            }

        val estimatedRunOutDate = estimatedRemainingDays?.let {
            input.currentDate.plusDays(floor(it).toLong())
        }

        val confidence = determinePredictionConfidence(
            expenses = validExpenses,
            startDate = input.startDate,
            currentDate = input.currentDate
        )

        val health = determineHealthStatus(
            currentDate = input.currentDate,
            startDate = input.startDate,
            endDate = input.endDate,
            totalFund = totalFund,
            totalExpense = totalExpense,
            idealExpense = idealExpenseToDate,
            remainingBalance = remainingBalance,
            estimatedRunOutDate = estimatedRunOutDate
        )

        return BudgetSummary(
            totalFund = totalFund,
            totalExpense = totalExpense,
            remainingBalance = remainingBalance,
            totalDays = totalDays,
            elapsedDays = elapsedDays,
            remainingDays = remainingDays,
            initialDailyBudget = initialDailyBudget,
            currentDailyBudget = currentDailyBudget,
            idealExpenseToDate = idealExpenseToDate,
            differenceFromTarget = differenceFromTarget,
            averageDailyExpense = averageDailyExpense,
            estimatedRemainingDays = estimatedRemainingDays,
            estimatedRunOutDate = estimatedRunOutDate,
            healthStatus = health,
            predictionConfidence = confidence
        )
    }

    /**
     * Menentukan status kesehatan dana.
     *
     * Aturan:
     * - sebelum periode → NOT_STARTED
     * - setelah periode → FINISHED_WITH_BALANCE (saldo >= 0) / FINISHED_EXCEEDED
     * - saldo habis/negatif saat periode berjalan → DEPLETED
     * - selain itu, bandingkan selisih terhadap jalur ideal dengan toleransi
     *   5% dari total dana (SAFE / ON_TRACK / WATCH / AT_RISK).
     *
     * [estimatedRunOutDate] disertakan sesuai kontrak PRD §49; ambang
     * selisih sudah mencerminkan risiko proyeksi sehingga status tetap
     * stabil dan dapat diuji (tidak flip-flop).
     */
    fun determineHealthStatus(
        currentDate: LocalDate,
        startDate: LocalDate,
        endDate: LocalDate,
        totalFund: Long,
        totalExpense: Long,
        idealExpense: Long,
        remainingBalance: Long,
        estimatedRunOutDate: LocalDate?
    ): BudgetHealthStatus {
        if (currentDate.isBefore(startDate)) return BudgetHealthStatus.NOT_STARTED
        if (currentDate.isAfter(endDate)) {
            return if (remainingBalance >= 0) {
                BudgetHealthStatus.FINISHED_WITH_BALANCE
            } else {
                BudgetHealthStatus.FINISHED_EXCEEDED
            }
        }
        if (remainingBalance <= 0) return BudgetHealthStatus.DEPLETED

        val differenceFromTarget = idealExpense - totalExpense
        val tolerance = maxOf(1L, totalFund / 20) // 5% dari total dana

        return when {
            differenceFromTarget >= tolerance -> BudgetHealthStatus.SAFE
            differenceFromTarget >= 0 -> BudgetHealthStatus.ON_TRACK
            differenceFromTarget >= -tolerance -> BudgetHealthStatus.WATCH
            else -> BudgetHealthStatus.AT_RISK
        }
    }

    /**
     * Menentukan keyakinan prediksi berdasarkan jumlah hari berbeda
     * yang memiliki catatan pengeluaran (bukan umur target).
     */
    fun determinePredictionConfidence(
        expenses: List<ExpenseData>,
        startDate: LocalDate,
        currentDate: LocalDate
    ): PredictionConfidence {
        if (currentDate.isBefore(startDate)) return PredictionConfidence.UNAVAILABLE

        val effectiveEnd = minOf(currentDate, expenses.maxOfOrNull { it.transactionDate } ?: currentDate)
        val distinctDays = expenses
            .asSequence()
            .map { it.transactionDate }
            .filter { !it.isBefore(startDate) && !it.isAfter(effectiveEnd) }
            .distinct()
            .count()

        return when {
            distinctDays <= 0 -> PredictionConfidence.UNAVAILABLE
            distinctDays <= 2 -> PredictionConfidence.EARLY
            distinctDays <= 6 -> PredictionConfidence.DEVELOPING
            else -> PredictionConfidence.ESTABLISHED
        }
    }

    /** Jumlah hari inklusif antara [start] dan [end] (keduanya dihitung). */
    fun daysInclusive(start: LocalDate, end: LocalDate): Int {
        require(!end.isBefore(start)) { "end tidak boleh sebelum start" }
        return ChronoUnit.DAYS.between(start, end).toInt() + 1
    }

    private fun BigDecimal.toLongSaturated(): Long =
        when {
            this > BigDecimal(Long.MAX_VALUE) -> Long.MAX_VALUE
            this < BigDecimal(Long.MIN_VALUE) -> Long.MIN_VALUE
            else -> this.longValueExact()
        }
}
