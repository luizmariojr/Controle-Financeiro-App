package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val type: String, // CONTA_CORRENTE, POUPANCA, DINHEIRO, CARTEIRA_DIGITAL, INVESTIMENTO, OUTRA
    val institution: String?,
    val initialBalance: Double,
    val currentBalance: Double,
    val colorHex: String = "#10B981",
    val iconName: String = "AccountBalance",
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
