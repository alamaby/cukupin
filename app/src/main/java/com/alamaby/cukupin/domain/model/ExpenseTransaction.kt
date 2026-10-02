package com.alamaby.cukupin.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Satu catatan pengeluaran. Nominal selalu positif dan disimpan sebagai Long.
 */
data class ExpenseTransaction(
    val id: String,
    val targetId: String,
    val amount: Long,
    val transactionDate: LocalDate,
    val categoryId: String?,
    val note: String?,
    val createdAt: Instant,
    val updatedAt: Instant
)
