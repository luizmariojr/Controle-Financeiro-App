package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.InvoiceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InvoiceDao {
    @Query("SELECT * FROM invoices ORDER BY referenceMonth DESC")
    fun getAllInvoices(): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE cardId = :cardId ORDER BY referenceMonth DESC")
    fun getInvoicesForCard(cardId: Long): Flow<List<InvoiceEntity>>

    @Query("SELECT * FROM invoices WHERE cardId = :cardId AND referenceMonth = :month LIMIT 1")
    suspend fun getInvoiceByCardAndMonth(cardId: Long, month: String): InvoiceEntity?

    @Query("SELECT * FROM invoices WHERE id = :id LIMIT 1")
    suspend fun getInvoiceById(id: Long): InvoiceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity): Long

    @Update
    suspend fun updateInvoice(invoice: InvoiceEntity)
}
