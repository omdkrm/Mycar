package com.mycar.app.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "mileage_records")
data class MileageRecord(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val vehicleId: String,
    val mileage: Int,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)
