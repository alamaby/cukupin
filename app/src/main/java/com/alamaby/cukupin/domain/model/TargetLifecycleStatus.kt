package com.alamaby.cukupin.domain.model

/**
 * Status siklus hidup target berdasarkan waktu dan pengelolaan data.
 * Terpisah dari [BudgetHealthStatus] agar tidak ambigu.
 */
enum class TargetLifecycleStatus {
    UPCOMING,
    ACTIVE,
    COMPLETED,
    ARCHIVED
}
