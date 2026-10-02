package com.alamaby.cukupin.domain.model

import java.time.Instant
import java.time.LocalDate

/**
 * Penyesuaian dana di tengah periode (mis. tambahan uang saku).
 * [amount] selalu positif; arah perubahan ditentukan oleh [type].
 */
data class FundAdjustment(
    val id: String,
    val targetId: String,
    val amount: Long,
    val type: FundAdjustmentType,
    val reason: String?,
    val adjustmentDate: LocalDate,
    val createdAt: Instant
)
