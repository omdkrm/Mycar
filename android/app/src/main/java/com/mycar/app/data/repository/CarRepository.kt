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
    val lastServiceDateTimestamp: Long
)

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
                isEnabled = true
            )
        }
        scheduleDao.insertSchedules(initialSchedules)
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

        return schedules.map { schedule ->
            val matching = services
                .filter { (schedule.catalogItemId != null && it.catalogItemId == schedule.catalogItemId) || it.partName == schedule.partName }
                .sortedByDescending { it.mileage }

            val latest = matching.firstOrNull()
            val lastMileage = latest?.mileage ?: vehicle.currentMileage
            val lastDate = latest?.dateTimestamp ?: vehicle.createdAt

            val dueMileage = lastMileage + schedule.kmInterval
            val monthsMs = (schedule.timeIntervalMonths * 30.44 * 24 * 60 * 60 * 1000).toLong()
            val dueDateTimestamp = lastDate + monthsMs

            val kmRemaining = dueMileage - vehicle.currentMileage
            val daysRemaining = ((dueDateTimestamp - now) / (1000 * 60 * 60 * 24)).toInt()

            val status = when {
                kmRemaining <= 0 || daysRemaining < 0 -> ReminderStatus.OVERDUE
                kmRemaining <= schedule.warningThresholdKm || daysRemaining <= schedule.warningThresholdDays -> ReminderStatus.APPROACHING
                else -> ReminderStatus.HEALTHY
            }

            ReminderItem(
                schedule = schedule,
                partName = schedule.partName,
                status = status,
                kmRemaining = kmRemaining,
                daysRemaining = daysRemaining,
                dueMileage = dueMileage,
                dueDateTimestamp = dueDateTimestamp,
                lastServiceMileage = lastMileage,
                lastServiceDateTimestamp = lastDate
            )
        }.sortedWith(compareBy({ it.status.ordinal }, { it.kmRemaining }))
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

    suspend fun clearAllUserData() {
        database.vehicleDao().deleteAllVehicles()
        database.serviceDao().deleteAllServices()
        database.fuelDao().deleteAllFuelRecords()
        database.mileageDao().deleteAllMileageRecords()
        database.scheduleDao().deleteAllSchedules()
    }
}
