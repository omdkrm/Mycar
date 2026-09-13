package com.mycar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "service_records")
data class ServiceRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val partName: String,
    val catalogItemId: String? = null,
    val serviceCategory: String = "دوره‌ای",
    val mileage: Int,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val costTotal: Long = 0L,
    val partCost: Long = 0L,
    val laborCost: Long = 0L,
    val brand: String = "",
    val serviceCenter: String = "",
    val invoiceNumber: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
