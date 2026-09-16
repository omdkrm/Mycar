package com.mycar.app.data.model

/**
 * Represents the reminder interval type for a service or catalog item:
 * - MILEAGE: Kilometers only (e.g., severe use oil change, tire rotation)
 * - TIME: Date / months only (e.g., annual insurance policy, annual technical inspection)
 * - COMBINED: Whichever comes first - mileage or date (e.g., standard engine oil, timing belt)
 * - NONE: For services or expense items without future reminders (e.g., fuel refill, accidental repair)
 */
enum class ReminderIntervalType(val titlePersian: String, val shortLabel: String) {
    COMBINED("ترکیبی (کیلومتر یا زمان)", "ترکیبی"),
    TIME("بر اساس زمان (تاریخ / ماه)", "زمانی"),
    MILEAGE("بر اساس کارکرد (کیلومتر)", "کارکردی"),
    NONE("بدون یادآوری", "بدون یادآوری");

    companion object {
        fun fromNameSafe(name: String?): ReminderIntervalType {
            if (name.isNullOrBlank()) return COMBINED
            return try {
                valueOf(name)
            } catch (e: Exception) {
                COMBINED
            }
        }
    }
}
