package com.mycar.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mycar.app.data.model.*

@Database(
    entities = [
        Vehicle::class,
        ServiceRecord::class,
        FuelRecord::class,
        MileageRecord::class,
        MaintenanceSchedule::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(AppTypeConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun serviceDao(): ServiceDao
    abstract fun fuelDao(): FuelDao
    abstract fun mileageDao(): MileageDao
    abstract fun scheduleDao(): ScheduleDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add intervalType column with default value COMBINED
                db.execSQL("ALTER TABLE maintenance_schedules ADD COLUMN intervalType TEXT NOT NULL DEFAULT 'COMBINED'")
                // Update known time-only services (inspection and insurances)
                db.execSQL("UPDATE maintenance_schedules SET intervalType = 'TIME', kmInterval = 0 WHERE catalogItemId IN ('cat-technical-inspection', 'cat-third-party-insurance', 'cat-body-insurance') OR partName LIKE '%بیمه%' OR partName LIKE '%معاینه%'")
                // Update fuel refill to NONE
                db.execSQL("UPDATE maintenance_schedules SET intervalType = 'NONE' WHERE catalogItemId = 'cat-fuel' OR partName LIKE '%بنزین%'")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "my_car_database"
                )
                    .addMigrations(MIGRATION_1_2)
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
