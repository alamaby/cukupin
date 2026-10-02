package com.alamaby.cukupin.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.alamaby.cukupin.data.local.dao.BudgetTargetDao
import com.alamaby.cukupin.data.local.dao.CategoryDao
import com.alamaby.cukupin.data.local.dao.ExpenseDao
import com.alamaby.cukupin.data.local.dao.FundAdjustmentDao
import com.alamaby.cukupin.data.local.entity.BudgetTargetEntity
import com.alamaby.cukupin.data.local.entity.CategoryEntity
import com.alamaby.cukupin.data.local.entity.ExpenseEntity
import com.alamaby.cukupin.data.local.entity.FundAdjustmentEntity

@Database(
    entities = [
        BudgetTargetEntity::class,
        ExpenseEntity::class,
        CategoryEntity::class,
        FundAdjustmentEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun budgetTargetDao(): BudgetTargetDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun categoryDao(): CategoryDao
    abstract fun fundAdjustmentDao(): FundAdjustmentDao
}
