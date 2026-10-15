package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // EXPENSE, INCOME
    val parentCategoryId: Long? = null,
    val iconName: String = "Category",
    val colorHex: String = "#0284C7",
    val isSystem: Boolean = false,
    val isActive: Boolean = true
)
