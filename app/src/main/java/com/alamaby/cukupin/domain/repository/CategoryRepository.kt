package com.alamaby.cukupin.domain.repository

import com.alamaby.cukupin.domain.model.ExpenseCategory
import kotlinx.coroutines.flow.Flow

interface CategoryRepository {
    fun observeActiveCategories(): Flow<List<ExpenseCategory>>
    suspend fun seedDefaults()
}
