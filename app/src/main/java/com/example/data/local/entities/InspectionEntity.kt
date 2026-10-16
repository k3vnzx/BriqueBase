package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "inspections")
data class InspectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val deviceModel: String,
    val imeiOrSerial: String = "",
    val sellerName: String = "",
    val date: String,
    val totalApproved: Int,
    val totalAttention: Int,
    val totalFailed: Int,
    val totalSkipped: Int,
    val recommendation: String, // "PROCEED", "CAUTION", "ABORT"
    val generalNotes: String = "",
    val checklistData: String, // JSON checklist state
    val createdAt: Long = System.currentTimeMillis()
)
