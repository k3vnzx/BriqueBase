package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfileEntity(
    @PrimaryKey val id: Int = 1,
    val displayName: String = "Briqueiro Pro",
    val businessName: String = "Briquei Revendas",
    val phone: String = "(11) 98765-4321",
    val pixKey: String = "contato@briquei.com.br",
    val currency: String = "BRL",
    val userRankPoints: Int = 320,
    val userLevel: String = "Mestre do Brique",
    val notificationsEnabled: Boolean = true,
    val demoLoaded: Boolean = false
)
