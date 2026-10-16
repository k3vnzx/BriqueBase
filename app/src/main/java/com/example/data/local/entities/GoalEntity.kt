package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "goals")
data class GoalEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetType: String, // "SALES_COUNT", "REVENUE", "PROFIT", "PRODUCTS_ADDED", "OPPORTUNITIES_ANALYZED", "CUSTOM"
    val targetPeriod: String, // "WEEKLY", "MONTHLY"
    val targetValue: Double,
    val currentValue: Double = 0.0,
    val startDate: String,
    val endDate: String,
    val isCompleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
