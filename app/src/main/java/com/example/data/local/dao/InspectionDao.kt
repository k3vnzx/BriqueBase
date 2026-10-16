package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.InspectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InspectionDao {
    @Query("SELECT * FROM inspections ORDER BY id DESC")
    fun getAllInspections(): Flow<List<InspectionEntity>>

    @Query("SELECT * FROM inspections WHERE id = :id")
    suspend fun getInspectionById(id: Long): InspectionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInspection(inspection: InspectionEntity): Long

    @Delete
    suspend fun deleteInspection(inspection: InspectionEntity)

    @Query("DELETE FROM inspections WHERE id = :id")
    suspend fun deleteInspectionById(id: Long)
}
