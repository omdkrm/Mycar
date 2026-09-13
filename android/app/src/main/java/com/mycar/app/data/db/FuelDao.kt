package com.mycar.app.data.db

import androidx.room.*
import com.mycar.app.data.model.FuelRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface FuelDao {
    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY dateTimestamp DESC")
    fun getFuelRecordsForVehicle(vehicleId: String): Flow<List<FuelRecord>>

    @Query("SELECT * FROM fuel_records WHERE vehicleId = :vehicleId ORDER BY dateTimestamp DESC")
    suspend fun getFuelRecordsListForVehicle(vehicleId: String): List<FuelRecord>

    @Query("SELECT * FROM fuel_records ORDER BY dateTimestamp DESC")
    fun getAllFuelRecords(): Flow<List<FuelRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelRecord(fuelRecord: FuelRecord)

    @Update
    suspend fun updateFuelRecord(fuelRecord: FuelRecord)

    @Delete
    suspend fun deleteFuelRecord(fuelRecord: FuelRecord)

    @Query("DELETE FROM fuel_records WHERE vehicleId = :vehicleId")
    suspend fun deleteFuelRecordsForVehicle(vehicleId: String)

    @Query("DELETE FROM fuel_records")
    suspend fun deleteAllFuelRecords()
}
