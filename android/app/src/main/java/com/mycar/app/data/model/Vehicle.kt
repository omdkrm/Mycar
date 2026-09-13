package com.mycar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val model: String = "",
    val year: String = "",
    val plateNumber: String = "",
    val currentMileage: Int = 0,
    val fuelCapacityLiters: Double = 50.0,
    val fuelType: String = "بنزین",
    val color: String = "",
    val vin: String = "",
    val notes: String = "",
    val isDefault: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
