package com.alamaby.cukupin.domain.repository

import com.alamaby.cukupin.domain.model.BudgetTarget
import kotlinx.coroutines.flow.Flow

interface BudgetTargetRepository {
    fun observeActiveTarget(): Flow<BudgetTarget?>
    fun observeTarget(id: String): Flow<BudgetTarget?>
    /** Target terbaru apa pun statusnya (untuk ringkasan pasca-selesai). */
    fun observeLatestTarget(): Flow<BudgetTarget?>
    suspend fun getTarget(id: String): BudgetTarget?
    suspend fun create(target: BudgetTarget)
    suspend fun update(target: BudgetTarget)
    suspend fun complete(id: String)
    suspend fun archive(id: String)
}
