package com.alamaby.cukupin.data.repository

import com.alamaby.cukupin.data.local.dao.CategoryDao
import com.alamaby.cukupin.domain.model.ExpenseCategory
import com.alamaby.cukupin.domain.repository.CategoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CategoryRepositoryImpl(
    private val dao: CategoryDao
) : CategoryRepository {

    override fun observeActiveCategories(): Flow<List<ExpenseCategory>> =
        dao.observeActive().map { list -> list.map { it.toDomain() } }

    override suspend fun seedDefaults() {
        if (dao.count() == 0) {
            dao.insertAll(DEFAULT_CATEGORIES.map { it.toEntity() })
        }
    }

    companion object {
        val DEFAULT_CATEGORIES = listOf(
            ExpenseCategory("makanan", "Makanan", "restaurant", 0, true, true),
            ExpenseCategory("transportasi", "Transportasi", "directions_car", 1, true, true),
            ExpenseCategory("belanja", "Belanja", "shopping_bag", 2, true, true),
            ExpenseCategory("hiburan", "Hiburan", "movie", 3, true, true),
            ExpenseCategory("kesehatan", "Kesehatan", "health", 4, true, true),
            ExpenseCategory("pendidikan", "Pendidikan", "school", 5, true, true),
            ExpenseCategory("tagihan", "Tagihan", "receipt", 6, true, true),
            ExpenseCategory("lainnya", "Lainnya", "category", 7, true, true)
        )

        const val FALLBACK_CATEGORY_ID = "lainnya"
    }
}
