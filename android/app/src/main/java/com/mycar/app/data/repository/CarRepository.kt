package com.mycar.app.data.repository

import com.mycar.app.data.db.AppDatabase
import com.mycar.app.data.model.*
import com.mycar.app.data.util.FuelCalculator
import kotlinx.coroutines.flow.Flow
import java.util.UUID

enum class ReminderStatus {
    OVERDUE,
    APPROACHING,
    HEALTHY
}

data class ReminderItem(
    val schedule: MaintenanceSchedule,
    val partName: String,
    val status: ReminderStatus,
    val kmRemaining: Int,
    val daysRemaining: Int,
    val dueMileage: Int,
    val dueDateTimestamp: Long,
    val lastServiceMileage: Int,
    val lastServiceDateTimestamp: Long,
    val intervalType: ReminderIntervalType = schedule.intervalType
) {
    val isTimeOnly: Boolean get() = intervalType == ReminderIntervalType.TIME
    val isMileageOnly: Boolean get() = intervalType == ReminderIntervalType.MILEAGE
    val isCombined: Boolean get() = intervalType == ReminderIntervalType.COMBINED
    val isNone: Boolean get() = intervalType == ReminderIntervalType.NONE
}

data class VehicleStats(
    val totalExpense: Long,
    val totalServiceCost: Long,
    val totalFuelCost: Long,
    val totalLiters: Double,
    val avgConsumptionLPer100Km: Double?,
    val costPerKm: Double?,
    val totalDistanceDrivenKm: Int
)

class CarRepository(private val database: AppDatabase) {
    private val vehicleDao = database.vehicleDao()
    private val serviceDao = database.serviceDao()
    private val fuelDao = database.fuelDao()
    private val mileageDao = database.mileageDao()
    private val scheduleDao = database.scheduleDao()

    val allVehicles: Flow<List<Vehicle>> = vehicleDao.getAllVehicles()

    fun getServices(vehicleId: String): Flow<List<ServiceRecord>> =
        serviceDao.getServicesForVehicle(vehicleId)

    fun getFuelRecords(vehicleId: String): Flow<List<FuelRecord>> =
        fuelDao.getFuelRecordsForVehicle(vehicleId)

    fun getSchedules(vehicleId: String): Flow<List<MaintenanceSchedule>> =
        scheduleDao.getSchedulesForVehicle(vehicleId)

    suspend fun addVehicle(vehicle: Vehicle) {
        vehicleDao.insertVehicle(vehicle)
        // Automatically create standard maintenance schedules for this vehicle from catalog
        val initialSchedules = CatalogData.items.map { item ->
            MaintenanceSchedule(
                id = UUID.randomUUID().toString(),
                vehicleId = vehicle.id,
                partName = item.name,
                catalogItemId = item.id,
                category = item.category,
                kmInterval = item.defaultKmInterval,
                timeIntervalMonths = item.defaultTimeIntervalMonths,
                warningThresholdKm = 500,
                warningThresholdDays = 14,
                isEnabled = item.defaultIntervalType != ReminderIntervalType.NONE,
                intervalType = item.defaultIntervalType
            )
        }
        scheduleDao.insertSchedules(initialSchedules)
    }

    suspend fun upsertSchedule(
        vehicleId: String,
        partName: String,
        catalogItemId: String?,
        category: String,
        intervalType: ReminderIntervalType,
        kmInterval: Int,
        timeIntervalMonths: Int
    ) {
        val existing = scheduleDao.getSchedulesListForVehicle(vehicleId)
            .find { (catalogItemId != null && it.catalogItemId == catalogItemId) || it.partName == partName }

        if (existing != null) {
            scheduleDao.updateSchedule(
                existing.copy(
                    intervalType = intervalType,
                    kmInterval = if (intervalType == ReminderIntervalType.TIME || intervalType == ReminderIntervalType.NONE) 0 else kmInterval,
                    timeIntervalMonths = if (intervalType == ReminderIntervalType.MILEAGE || intervalType == ReminderIntervalType.NONE) 0 else timeIntervalMonths,
                    isEnabled = (intervalType != ReminderIntervalType.NONE)
                )
            )
        } else if (intervalType != ReminderIntervalType.NONE) {
            scheduleDao.insertSchedule(
                MaintenanceSchedule(
                    vehicleId = vehicleId,
                    partName = partName,
                    catalogItemId = catalogItemId,
                    category = category,
                    kmInterval = if (intervalType == ReminderIntervalType.TIME) 0 else kmInterval,
                    timeIntervalMonths = if (intervalType == ReminderIntervalType.MILEAGE) 0 else timeIntervalMonths,
                    warningThresholdKm = 500,
                    warningThresholdDays = 14,
                    isEnabled = true,
                    intervalType = intervalType
                )
            )
        }
    }

    suspend fun updateVehicle(vehicle: Vehicle) {
        vehicleDao.updateVehicle(vehicle)
    }

    suspend fun deleteVehicle(vehicle: Vehicle) {
        vehicleDao.deleteVehicle(vehicle)
        serviceDao.deleteServicesForVehicle(vehicle.id)
        fuelDao.deleteFuelRecordsForVehicle(vehicle.id)
        mileageDao.deleteMileageRecordsForVehicle(vehicle.id)
        scheduleDao.deleteSchedulesForVehicle(vehicle.id)
    }

    suspend fun addService(service: ServiceRecord, updateMileage: Boolean = true) {
        serviceDao.insertService(service)
        if (updateMileage) {
            val vehicle = vehicleDao.getVehicleById(service.vehicleId)
            if (vehicle != null && service.mileage > vehicle.currentMileage) {
                vehicleDao.updateVehicle(vehicle.copy(currentMileage = service.mileage))
                mileageDao.insertMileageRecord(
                    MileageRecord(
                        vehicleId = vehicle.id,
                        mileage = service.mileage,
                        dateTimestamp = service.dateTimestamp,
                        note = "سرویس: ${service.partName}"
                    )
                )
            }
        }
    }

    suspend fun updateService(service: ServiceRecord, updateMileage: Boolean = true) {
        serviceDao.updateService(service)
        if (updateMileage) {
            val vehicle = vehicleDao.getVehicleById(service.vehicleId)
            if (vehicle != null && service.mileage > vehicle.currentMileage) {
                vehicleDao.updateVehicle(vehicle.copy(currentMileage = service.mileage))
                mileageDao.insertMileageRecord(
                    MileageRecord(
                        vehicleId = vehicle.id,
                        mileage = service.mileage,
                        dateTimestamp = service.dateTimestamp,
                        note = "سرویس: ${service.partName}"
                    )
                )
            }
        }
    }

    suspend fun deleteService(service: ServiceRecord) {
        serviceDao.deleteService(service)
    }

    suspend fun addFuelRecord(fuelRecord: FuelRecord, updateMileage: Boolean = true) {
        // Calculate consumption if previous fuel record exists based on consecutive odometer mileage
        val existingRecords = fuelDao.getFuelRecordsListForVehicle(fuelRecord.vehicleId)
        val allRecords = existingRecords.filter { it.id != fuelRecord.id } + fuelRecord
        val calculatedConsumption = FuelCalculator.calculateIntervalConsumption(fuelRecord, allRecords)

        val recordToSave = fuelRecord.copy(calculatedConsumptionLPer100Km = calculatedConsumption)
        fuelDao.insertFuelRecord(recordToSave)

        // Update subsequent record if this record was inserted between or before existing records
        val validSorted = allRecords.filter { it.liters > 0.0 && it.mileage > 0 }
            .sortedWith(compareBy<FuelRecord> { it.mileage }.thenBy { it.dateTimestamp })
        val currentIndex = validSorted.indexOfFirst { it.id == fuelRecord.id }
        if (currentIndex in 0 until validSorted.size - 1) {
            val nextRecord = validSorted[currentIndex + 1]
            val updatedNextConsumption = FuelCalculator.calculateIntervalConsumption(nextRecord, validSorted)
            fuelDao.updateFuelRecord(nextRecord.copy(calculatedConsumptionLPer100Km = updatedNextConsumption))
        }

        if (updateMileage) {
            val vehicle = vehicleDao.getVehicleById(fuelRecord.vehicleId)
            if (vehicle != null && fuelRecord.mileage > vehicle.currentMileage) {
                vehicleDao.updateVehicle(vehicle.copy(currentMileage = fuelRecord.mileage))
                mileageDao.insertMileageRecord(
                    MileageRecord(
                        vehicleId = vehicle.id,
                        mileage = fuelRecord.mileage,
                        dateTimestamp = fuelRecord.dateTimestamp,
                        note = "سوخت‌گیری"
                    )
                )
            }
        }
    }

    suspend fun deleteFuelRecord(record: FuelRecord) {
        fuelDao.deleteFuelRecord(record)
        val remainingRecords = fuelDao.getFuelRecordsListForVehicle(record.vehicleId)
        val validSorted = remainingRecords.filter { it.liters > 0.0 && it.mileage > 0 }
            .sortedWith(compareBy<FuelRecord> { it.mileage }.thenBy { it.dateTimestamp })
        for (item in validSorted) {
            val updated = FuelCalculator.calculateIntervalConsumption(item, validSorted)
            if (updated != item.calculatedConsumptionLPer100Km) {
                fuelDao.updateFuelRecord(item.copy(calculatedConsumptionLPer100Km = updated))
            }
        }
    }

    suspend fun calculateReminders(vehicle: Vehicle): List<ReminderItem> {
        val schedules = scheduleDao.getSchedulesListForVehicle(vehicle.id).filter { it.isEnabled }
        val services = serviceDao.getServicesListForVehicle(vehicle.id)

        val now = System.currentTimeMillis()

        return schedules.mapNotNull { schedule ->
            val effectiveIntervalType = when {
                schedule.intervalType != ReminderIntervalType.COMBINED -> schedule.intervalType
                schedule.catalogItemId in listOf("cat-technical-inspection", "cat-third-party-insurance", "cat-body-insurance") ||
                    schedule.partName.contains("بیمه") || schedule.partName.contains("معاینه") -> ReminderIntervalType.TIME
                schedule.catalogItemId == "cat-fuel" || schedule.partName.contains("بنزین") -> ReminderIntervalType.NONE
                else -> schedule.intervalType
            }

            if (effectiveIntervalType == ReminderIntervalType.NONE) {
                return@mapNotNull null
            }

            val matching = services
                .filter { (schedule.catalogItemId != null && it.catalogItemId == schedule.catalogItemId) || it.partName == schedule.partName }
                .sortedWith(compareByDescending<ServiceRecord> { it.dateTimestamp }.thenByDescending { it.mileage })

            val latest = matching.firstOrNull()
            val lastMileage = latest?.mileage ?: vehicle.currentMileage
            val lastDate = latest?.dateTimestamp ?: vehicle.createdAt

            val kmInterval = schedule.kmInterval
            val timeIntervalMonths = schedule.timeIntervalMonths

            val dueMileage = if (effectiveIntervalType == ReminderIntervalType.TIME) 0 else lastMileage + kmInterval
            val dueDateTimestamp = if (effectiveIntervalType == ReminderIntervalType.MILEAGE) 0L else com.mycar.app.data.util.PersianDateHelper.addJalaliMonths(lastDate, timeIntervalMonths)

            val kmRemaining = if (effectiveIntervalType == ReminderIntervalType.TIME) 0 else dueMileage - vehicle.currentMileage
            val daysRemaining = if (effectiveIntervalType == ReminderIntervalType.MILEAGE) Int.MAX_VALUE else com.mycar.app.data.util.PersianDateHelper.daysBetween(now, dueDateTimestamp)

            val status = when (effectiveIntervalType) {
                ReminderIntervalType.TIME -> when {
                    daysRemaining < 0 -> ReminderStatus.OVERDUE
                    daysRemaining <= schedule.warningThresholdDays -> ReminderStatus.APPROACHING
                    else -> ReminderStatus.HEALTHY
                }
                ReminderIntervalType.MILEAGE -> when {
                    kmRemaining <= 0 -> ReminderStatus.OVERDUE
                    kmRemaining <= schedule.warningThresholdKm -> ReminderStatus.APPROACHING
                    else -> ReminderStatus.HEALTHY
                }
                ReminderIntervalType.COMBINED -> when {
                    kmRemaining <= 0 || daysRemaining < 0 -> ReminderStatus.OVERDUE
                    kmRemaining <= schedule.warningThresholdKm || daysRemaining <= schedule.warningThresholdDays -> ReminderStatus.APPROACHING
                    else -> ReminderStatus.HEALTHY
                }
                ReminderIntervalType.NONE -> ReminderStatus.HEALTHY
            }

            ReminderItem(
                schedule = schedule.copy(intervalType = effectiveIntervalType),
                partName = schedule.partName,
                status = status,
                kmRemaining = kmRemaining,
                daysRemaining = daysRemaining,
                dueMileage = dueMileage,
                dueDateTimestamp = dueDateTimestamp,
                lastServiceMileage = lastMileage,
                lastServiceDateTimestamp = lastDate,
                intervalType = effectiveIntervalType
            )
        }.sortedWith(
            compareBy<ReminderItem> { it.status.ordinal }
                .thenBy { item ->
                    when (item.intervalType) {
                        ReminderIntervalType.TIME -> item.daysRemaining
                        ReminderIntervalType.MILEAGE -> item.kmRemaining / 100
                        ReminderIntervalType.COMBINED -> minOf(item.daysRemaining, item.kmRemaining / 100)
                        ReminderIntervalType.NONE -> Int.MAX_VALUE
                    }
                }
        )
    }

    suspend fun calculateStats(vehicle: Vehicle): VehicleStats {
        val services = serviceDao.getServicesListForVehicle(vehicle.id)
        val fuels = fuelDao.getFuelRecordsListForVehicle(vehicle.id)

        val serviceTotal = services.sumOf { it.costTotal }
        val fuelTotal = fuels.sumOf { it.totalCost }
        val totalExp = serviceTotal + fuelTotal

        val totalLiters = fuels.sumOf { it.liters }
        val avgConsumption = FuelCalculator.calculateAverageConsumption(fuels, vehicle.id)

        val minMileage = fuels.minOfOrNull { it.mileage } ?: vehicle.currentMileage
        val distanceDriven = (vehicle.currentMileage - minMileage).coerceAtLeast(0)
        val costPerKm = if (distanceDriven > 0) totalExp.toDouble() / distanceDriven.toDouble() else null

        return VehicleStats(
            totalExpense = totalExp,
            totalServiceCost = serviceTotal,
            totalFuelCost = fuelTotal,
            totalLiters = totalLiters,
            avgConsumptionLPer100Km = avgConsumption,
            costPerKm = costPerKm,
            totalDistanceDrivenKm = distanceDriven
        )
    }

    val backupManager = com.mycar.app.data.backup.BackupManager(database)

    suspend fun createBackupJson(): String = backupManager.createBackupJson()

    suspend fun restoreFromJson(json: String): com.mycar.app.data.backup.RestoreResult = backupManager.restoreFromJson(json)

    suspend fun clearAllUserData() {
        database.vehicleDao().deleteAllVehicles()
        database.serviceDao().deleteAllServices()
        database.fuelDao().deleteAllFuelRecords()
        database.mileageDao().deleteAllMileageRecords()
        database.scheduleDao().deleteAllSchedules()
    }
}
