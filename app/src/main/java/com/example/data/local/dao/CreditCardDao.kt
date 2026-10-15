package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.local.entity.CreditCardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY isActive DESC, name ASC")
    fun getAllCards(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id LIMIT 1")
    suspend fun getCardById(id: Long): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: CreditCardEntity): Long

    @Update
    suspend fun updateCard(card: CreditCardEntity)

    @Query("UPDATE credit_cards SET committedLimit = :committed, availableLimit = :available WHERE id = :cardId")
    suspend fun updateLimits(cardId: Long, committed: Double, available: Double)

    @Query("UPDATE credit_cards SET isActive = :isActive WHERE id = :cardId")
    suspend fun setCardActive(cardId: Long, isActive: Boolean)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteCard(id: Long)
}
