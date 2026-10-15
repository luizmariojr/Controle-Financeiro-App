package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "invoices")
data class InvoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cardId: Long,
    val referenceMonth: String, // YYYY-MM
    val closingDate: String, // YYYY-MM-DD
    val dueDate: String, // YYYY-MM-DD
    val totalAmount: Double = 0.0,
    val paidAmount: Double = 0.0,
    val status: String = "OPEN" // OPEN, CLOSED, PAID
)
