package com.alamaby.cukupin.domain.model

/**
 * Kategori pengeluaran. Bersifat label informatif saja (bukan anggaran ketat).
 */
data class ExpenseCategory(
    val id: String,
    val name: String,
    val iconKey: String,
    val displayOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean
)
