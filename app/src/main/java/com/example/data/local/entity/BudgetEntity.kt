package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "budgets")
data class BudgetEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val categoryId: Long,
    val subcategoryId: Long? = null,
    val amount: Double,
    val monthYear: String, // YYYY-MM
    val alertThresholdPercentage: Int = 80
)
