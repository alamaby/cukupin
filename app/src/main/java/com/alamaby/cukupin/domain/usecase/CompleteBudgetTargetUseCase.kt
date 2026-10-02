package com.alamaby.cukupin.domain.usecase

import com.alamaby.cukupin.domain.repository.BudgetTargetRepository

/** Menyelesaikan target saat periode berakhir (FR-10). */
class CompleteBudgetTargetUseCase(
    private val repository: BudgetTargetRepository
) {
    suspend operator fun invoke(targetId: String) {
        repository.complete(targetId)
    }
}
