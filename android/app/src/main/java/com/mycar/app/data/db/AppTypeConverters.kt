package com.mycar.app.data.db

import androidx.room.TypeConverter
import com.mycar.app.data.model.ReminderIntervalType

class AppTypeConverters {

    @TypeConverter
    fun fromReminderIntervalType(type: ReminderIntervalType?): String {
        return (type ?: ReminderIntervalType.COMBINED).name
    }

    @TypeConverter
    fun toReminderIntervalType(value: String?): ReminderIntervalType {
        return ReminderIntervalType.fromNameSafe(value)
    }
}
