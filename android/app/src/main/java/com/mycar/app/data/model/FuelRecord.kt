package com.mycar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "fuel_records")
data class FuelRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val mileage: Int,
    val liters: Double,
    val costPerLiter: Long,
    val totalCost: Long,
    val isFullTank: Boolean = true,
    val fuelType: String = "بنزین معمولی",
    val gasStation: String = "",
    val notes: String = "",
    val calculatedConsumptionLPer100Km: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)
