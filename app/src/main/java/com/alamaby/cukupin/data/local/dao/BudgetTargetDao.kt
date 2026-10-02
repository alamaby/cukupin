package com.alamaby.cukupin.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.alamaby.cukupin.data.local.entity.BudgetTargetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetTargetDao {

    /** Target aktif: satu-satunya target yang belum COMPLETED/ARCHIVED. */
    @Query(
        """
        SELECT * FROM budget_targets
        WHERE lifecycleStatus IN ('ACTIVE', 'UPCOMING')
        ORDER BY createdAt DESC
        LIMIT 1
        """
    )
    fun observeActiveTarget(): Flow<BudgetTargetEntity?>

    @Query("SELECT * FROM budget_targets WHERE id = :id")
    fun observeTarget(id: String): Flow<BudgetTargetEntity?>

    @Query("SELECT * FROM budget_targets WHERE id = :id")
    suspend fun getById(id: String): BudgetTargetEntity?

    @Query("SELECT * FROM budget_targets ORDER BY createdAt DESC")
    fun observeAll(): Flow<List<BudgetTargetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(target: BudgetTargetEntity)

    @Update
    suspend fun update(target: BudgetTargetEntity)

    @Query("UPDATE budget_targets SET lifecycleStatus = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateStatus(id: String, status: String, updatedAt: Long)
}
