package com.example.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val receiptNumber: String,
    val sellerName: String,
    val sellerPhone: String = "",
    val sellerDocument: String = "",
    val buyerName: String,
    val buyerPhone: String = "",
    val buyerDocument: String = "",
    val productName: String,
    val brandModel: String = "",
    val imeiOrSerial: String = "",
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentStatus: String = "PAGO", // PAGO, PENDENTE, PARCELADO
    val warrantyTerms: String = "Garantia de 90 dias contra defeitos de funcionamento, excluindo avarias por queda ou contato com água.",
    val date: String,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
