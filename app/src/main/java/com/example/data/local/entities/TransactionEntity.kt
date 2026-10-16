package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "INCOME" or "EXPENSE"
    val description: String,
    val amount: Double,
    val date: String,
    val category: String,
    val paymentMethod: String,
    val status: String = "COMPLETED", // "COMPLETED" or "PLANNED"
    val productId: Long? = null,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
