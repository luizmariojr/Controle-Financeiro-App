package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // INCOME, EXPENSE, TRANSFER
    val amount: Double,
    val description: String,
    val date: String, // YYYY-MM-DD
    val timestamp: Long = System.currentTimeMillis(),
    val accountId: Long? = null,
    val destinationAccountId: Long? = null,
    val categoryId: Long? = null,
    val subcategoryId: Long? = null,
    val cardId: Long? = null,
    val invoiceId: Long? = null,
    val isInvoicePayment: Boolean = false,
    val notes: String? = null,
    val status: String = "COMPLETED" // COMPLETED, CANCELLED
)
