package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goal_contributions")
data class GoalContributionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val goalId: Long,
    val amount: Double,
    val type: String, // CONTRIBUTION, WITHDRAWAL
    val date: String, // YYYY-MM-DD
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
