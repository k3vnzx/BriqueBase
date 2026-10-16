package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val brand: String,
    val model: String,
    val internalCode: String,
    val imei: String? = null,
    val condition: String,
    val description: String = "",
    val quantity: Int = 1,
    val purchasePrice: Double,
    val additionalCosts: Double = 0.0,
    val announcedPrice: Double,
    val minSalePrice: Double,
    val purchaseDate: String,
    val origin: String = "Marketplace",
    val notes: String = "",
    val status: String = "AVAILABLE", // AVAILABLE, RESERVED, SOLD, ARCHIVED
    val reservationClient: String? = null,
    val reservationDeposit: Double? = null,
    val reservationDate: String? = null,
    val soldPrice: Double? = null,
    val soldDate: String? = null,
    val buyerName: String? = null,
    val buyerPhone: String? = null,
    val paymentMethod: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
