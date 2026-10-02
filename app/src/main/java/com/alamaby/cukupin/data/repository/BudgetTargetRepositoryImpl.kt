package com.alamaby.cukupin.data.repository

import com.alamaby.cukupin.data.local.dao.BudgetTargetDao
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.TargetLifecycleStatus
import com.alamaby.cukupin.domain.repository.BudgetTargetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant

class BudgetTargetRepositoryImpl(
    private val dao: BudgetTargetDao
) : BudgetTargetRepository {

    override fun observeActiveTarget(): Flow<BudgetTarget?> =
        dao.observeActiveTarget().map { it?.toDomain() }

    override fun observeTarget(id: String): Flow<BudgetTarget?> =
        dao.observeTarget(id).map { it?.toDomain() }

    override fun observeLatestTarget(): Flow<BudgetTarget?> =
        dao.observeAll().map { list -> list.firstOrNull()?.toDomain() }

    override suspend fun getTarget(id: String): BudgetTarget? =
        dao.getById(id)?.toDomain()

    override suspend fun create(target: BudgetTarget) {
        dao.upsert(target.toEntity())
    }

    override suspend fun update(target: BudgetTarget) {
        dao.update(target.copy(updatedAt = Instant.now()).toEntity())
    }

    override suspend fun complete(id: String) {
        dao.updateStatus(id, TargetLifecycleStatus.COMPLETED.name, Instant.now().toEpochMilli())
    }

    override suspend fun archive(id: String) {
        dao.updateStatus(id, TargetLifecycleStatus.ARCHIVED.name, Instant.now().toEpochMilli())
    }
}
