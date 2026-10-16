package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.SavedAdEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedAdDao {
    @Query("SELECT * FROM saved_ads ORDER BY id DESC")
    fun getAllSavedAds(): Flow<List<SavedAdEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAd(ad: SavedAdEntity): Long

    @Delete
    suspend fun deleteAd(ad: SavedAdEntity)

    @Query("DELETE FROM saved_ads WHERE id = :id")
    suspend fun deleteAdById(id: Long)
}
