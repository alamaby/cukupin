package com.alamaby.cukupin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.alamaby.cukupin.data.local.entity.FundAdjustmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FundAdjustmentDao {

    @Query("SELECT * FROM fund_adjustments WHERE targetId = :targetId ORDER BY adjustmentDate ASC")
    fun observeByTarget(targetId: String): Flow<List<FundAdjustmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(adjustment: FundAdjustmentEntity)
}
