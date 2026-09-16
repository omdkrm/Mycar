package com.mycar.app.data.backup

import com.mycar.app.data.model.*

/**
 * Top-level structured versioned backup format for My Car application.
 */
data class BackupPayload(
    val schemaVersion: Int = CURRENT_SCHEMA_VERSION,
    val appName: String = APP_NAME,
    val packageName: String = PACKAGE_NAME,
    val exportTimestamp: Long = System.currentTimeMillis(),
    val data: BackupDataContent
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
        const val APP_NAME = "My Car"
        const val PACKAGE_NAME = "com.mycar.app"
    }
}

data class BackupDataContent(
    val vehicles: List<VehicleBackupDto> = emptyList(),
    val serviceRecords: List<ServiceRecordBackupDto> = emptyList(),
    val fuelRecords: List<FuelRecordBackupDto> = emptyList(),
    val mileageRecords: List<MileageRecordBackupDto> = emptyList(),
    val maintenanceSchedules: List<MaintenanceScheduleBackupDto> = emptyList()
)

data class VehicleBackupDto(
    val id: String,
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

data class ServiceRecordBackupDto(
    val id: String,
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

data class FuelRecordBackupDto(
    val id: String,
    val vehicleId: String,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val mileage: Int,
    val liters: Double,
    val costPerLiter: Long = 0L,
    val totalCost: Long = 0L,
    val isFullTank: Boolean = true,
    val fuelType: String = "بنزین معمولی",
    val gasStation: String = "",
    val notes: String = "",
    val calculatedConsumptionLPer100Km: Double? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class MileageRecordBackupDto(
    val id: String,
    val vehicleId: String,
    val mileage: Int,
    val dateTimestamp: Long = System.currentTimeMillis(),
    val note: String = ""
)

data class MaintenanceScheduleBackupDto(
    val id: String,
    val vehicleId: String,
    val partName: String,
    val catalogItemId: String? = null,
    val category: String = "دوره‌ای",
    val kmInterval: Int,
    val timeIntervalMonths: Int,
    val warningThresholdKm: Int = 500,
    val warningThresholdDays: Int = 14,
    val isEnabled: Boolean = true,
    val intervalType: String = ReminderIntervalType.COMBINED.name
)

// Mapping functions between Room entities and Backup DTOs
fun Vehicle.toBackupDto() = VehicleBackupDto(
    id = id,
    name = name,
    model = model,
    year = year,
    plateNumber = plateNumber,
    currentMileage = currentMileage,
    fuelCapacityLiters = fuelCapacityLiters,
    fuelType = fuelType,
    color = color,
    vin = vin,
    notes = notes,
    isDefault = isDefault,
    createdAt = createdAt
)

fun VehicleBackupDto.toEntity() = Vehicle(
    id = id,
    name = name,
    model = model,
    year = year,
    plateNumber = plateNumber,
    currentMileage = currentMileage,
    fuelCapacityLiters = fuelCapacityLiters,
    fuelType = fuelType,
    color = color,
    vin = vin,
    notes = notes,
    isDefault = isDefault,
    createdAt = createdAt
)

fun ServiceRecord.toBackupDto() = ServiceRecordBackupDto(
    id = id,
    vehicleId = vehicleId,
    partName = partName,
    catalogItemId = catalogItemId,
    serviceCategory = serviceCategory,
    mileage = mileage,
    dateTimestamp = dateTimestamp,
    costTotal = costTotal,
    partCost = partCost,
    laborCost = laborCost,
    brand = brand,
    serviceCenter = serviceCenter,
    invoiceNumber = invoiceNumber,
    notes = notes,
    createdAt = createdAt
)

fun ServiceRecordBackupDto.toEntity() = ServiceRecord(
    id = id,
    vehicleId = vehicleId,
    partName = partName,
    catalogItemId = catalogItemId,
    serviceCategory = serviceCategory,
    mileage = mileage,
    dateTimestamp = dateTimestamp,
    costTotal = costTotal,
    partCost = partCost,
    laborCost = laborCost,
    brand = brand,
    serviceCenter = serviceCenter,
    invoiceNumber = invoiceNumber,
    notes = notes,
    createdAt = createdAt
)

fun FuelRecord.toBackupDto() = FuelRecordBackupDto(
    id = id,
    vehicleId = vehicleId,
    dateTimestamp = dateTimestamp,
    mileage = mileage,
    liters = liters,
    costPerLiter = costPerLiter,
    totalCost = totalCost,
    isFullTank = isFullTank,
    fuelType = fuelType,
    gasStation = gasStation,
    notes = notes,
    calculatedConsumptionLPer100Km = calculatedConsumptionLPer100Km,
    createdAt = createdAt
)

fun FuelRecordBackupDto.toEntity() = FuelRecord(
    id = id,
    vehicleId = vehicleId,
    dateTimestamp = dateTimestamp,
    mileage = mileage,
    liters = liters,
    costPerLiter = costPerLiter,
    totalCost = totalCost,
    isFullTank = isFullTank,
    fuelType = fuelType,
    gasStation = gasStation,
    notes = notes,
    calculatedConsumptionLPer100Km = calculatedConsumptionLPer100Km,
    createdAt = createdAt
)

fun MileageRecord.toBackupDto() = MileageRecordBackupDto(
    id = id,
    vehicleId = vehicleId,
    mileage = mileage,
    dateTimestamp = dateTimestamp,
    note = note
)

fun MileageRecordBackupDto.toEntity() = MileageRecord(
    id = id,
    vehicleId = vehicleId,
    mileage = mileage,
    dateTimestamp = dateTimestamp,
    note = note
)

fun MaintenanceSchedule.toBackupDto() = MaintenanceScheduleBackupDto(
    id = id,
    vehicleId = vehicleId,
    partName = partName,
    catalogItemId = catalogItemId,
    category = category,
    kmInterval = kmInterval,
    timeIntervalMonths = timeIntervalMonths,
    warningThresholdKm = warningThresholdKm,
    warningThresholdDays = warningThresholdDays,
    isEnabled = isEnabled,
    intervalType = intervalType.name
)

fun MaintenanceScheduleBackupDto.toEntity() = MaintenanceSchedule(
    id = id,
    vehicleId = vehicleId,
    partName = partName,
    catalogItemId = catalogItemId,
    category = category,
    kmInterval = kmInterval,
    timeIntervalMonths = timeIntervalMonths,
    warningThresholdKm = warningThresholdKm,
    warningThresholdDays = warningThresholdDays,
    isEnabled = isEnabled,
    intervalType = ReminderIntervalType.fromNameSafe(intervalType)
)
