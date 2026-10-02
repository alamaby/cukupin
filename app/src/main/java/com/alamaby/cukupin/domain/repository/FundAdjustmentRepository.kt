package com.alamaby.cukupin.domain.repository

import com.alamaby.cukupin.domain.model.FundAdjustment
import kotlinx.coroutines.flow.Flow

interface FundAdjustmentRepository {
    fun observeByTarget(targetId: String): Flow<List<FundAdjustment>>
    suspend fun add(adjustment: FundAdjustment)
}
