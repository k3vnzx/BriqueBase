package com.example.data.local.dao

import androidx.room.*
import com.example.data.local.entities.OpportunityEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OpportunityDao {
    @Query("SELECT * FROM opportunities ORDER BY discountPercent DESC, id DESC")
    fun getAllOpportunities(): Flow<List<OpportunityEntity>>

    @Query("SELECT * FROM opportunities WHERE id = :id")
    suspend fun getOpportunityById(id: Long): OpportunityEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOpportunity(opportunity: OpportunityEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(opportunities: List<OpportunityEntity>)

    @Update
    suspend fun updateOpportunity(opportunity: OpportunityEntity)

    @Delete
    suspend fun deleteOpportunity(opportunity: OpportunityEntity)

    @Query("DELETE FROM opportunities WHERE id = :id")
    suspend fun deleteOpportunityById(id: Long)
}
