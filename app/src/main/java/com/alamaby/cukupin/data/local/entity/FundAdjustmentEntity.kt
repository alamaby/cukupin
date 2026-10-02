package com.alamaby.cukupin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "fund_adjustments",
    foreignKeys = [
        ForeignKey(
            entity = BudgetTargetEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("targetId")]
)
data class FundAdjustmentEntity(
    @PrimaryKey val id: String,
    val targetId: String,
    val amount: Long,
    val type: String,
    val reason: String?,
    val adjustmentDate: LocalDate,
    val createdAt: Instant
)
