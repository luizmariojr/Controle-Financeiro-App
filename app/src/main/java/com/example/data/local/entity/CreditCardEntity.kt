package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val bankName: String? = null,
    val brand: String = "Mastercard",
    val lastFourDigits: String? = null,
    val creditLimit: Double,
    val committedLimit: Double = 0.0,
    val availableLimit: Double = creditLimit,
    val closingDay: Int = 10,
    val dueDay: Int = 20,
    val colorHex: String = "#6366F1",
    val linkedAccountId: Long? = null,
    val isActive: Boolean = true
)
