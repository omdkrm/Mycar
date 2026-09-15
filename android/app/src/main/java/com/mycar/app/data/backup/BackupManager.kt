package com.mycar.app.data.backup

import android.content.Context
import android.net.Uri
import androidx.room.withTransaction
import com.mycar.app.data.db.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

sealed class RestoreResult {
    object Success : RestoreResult()
    object InvalidFile : RestoreResult()
    object UnsupportedSchema : RestoreResult()
    data class Failure(val message: String) : RestoreResult()
}

class BackupManager(private val database: AppDatabase) {

    private val vehicleDao = database.vehicleDao()
    private val serviceDao = database.serviceDao()
    private val fuelDao = database.fuelDao()
    private val mileageDao = database.mileageDao()
    private val scheduleDao = database.scheduleDao()

    /**
     * Collects all user data from Room, maps it to versioned DTOs, and serializes to JSON string.
     */
    suspend fun createBackupJson(): String = withContext(Dispatchers.IO) {
        val vehicles = vehicleDao.getAllVehiclesList().map { it.toBackupDto() }
        val services = serviceDao.getAllServicesList().map { it.toBackupDto() }
        val fuel = fuelDao.getAllFuelRecordsList().map { it.toBackupDto() }
        val mileage = mileageDao.getAllMileageRecordsList().map { it.toBackupDto() }
        val schedules = scheduleDao.getAllSchedulesList().map { it.toBackupDto() }

        val payload = BackupPayload(
            schemaVersion = BackupPayload.CURRENT_SCHEMA_VERSION,
            appName = BackupPayload.APP_NAME,
            packageName = BackupPayload.PACKAGE_NAME,
            exportTimestamp = System.currentTimeMillis(),
            data = BackupDataContent(
                vehicles = vehicles,
                serviceRecords = services,
                fuelRecords = fuel,
                mileageRecords = mileage,
                maintenanceSchedules = schedules
            )
        )

        BackupSerializer.serialize(payload)
    }

    /**
     * Writes backup string to the given Uri via Storage Access Framework.
     */
    suspend fun writeBackupToUri(context: Context, uri: Uri, jsonContent: String): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(jsonContent)
                    writer.flush()
                }
            } ?: false
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads backup string from the given Uri via Storage Access Framework.
     */
    suspend fun readBackupFromUri(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream, StandardCharsets.UTF_8)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Validates and restores backup JSON into Room database atomically.
     * If validation fails or any error occurs during restore, the database remains untouched.
     */
    suspend fun restoreFromJson(jsonString: String): RestoreResult = withContext(Dispatchers.IO) {
        // Step 1: Strict Validation before touching the database
        val validationResult = BackupSerializer.deserializeAndValidate(jsonString)

        val payload = when (validationResult) {
            is ValidationResult.UnsupportedSchema -> return@withContext RestoreResult.UnsupportedSchema
            is ValidationResult.MalformedFile -> return@withContext RestoreResult.InvalidFile
            is ValidationResult.Success -> validationResult.payload
        }

        // Step 2: Atomic transaction - all or nothing
        try {
            database.withTransaction {
                // Clear existing tables in referential order
                serviceDao.deleteAllServices()
                fuelDao.deleteAllFuelRecords()
                mileageDao.deleteAllMileageRecords()
                scheduleDao.deleteAllSchedules()
                vehicleDao.deleteAllVehicles()

                // Insert restored entities
                val vehicles = payload.data.vehicles.map { it.toEntity() }
                if (vehicles.isNotEmpty()) {
                    vehicleDao.insertVehicles(vehicles)
                }

                val schedules = payload.data.maintenanceSchedules.map { it.toEntity() }
                if (schedules.isNotEmpty()) {
                    scheduleDao.insertSchedules(schedules)
                }

                val services = payload.data.serviceRecords.map { it.toEntity() }
                if (services.isNotEmpty()) {
                    serviceDao.insertServices(services)
                }

                val fuelRecords = payload.data.fuelRecords.map { it.toEntity() }
                if (fuelRecords.isNotEmpty()) {
                    fuelDao.insertFuelRecords(fuelRecords)
                }

                val mileageRecords = payload.data.mileageRecords.map { it.toEntity() }
                if (mileageRecords.isNotEmpty()) {
                    mileageDao.insertMileageRecords(mileageRecords)
                }
            }
            RestoreResult.Success
        } catch (e: Throwable) {
            e.printStackTrace()
            RestoreResult.Failure(e.message ?: "خطا در بازیابی اطلاعات")
        }
    }
}
