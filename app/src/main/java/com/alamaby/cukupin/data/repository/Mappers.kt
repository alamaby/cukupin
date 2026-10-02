package com.alamaby.cukupin.data.repository

import com.alamaby.cukupin.data.local.entity.BudgetTargetEntity
import com.alamaby.cukupin.data.local.entity.CategoryEntity
import com.alamaby.cukupin.data.local.entity.ExpenseEntity
import com.alamaby.cukupin.data.local.entity.FundAdjustmentEntity
import com.alamaby.cukupin.domain.model.BudgetTarget
import com.alamaby.cukupin.domain.model.ExpenseCategory
import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.model.FundAdjustment
import com.alamaby.cukupin.domain.model.FundAdjustmentType
import com.alamaby.cukupin.domain.model.TargetLifecycleStatus

/** Mapper entity <-> domain. Terpusat agar konsisten. */

fun BudgetTargetEntity.toDomain() = BudgetTarget(
    id = id,
    name = name,
    initialAmount = initialAmount,
    currencyCode = currencyCode,
    startDate = startDate,
    endDate = endDate,
    lifecycleStatus = TargetLifecycleStatus.valueOf(lifecycleStatus),
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun BudgetTarget.toEntity() = BudgetTargetEntity(
    id = id,
    name = name,
    initialAmount = initialAmount,
    currencyCode = currencyCode,
    startDate = startDate,
    endDate = endDate,
    lifecycleStatus = lifecycleStatus.name,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ExpenseEntity.toDomain() = ExpenseTransaction(
    id = id,
    targetId = targetId,
    amount = amount,
    transactionDate = transactionDate,
    categoryId = categoryId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun ExpenseTransaction.toEntity() = ExpenseEntity(
    id = id,
    targetId = targetId,
    amount = amount,
    transactionDate = transactionDate,
    categoryId = categoryId,
    note = note,
    createdAt = createdAt,
    updatedAt = updatedAt
)

fun CategoryEntity.toDomain() = ExpenseCategory(
    id = id,
    name = name,
    iconKey = iconKey,
    displayOrder = displayOrder,
    isDefault = isDefault,
    isActive = isActive
)

fun ExpenseCategory.toEntity() = CategoryEntity(
    id = id,
    name = name,
    iconKey = iconKey,
    displayOrder = displayOrder,
    isDefault = isDefault,
    isActive = isActive
)

fun FundAdjustmentEntity.toDomain() = FundAdjustment(
    id = id,
    targetId = targetId,
    amount = amount,
    type = FundAdjustmentType.valueOf(type),
    reason = reason,
    adjustmentDate = adjustmentDate,
    createdAt = createdAt
)

fun FundAdjustment.toEntity() = FundAdjustmentEntity(
    id = id,
    targetId = targetId,
    amount = amount,
    type = type.name,
    reason = reason,
    adjustmentDate = adjustmentDate,
    createdAt = createdAt
)
