package com.mycar.app

import android.app.Application
import com.mycar.app.data.db.AppDatabase
import com.mycar.app.data.repository.CarRepository

class MyCarApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val repository: CarRepository by lazy { CarRepository(database) }

    override fun onCreate() {
        super.onCreate()
    }
}
