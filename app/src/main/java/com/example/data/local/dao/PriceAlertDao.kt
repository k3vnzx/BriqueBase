package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.PriceAlertEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PriceAlertDao {
    @Query("SELECT * FROM price_alerts ORDER BY id DESC")
    fun getAllAlerts(): Flow<List<PriceAlertEntity>>

    @Query("SELECT * FROM price_alerts WHERE isActive = 1")
    suspend fun getActiveAlerts(): List<PriceAlertEntity>

    @Query("SELECT * FROM price_alerts WHERE id = :id")
    suspend fun getAlertById(id: Long): PriceAlertEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: PriceAlertEntity): Long

    @Update
    suspend fun updateAlert(alert: PriceAlertEntity)

    @Delete
    suspend fun deleteAlert(alert: PriceAlertEntity)

    @Query("DELETE FROM price_alerts WHERE id = :id")
    suspend fun deleteAlertById(id: Long)
}
