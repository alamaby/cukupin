package com.alamaby.cukupin.data.repository

import com.alamaby.cukupin.data.local.dao.FundAdjustmentDao
import com.alamaby.cukupin.domain.model.FundAdjustment
import com.alamaby.cukupin.domain.repository.FundAdjustmentRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FundAdjustmentRepositoryImpl(
    private val dao: FundAdjustmentDao
) : FundAdjustmentRepository {

    override fun observeByTarget(targetId: String): Flow<List<FundAdjustment>> =
        dao.observeByTarget(targetId).map { list -> list.map { it.toDomain() } }

    override suspend fun add(adjustment: FundAdjustment) {
        dao.upsert(adjustment.toEntity())
    }
}
