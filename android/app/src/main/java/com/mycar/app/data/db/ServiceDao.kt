package com.mycar.app.data.db

import androidx.room.*
import com.mycar.app.data.model.ServiceRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface ServiceDao {
    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY dateTimestamp DESC")
    fun getServicesForVehicle(vehicleId: String): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM service_records WHERE vehicleId = :vehicleId ORDER BY dateTimestamp DESC")
    suspend fun getServicesListForVehicle(vehicleId: String): List<ServiceRecord>

    @Query("SELECT * FROM service_records ORDER BY dateTimestamp DESC")
    fun getAllServices(): Flow<List<ServiceRecord>>

    @Query("SELECT * FROM service_records ORDER BY dateTimestamp DESC")
    suspend fun getAllServicesList(): List<ServiceRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertService(service: ServiceRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertServices(services: List<ServiceRecord>)

    @Update
    suspend fun updateService(service: ServiceRecord)

    @Delete
    suspend fun deleteService(service: ServiceRecord)

    @Query("DELETE FROM service_records WHERE vehicleId = :vehicleId")
    suspend fun deleteServicesForVehicle(vehicleId: String)

    @Query("DELETE FROM service_records")
    suspend fun deleteAllServices()
}
