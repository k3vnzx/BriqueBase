package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notifications")
data class AppNotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String, // SALE, STOCK, GOAL, ALERT, INSPECTION, RECEIPT, SYSTEM
    val isRead: Boolean = false,
    val timestamp: Long = System.currentTimeMillis(),
    val actionRoute: String = ""
)
