package com.mycar.app.data.db

import androidx.room.*
import com.mycar.app.data.model.MileageRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface MileageDao {
    @Query("SELECT * FROM mileage_records WHERE vehicleId = :vehicleId ORDER BY dateTimestamp DESC")
    fun getMileageRecordsForVehicle(vehicleId: String): Flow<List<MileageRecord>>

    @Query("SELECT * FROM mileage_records ORDER BY dateTimestamp DESC")
    suspend fun getAllMileageRecordsList(): List<MileageRecord>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMileageRecord(record: MileageRecord)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMileageRecords(records: List<MileageRecord>)

    @Delete
    suspend fun deleteMileageRecord(record: MileageRecord)

    @Query("DELETE FROM mileage_records WHERE vehicleId = :vehicleId")
    suspend fun deleteMileageRecordsForVehicle(vehicleId: String)

    @Query("DELETE FROM mileage_records")
    suspend fun deleteAllMileageRecords()
}
