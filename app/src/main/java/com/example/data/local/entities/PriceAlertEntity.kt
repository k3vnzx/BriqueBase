package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "price_alerts")
data class PriceAlertEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productName: String,
    val brand: String = "",
    val model: String = "",
    val category: String = "",
    val targetPrice: Double,
    val location: String = "",
    val includeKeywords: String = "",
    val excludeKeywords: String = "",
    val isActive: Boolean = true,
    val lastChecked: String = "",
    val matchCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)
