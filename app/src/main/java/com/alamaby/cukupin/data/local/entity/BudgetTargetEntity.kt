package com.alamaby.cukupin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant
import java.time.LocalDate

@Entity(tableName = "budget_targets")
data class BudgetTargetEntity(
    @PrimaryKey val id: String,
    val name: String,
    val initialAmount: Long,
    val currencyCode: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val lifecycleStatus: String,
    val createdAt: Instant,
    val updatedAt: Instant
)
