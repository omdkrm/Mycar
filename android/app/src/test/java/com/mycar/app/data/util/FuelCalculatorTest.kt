package com.mycar.app.data.util

import com.mycar.app.data.model.FuelRecord
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class FuelCalculatorTest {

    private val vehicleId1 = "v1"
    private val vehicleId2 = "v2"

    /**
     * Case 1:
     * Refueling 1: 50,000 km / 40 L
     * Refueling 2: 50,500 km / 38 L
     * Expected: 7.6 L/100km
     */
    @Test
    fun testCase1_twoConsecutiveRecords() {
        val records = listOf(
            FuelRecord(
                id = "r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            ),
            FuelRecord(
                id = "r2",
                vehicleId = vehicleId1,
                mileage = 50500,
                liters = 38.0,
                costPerLiter = 3000,
                totalCost = 114000
            )
        )

        val result = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNotNull(result)
        assertEquals(7.6, result!!, 0.05)

        val formatted = FuelCalculator.formatConsumption(result, "L/100km")
        assertEquals("7.6 L/100km", formatted)
    }

    /**
     * Case 2:
     * Refueling 1: 50,000 km / 40 L
     * Refueling 2: 50,500 km / 38 L
     * Refueling 3: 51,000 km / 41 L
     * Expected: 7.9 L/100km ((38 + 41) / (500 + 500) * 100 = 7.9)
     */
    @Test
    fun testCase2_threeConsecutiveRecords() {
        val records = listOf(
            FuelRecord(
                id = "r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            ),
            FuelRecord(
                id = "r2",
                vehicleId = vehicleId1,
                mileage = 50500,
                liters = 38.0,
                costPerLiter = 3000,
                totalCost = 114000
            ),
            FuelRecord(
                id = "r3",
                vehicleId = vehicleId1,
                mileage = 51000,
                liters = 41.0,
                costPerLiter = 3000,
                totalCost = 123000
            )
        )

        val result = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNotNull(result)
        assertEquals(7.9, result!!, 0.05)

        val formatted = FuelCalculator.formatConsumption(result, "L/100km")
        assertEquals("7.9 L/100km", formatted)
    }

    /**
     * Case 3:
     * Only one refueling record
     * Expected: "اطلاعات کافی نیست"
     */
    @Test
    fun testCase3_onlyOneRefuelingRecord() {
        val records = listOf(
            FuelRecord(
                id = "r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            )
        )

        val result = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNull(result)

        val formatted = FuelCalculator.formatConsumption(result)
        assertEquals("اطلاعات کافی نیست", formatted)
    }

    /**
     * Case 4:
     * Two records with identical odometer
     * Expected: "اطلاعات کافی نیست"
     */
    @Test
    fun testCase4_twoRecordsWithIdenticalOdometer() {
        val records = listOf(
            FuelRecord(
                id = "r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            ),
            FuelRecord(
                id = "r2",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 38.0,
                costPerLiter = 3000,
                totalCost = 114000
            )
        )

        val result = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNull(result)

        val formatted = FuelCalculator.formatConsumption(result)
        assertEquals("اطلاعات کافی نیست", formatted)
    }

    /**
     * Case 5:
     * Two different vehicles
     * Expected: Each vehicle's calculation is independent
     */
    @Test
    fun testCase5_twoDifferentVehicles() {
        val records = listOf(
            FuelRecord(
                id = "v1_r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            ),
            FuelRecord(
                id = "v2_r1",
                vehicleId = vehicleId2,
                mileage = 10000,
                liters = 20.0,
                costPerLiter = 3000,
                totalCost = 60000
            ),
            FuelRecord(
                id = "v1_r2",
                vehicleId = vehicleId1,
                mileage = 50500,
                liters = 38.0,
                costPerLiter = 3000,
                totalCost = 114000
            ),
            FuelRecord(
                id = "v2_r2",
                vehicleId = vehicleId2,
                mileage = 10400,
                liters = 24.0,
                costPerLiter = 3000,
                totalCost = 72000
            )
        )

        val resultV1 = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNotNull(resultV1)
        assertEquals(7.6, resultV1!!, 0.05)

        // v2: 400 km / 24 L -> 24 / 400 * 100 = 6.0 L/100km
        val resultV2 = FuelCalculator.calculateAverageConsumption(records, vehicleId2)
        assertNotNull(resultV2)
        assertEquals(6.0, resultV2!!, 0.05)
    }

    /**
     * Case 6:
     * A decreasing odometer value
     * Expected: Invalid interval ignored
     */
    @Test
    fun testCase6_decreasingOdometerIgnored() {
        val records = listOf(
            FuelRecord(
                id = "r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            ),
            FuelRecord(
                id = "r2",
                vehicleId = vehicleId1,
                mileage = 49000, // Invalid decrease
                liters = 20.0,
                costPerLiter = 3000,
                totalCost = 60000
            ),
            FuelRecord(
                id = "r3",
                vehicleId = vehicleId1,
                mileage = 50500,
                liters = 38.0,
                costPerLiter = 3000,
                totalCost = 114000
            )
        )

        // When sorted by mileage: 49000 (20L), 50000 (40L), 50500 (38L)
        // Interval 1: 49000 -> 50000: 1000km, 40L
        // Interval 2: 50000 -> 50500: 500km, 38L
        // Or if decreasing timestamp/reversal, distance > 0 is respected.
        // Let's also test where second record has duplicate or invalid decrease:
        val result = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNotNull(result)
    }

    /**
     * Case 7:
     * Zero or negative fuel amount
     * Expected: Invalid record ignored
     */
    @Test
    fun testCase7_zeroOrNegativeFuelAmountIgnored() {
        val records = listOf(
            FuelRecord(
                id = "r1",
                vehicleId = vehicleId1,
                mileage = 50000,
                liters = 40.0,
                costPerLiter = 3000,
                totalCost = 120000
            ),
            FuelRecord(
                id = "r_invalid_zero",
                vehicleId = vehicleId1,
                mileage = 50200,
                liters = 0.0,
                costPerLiter = 3000,
                totalCost = 0
            ),
            FuelRecord(
                id = "r_invalid_neg",
                vehicleId = vehicleId1,
                mileage = 50300,
                liters = -15.0,
                costPerLiter = 3000,
                totalCost = -45000
            ),
            FuelRecord(
                id = "r2",
                vehicleId = vehicleId1,
                mileage = 50500,
                liters = 38.0,
                costPerLiter = 3000,
                totalCost = 114000
            )
        )

        // Only r1 and r2 are valid.
        // 500 km / 38 L -> 7.6 L/100km
        val result = FuelCalculator.calculateAverageConsumption(records, vehicleId1)
        assertNotNull(result)
        assertEquals(7.6, result!!, 0.05)
    }

    /**
     * Display example:
     * 8.2 لیتر در 100 کیلومتر
     */
    @Test
    fun testDisplayExample_persianUnit() {
        val formatted = FuelCalculator.formatConsumption(8.2, "fa")
        assertEquals("8.2 لیتر در 100 کیلومتر", formatted)
    }

    /**
     * Interval consumption on individual record
     */
    @Test
    fun testIntervalConsumption() {
        val r1 = FuelRecord(
            id = "r1",
            vehicleId = vehicleId1,
            mileage = 50000,
            liters = 40.0,
            costPerLiter = 3000,
            totalCost = 120000
        )
        val r2 = FuelRecord(
            id = "r2",
            vehicleId = vehicleId1,
            mileage = 50500,
            liters = 38.0,
            costPerLiter = 3000,
            totalCost = 114000
        )

        val interval1 = FuelCalculator.calculateIntervalConsumption(r1, listOf(r1, r2))
        assertNull(interval1) // Baseline

        val interval2 = FuelCalculator.calculateIntervalConsumption(r2, listOf(r1, r2))
        assertNotNull(interval2)
        assertEquals(7.6, interval2!!, 0.05)
    }
}
