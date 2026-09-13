package com.mycar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "maintenance_schedules")
data class MaintenanceSchedule(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val partName: String,
    val catalogItemId: String? = null,
    val category: String = "دوره‌ای",
    val kmInterval: Int,
    val timeIntervalMonths: Int,
    val warningThresholdKm: Int = 500,
    val warningThresholdDays: Int = 14,
    val isEnabled: Boolean = true
)
