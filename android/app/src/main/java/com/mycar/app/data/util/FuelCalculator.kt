package com.mycar.app.data.util

import com.mycar.app.data.model.FuelRecord
import java.util.Locale

/**
 * Reusable domain helper for vehicle fuel consumption calculations.
 *
 * Implements weighted consecutive interval consumption calculation based on
 * odometer mileage and fuel volume:
 * - A single refueling record establishes the baseline and is not sufficient for calculation.
 * - Each consecutive pair of records with positive distance yields an interval.
 * - The fuel consumed over the interval is associated with the CURRENT refueling.
 * - Total consumption is weighted: (totalFuelLiters / totalDistanceKm) * 100.
 */
object FuelCalculator {

    const val INSUFFICIENT_DATA_TEXT = "اطلاعات کافی نیست"
    private const val MAX_SANITY_CONSUMPTION = 100.0 // 100 L/100km safety guard

    /**
     * Calculates the average fuel consumption (in L/100 km) for a vehicle.
     *
     * @param records List of refueling records (can contain multiple vehicles or unsorted).
     * @param vehicleId Optional ID to filter records for a specific vehicle.
     * @return Calculated consumption in L/100 km as an unrounded Double, or null if insufficient data.
     */
    fun calculateAverageConsumption(
        records: List<FuelRecord>,
        vehicleId: String? = null
    ): Double? {
        // Step 1: Filter refueling records to the selected vehicle
        val vehicleRecords = if (vehicleId != null) {
            records.filter { it.vehicleId == vehicleId }
        } else {
            records
        }

        // Step 2: Remove invalid records (fuel amount <= 0, invalid/non-positive mileage, NaN/Infinite)
        val validRecords = vehicleRecords.filter { record ->
            record.liters > 0.0 &&
            !record.liters.isNaN() &&
            !record.liters.isInfinite() &&
            record.mileage > 0
        }

        // Step 3: Sort records by odometer mileage ascending, secondary by date timestamp
        val sortedRecords = validRecords.sortedWith(
            compareBy<FuelRecord> { it.mileage }.thenBy { it.dateTimestamp }
        )

        // Step 4: A single refueling record is NOT enough to calculate consumption
        if (sortedRecords.size < 2) {
            return null
        }

        // Steps 5, 6, 7, 8: Pairwise consecutive intervals
        var totalDistanceKm = 0
        var totalFuelLiters = 0.0

        for (i in 1 until sortedRecords.size) {
            val previous = sortedRecords[i - 1]
            val current = sortedRecords[i]

            val distanceKm = current.mileage - previous.mileage

            // Step 6: Ignore pairs where distanceKm <= 0 (e.g. duplicate or decreasing mileage)
            if (distanceKm > 0) {
                // Step 7: Interval fuel is current refueling liters
                val intervalLiters = current.liters
                if (intervalLiters > 0.0 && !intervalLiters.isNaN() && !intervalLiters.isInfinite()) {
                    totalDistanceKm += distanceKm
                    totalFuelLiters += intervalLiters
                }
            }
        }

        // Check if we accumulated any valid positive distance
        if (totalDistanceKm <= 0 || totalFuelLiters <= 0.0) {
            return null
        }

        // Step 9: Calculate average consumption = (totalFuelLiters / totalDistanceKm) * 100
        val averageConsumption = (totalFuelLiters / totalDistanceKm.toDouble()) * 100.0

        // Guard against NaN, Infinity, and unrealistic outliers
        if (averageConsumption.isNaN() || averageConsumption.isInfinite() || averageConsumption <= 0.0 || averageConsumption > MAX_SANITY_CONSUMPTION) {
            return null
        }

        return averageConsumption
    }

    /**
     * Calculates the fuel consumption (in L/100 km) for an individual refueling interval
     * relative to its immediate previous valid refueling record by odometer.
     */
    fun calculateIntervalConsumption(
        currentRecord: FuelRecord,
        allRecords: List<FuelRecord>
    ): Double? {
        if (currentRecord.liters <= 0.0 || currentRecord.liters.isNaN() || currentRecord.mileage <= 0) {
            return null
        }

        val validVehicleRecords = allRecords
            .filter { it.vehicleId == currentRecord.vehicleId && it.liters > 0.0 && it.mileage > 0 }
            .sortedWith(compareBy<FuelRecord> { it.mileage }.thenBy { it.dateTimestamp })

        val currentIndex = validVehicleRecords.indexOfFirst { it.id == currentRecord.id }
        if (currentIndex <= 0) {
            // First refueling record or not found; establishes baseline, no interval
            return null
        }

        val previousRecord = validVehicleRecords[currentIndex - 1]
        val distanceKm = currentRecord.mileage - previousRecord.mileage

        if (distanceKm <= 0) {
            return null
        }

        val consumption = (currentRecord.liters / distanceKm.toDouble()) * 100.0
        if (consumption.isNaN() || consumption.isInfinite() || consumption <= 0.0 || consumption > MAX_SANITY_CONSUMPTION) {
            return null
        }

        return consumption
    }

    /**
     * Formats the average consumption into a user-facing string.
     *
     * @param consumption Unrounded Double consumption rate in L/100 km, or null.
     * @param unit Display unit: "L/100 km", "L/100km", or "fa" ("لیتر در 100 کیلومتر").
     * @return Formatted string (e.g. "7.6 L/100 km", "8.2 لیتر در 100 کیلومتر") or "اطلاعات کافی نیست".
     */
    fun formatConsumption(
        consumption: Double?,
        unit: String = "L/100 km"
    ): String {
        if (consumption == null || consumption.isNaN() || consumption.isInfinite()) {
            return INSUFFICIENT_DATA_TEXT
        }

        val formattedValue = String.format(Locale.US, "%.1f", consumption)
        return when (unit) {
            "fa", "لیتر در 100 کیلومتر" -> "$formattedValue لیتر در 100 کیلومتر"
            "L/100km" -> "$formattedValue L/100km"
            else -> "$formattedValue L/100 km"
        }
    }
}
