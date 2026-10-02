package com.alamaby.cukupin.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey val id: String,
    val name: String,
    val iconKey: String,
    val displayOrder: Int,
    val isDefault: Boolean,
    val isActive: Boolean
)
