package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "saved_ads")
data class SavedAdEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long? = null,
    val title: String,
    val brandModel: String = "",
    val price: Double = 0.0,
    val platform: String, // WhatsApp, Instagram, Marketplace, Todos
    val tone: String, // Profissional, Direto, Premium, Informal, Focado em Conversão
    val fullAdText: String,
    val whatsappVersion: String = "",
    val instagramVersion: String = "",
    val marketplaceVersion: String = "",
    val highlights: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
