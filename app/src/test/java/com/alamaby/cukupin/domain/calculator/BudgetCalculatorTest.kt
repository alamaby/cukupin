package com.alamaby.cukupin.domain.calculator

import com.alamaby.cukupin.domain.model.AdjustmentData
import com.alamaby.cukupin.domain.model.BudgetCalculationInput
import com.alamaby.cukupin.domain.model.BudgetHealthStatus
import com.alamaby.cukupin.domain.model.ExpenseData
import com.alamaby.cukupin.domain.model.FundAdjustmentType
import com.alamaby.cukupin.domain.model.PredictionConfidence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/**
 * Skenario uji kalkulator sesuai PRD §56, ditambah kasus tepi.
 */
class BudgetCalculatorTest {

    private fun input(
        initialAmount: Long = 1_000_000L,
        expenses: List<ExpenseData> = emptyList(),
        adjustments: List<AdjustmentData> = emptyList(),
        startDate: LocalDate = LocalDate.of(2026, 10, 1),
        endDate: LocalDate = LocalDate.of(2026, 10, 10),
        currentDate: LocalDate = LocalDate.of(2026, 10, 3)
    ) = BudgetCalculationInput(
        initialAmount = initialAmount,
        adjustments = adjustments,
        expenses = expenses,
        startDate = startDate,
        endDate = endDate,
        currentDate = currentDate
    )

    // ---------- PRD §56: kasus normal ----------

    @Test
    fun `kasus normal - angka sesuai PRD`() {
        val summary = BudgetCalculator.calculateBudget(
            input(expenses = listOf(ExpenseData(200_000L, LocalDate.of(2026, 10, 2))))
        )

        assertEquals(10, summary.totalDays)
        assertEquals(3, summary.elapsedDays)
        assertEquals(8, summary.remainingDays)
        assertEquals(800_000L, summary.remainingBalance)
        assertEquals(100_000L, summary.initialDailyBudget)
        assertEquals(100_000L, summary.currentDailyBudget)
        assertEquals(300_000L, summary.idealExpenseToDate)
        assertEquals(100_000L, summary.differenceFromTarget)
        assertEquals(BudgetHealthStatus.SAFE, summary.healthStatus)
        assertEquals(PredictionConfidence.EARLY, summary.predictionConfidence)
    }

    // ---------- PRD §56: kasus terlalu cepat ----------

    @Test
    fun `kasus terlalu cepat - status AT_RISK`() {
        val summary = BudgetCalculator.calculateBudget(
            input(expenses = listOf(ExpenseData(450_000L, LocalDate.of(2026, 10, 2))))
        )

        assertEquals(550_000L, summary.remainingBalance)
        assertEquals(68_750L, summary.currentDailyBudget)
        assertEquals(300_000L, summary.idealExpenseToDate)
        assertEquals(-150_000L, summary.differenceFromTarget)
        assertEquals(BudgetHealthStatus.AT_RISK, summary.healthStatus)
    }

    // ---------- PRD §56: kasus saldo negatif ----------

    @Test
    fun `kasus saldo negatif - status DEPLETED`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                initialAmount = 100_000L,
                expenses = listOf(ExpenseData(125_000L, LocalDate.of(2026, 10, 2)))
            )
        )

        assertEquals(-25_000L, summary.remainingBalance)
        assertEquals(BudgetHealthStatus.DEPLETED, summary.healthStatus)
        assertNull(summary.currentDailyBudget)
    }

    // ---------- PRD §56: kasus belum dimulai ----------

    @Test
    fun `kasus belum dimulai - status NOT_STARTED`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                startDate = LocalDate.of(2026, 10, 10),
                endDate = LocalDate.of(2026, 10, 20),
                currentDate = LocalDate.of(2026, 10, 5)
            )
        )

        assertEquals(0, summary.elapsedDays)
        assertEquals(11, summary.remainingDays)
        assertEquals(BudgetHealthStatus.NOT_STARTED, summary.healthStatus)
        assertEquals(PredictionConfidence.UNAVAILABLE, summary.predictionConfidence)
    }

    // ---------- PRD §56: kasus setelah selesai ----------

    @Test
    fun `kasus setelah selesai dengan sisa - FINISHED_WITH_BALANCE`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                startDate = LocalDate.of(2026, 10, 1),
                endDate = LocalDate.of(2026, 10, 30),
                currentDate = LocalDate.of(2026, 10, 31),
                expenses = listOf(ExpenseData(950_000L, LocalDate.of(2026, 10, 15)))
            )
        )

        assertEquals(0, summary.remainingDays)
        assertEquals(50_000L, summary.remainingBalance)
        assertEquals(BudgetHealthStatus.FINISHED_WITH_BALANCE, summary.healthStatus)
        assertNull(summary.currentDailyBudget)
    }

    @Test
    fun `kasus setelah selesai melebihi dana - FINISHED_EXCEEDED`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                startDate = LocalDate.of(2026, 10, 1),
                endDate = LocalDate.of(2026, 10, 30),
                currentDate = LocalDate.of(2026, 11, 2),
                expenses = listOf(ExpenseData(1_100_000L, LocalDate.of(2026, 10, 15)))
            )
        )

        assertEquals(BudgetHealthStatus.FINISHED_EXCEEDED, summary.healthStatus)
    }

    // ---------- Kasus tepi ----------

    @Test
    fun `saldo tepat nol saat selesai tetap berhasil`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                startDate = LocalDate.of(2026, 10, 1),
                endDate = LocalDate.of(2026, 10, 30),
                currentDate = LocalDate.of(2026, 10, 31),
                expenses = listOf(ExpenseData(1_000_000L, LocalDate.of(2026, 10, 15)))
            )
        )

        assertEquals(0L, summary.remainingBalance)
        assertEquals(BudgetHealthStatus.FINISHED_WITH_BALANCE, summary.healthStatus)
    }

    @Test
    fun `pembagian dibulatkan ke bawah`() {
        // 100.000 / 3 hari = 33.333 (bukan 33.334)
        val summary = BudgetCalculator.calculateBudget(
            input(
                initialAmount = 100_000L,
                startDate = LocalDate.of(2026, 10, 1),
                endDate = LocalDate.of(2026, 10, 3),
                currentDate = LocalDate.of(2026, 10, 1)
            )
        )

        assertEquals(3, summary.totalDays)
        assertEquals(33_333L, summary.initialDailyBudget)
    }

    @Test
    fun `transaksi di luar periode tidak masuk kalkulasi`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                expenses = listOf(
                    ExpenseData(200_000L, LocalDate.of(2026, 10, 2)),
                    // Di luar periode 1–10 Okt: harus diabaikan.
                    ExpenseData(500_000L, LocalDate.of(2026, 9, 30)),
                    ExpenseData(500_000L, LocalDate.of(2026, 10, 11))
                )
            )
        )

        assertEquals(200_000L, summary.totalExpense)
        assertEquals(800_000L, summary.remainingBalance)
    }

    @Test
    fun `penyesuaian dana ADD dan SUBTRACT`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                adjustments = listOf(
                    AdjustmentData(200_000L, FundAdjustmentType.ADD),
                    AdjustmentData(50_000L, FundAdjustmentType.SUBTRACT)
                )
            )
        )

        assertEquals(1_150_000L, summary.totalFund)
        assertEquals(115_000L, summary.initialDailyBudget)
    }

    @Test
    fun `periode satu hari dihitung inklusif`() {
        val day = LocalDate.of(2026, 10, 5)
        val summary = BudgetCalculator.calculateBudget(
            input(startDate = day, endDate = day, currentDate = day)
        )

        assertEquals(1, summary.totalDays)
        assertEquals(1, summary.elapsedDays)
        assertEquals(1, summary.remainingDays)
    }

    @Test
    fun `keyakinan prediksi mengikuti jumlah hari data`() {
        // 3 hari berbeda -> DEVELOPING
        val developing = BudgetCalculator.calculateBudget(
            input(
                expenses = listOf(
                    ExpenseData(10_000L, LocalDate.of(2026, 10, 1)),
                    ExpenseData(10_000L, LocalDate.of(2026, 10, 2)),
                    ExpenseData(10_000L, LocalDate.of(2026, 10, 3))
                )
            )
        )
        assertEquals(PredictionConfidence.DEVELOPING, developing.predictionConfidence)

        // 7 hari berbeda -> ESTABLISHED
        val established = BudgetCalculator.calculateBudget(
            input(
                startDate = LocalDate.of(2026, 10, 1),
                endDate = LocalDate.of(2026, 10, 31),
                currentDate = LocalDate.of(2026, 10, 10),
                expenses = (1..7).map {
                    ExpenseData(10_000L, LocalDate.of(2026, 10, it))
                }
            )
        )
        assertEquals(PredictionConfidence.ESTABLISHED, established.predictionConfidence)

        // Tanpa pengeluaran -> UNAVAILABLE
        val unavailable = BudgetCalculator.calculateBudget(input())
        assertEquals(PredictionConfidence.UNAVAILABLE, unavailable.predictionConfidence)
    }

    @Test
    fun `nominal sangat besar tidak overflow`() {
        val summary = BudgetCalculator.calculateBudget(
            input(
                initialAmount = Long.MAX_VALUE / 4,
                startDate = LocalDate.of(2026, 1, 1),
                endDate = LocalDate.of(2026, 12, 31),
                currentDate = LocalDate.of(2026, 6, 15)
            )
        )

        // idealExpenseToDate = totalFund * elapsed / totalDays tanpa overflow.
        assertEquals(365, summary.totalDays)
        // Harus tetap bernilai wajar, bukan negatif akibat overflow.
        assertTrue(summary.idealExpenseToDate > 0)
        assertTrue(summary.initialDailyBudget > 0)
    }

    @Test
    fun `status ON_TRACK dan WATCH pada batas toleransi`() {
        // Tepat di jalur ideal -> ON_TRACK
        val onTrack = BudgetCalculator.calculateBudget(
            input(expenses = listOf(ExpenseData(300_000L, LocalDate.of(2026, 10, 2))))
        )
        assertEquals(BudgetHealthStatus.ON_TRACK, onTrack.healthStatus)

        // Sedikit di atas ideal (dalam 5% toleransi = 50rb) -> WATCH
        val watch = BudgetCalculator.calculateBudget(
            input(expenses = listOf(ExpenseData(320_000L, LocalDate.of(2026, 10, 2))))
        )
        assertEquals(BudgetHealthStatus.WATCH, watch.healthStatus)
    }
}
