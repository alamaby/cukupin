package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.TargetLifecycleStatus
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import com.alamaby.cukupin.domain.time.DateProvider
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.util.UUID

/**
 * Membuat target dana baru (FR-02).
 * Hanya satu target aktif: target aktif lama diarsipkan lebih dulu.
 */
class CreateBudgetTargetUseCase(
    private val repository: BudgetTargetRepository,
    private val dateProvider: DateProvider
) {
    sealed interface Result {
        data class Success(val target: BudgetTarget) : Result
        data class Error(val message: String) : Result
    }

    suspend operator fun invoke(
        name: String,
        initialAmount: Long,
        startDate: LocalDate,
        endDate: LocalDate
    ): Result {
        if (name.isBlank()) return Result.Error("Nama target tidak boleh kosong")
        if (initialAmount <= 0) return Result.Error("Dana awal harus lebih besar dari nol")
        if (endDate.isBefore(startDate)) {
            return Result.Error("Tanggal selesai tidak boleh sebelum tanggal mulai")
        }

        // Arsipkan target aktif sebelumnya agar hanya satu yang aktif.
        val previous = repository.observeActiveTarget().first()
        if (previous != null) repository.archive(previous.id)

        val now = Instant.now()
        val today = dateProvider.today()
        val status = if (startDate.isAfter(today)) {
            TargetLifecycleStatus.UPCOMING
        } else {
            TargetLifecycleStatus.ACTIVE
        }
        val target = BudgetTarget(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            initialAmount = initialAmount,
            startDate = startDate,
            endDate = endDate,
            lifecycleStatus = status,
            createdAt = now,
            updatedAt = now
        )
        repository.create(target)
        return Result.Success(target)
    }
}
