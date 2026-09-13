package com.mycar.app.data.db

import androidx.room.*
import com.mycar.app.data.model.MaintenanceSchedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM maintenance_schedules WHERE vehicleId = :vehicleId")
    fun getSchedulesForVehicle(vehicleId: String): Flow<List<MaintenanceSchedule>>

    @Query("SELECT * FROM maintenance_schedules WHERE vehicleId = :vehicleId")
    suspend fun getSchedulesListForVehicle(vehicleId: String): List<MaintenanceSchedule>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: MaintenanceSchedule)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedules(schedules: List<MaintenanceSchedule>)

    @Update
    suspend fun updateSchedule(schedule: MaintenanceSchedule)

    @Delete
    suspend fun deleteSchedule(schedule: MaintenanceSchedule)

    @Query("DELETE FROM maintenance_schedules WHERE vehicleId = :vehicleId")
    suspend fun deleteSchedulesForVehicle(vehicleId: String)

    @Query("DELETE FROM maintenance_schedules")
    suspend fun deleteAllSchedules()
}
