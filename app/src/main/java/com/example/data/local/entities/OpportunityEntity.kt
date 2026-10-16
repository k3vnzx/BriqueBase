package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "opportunities")
data class OpportunityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: String,
    val brand: String,
    val model: String,
    val announcedPrice: Double,
    val referencePrice: Double, // Fipe / Market reference
    val discountPercent: Double,
    val location: String = "São Paulo, SP",
    val sourceUrl: String = "",
    val estimatedCosts: Double = 0.0,
    val estimatedResalePrice: Double = 0.0,
    val estimatedProfit: Double = 0.0,
    val condition: String = "Seminovo",
    val status: String = "NEW", // NEW, ANALYZED, CONVERTED, DISCARDED
    val isFavorite: Boolean = false,
    val notes: String = "",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
