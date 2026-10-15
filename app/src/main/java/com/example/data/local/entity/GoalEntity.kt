package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String? = null,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val targetDate: String? = null, // YYYY-MM-DD
    val colorHex: String = "#3B82F6",
    val iconName: String = "Target",
    val status: String = "ACTIVE" // ACTIVE, COMPLETED, PAUSED
)
