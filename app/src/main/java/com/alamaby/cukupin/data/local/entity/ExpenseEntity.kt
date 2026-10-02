package com.alamaby.cukupin.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = BudgetTargetEntity::class,
            parentColumns = ["id"],
            childColumns = ["targetId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("targetId"), Index("transactionDate")]
)
data class ExpenseEntity(
    @PrimaryKey val id: String,
    val targetId: String,
    val amount: Long,
    val transactionDate: LocalDate,
    val categoryId: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
