package com.alamaby.cukupin.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Target dana: sejumlah uang yang harus bertahan sampai tanggal tertentu.
 * Hanya satu target yang boleh aktif dalam satu waktu (MVP).
 */
data class BudgetTarget(
    val id: String,
    val name: String,
    val initialAmount: Long,
    val currencyCode: String = "IDR",
    val startDate: LocalDate,
    val endDate: LocalDate,
    val lifecycleStatus: TargetLifecycleStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)
