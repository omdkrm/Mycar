package com.mycar.app.data.backup

import org.junit.Assert.*
import org.junit.Test

class BackupSerializerTest {

    private val vehicleId = "vehicle-uuid-1"

    private fun createSamplePayload(): BackupPayload {
        val vehicle = VehicleBackupDto(
            id = vehicleId,
            name = "پژو ۲۰۶",
            model = "تیپ ۵",
            year = "1398",
            plateNumber = "12ب345-67",
            currentMileage = 55000,
            fuelCapacityLiters = 50.0,
            fuelType = "بنزین",
            color = "سفید",
            vin = "IR123456",
            notes = "خودروی شخصی",
            isDefault = true,
            createdAt = 1700000000000L
        )

        val service = ServiceRecordBackupDto(
            id = "service-uuid-1",
            vehicleId = vehicleId,
            partName = "روغن موتور",
            catalogItemId = "cat-engine-oil",
            serviceCategory = "مایعات و روانکارها",
            mileage = 50000,
            dateTimestamp = 1700000000000L,
            costTotal = 650000L,
            partCost = 500000L,
            laborCost = 150000L,
            brand = "کاسترول",
            serviceCenter = "اتوسرویس مرکزی",
            invoiceNumber = "1024",
            notes = "سرویس دوره‌ای",
            createdAt = 1700000000000L
        )

        val fuel = FuelRecordBackupDto(
            id = "fuel-uuid-1",
            vehicleId = vehicleId,
            dateTimestamp = 1700000000000L,
            mileage = 50500,
            liters = 38.0,
            costPerLiter = 3000L,
            totalCost = 114000L,
            isFullTank = true,
            fuelType = "بنزین معمولی",
            gasStation = "جایگاه بهاران",
            notes = "باک پر",
            calculatedConsumptionLPer100Km = 7.6,
            createdAt = 1700000000000L
        )

        val mileage = MileageRecordBackupDto(
            id = "mileage-uuid-1",
            vehicleId = vehicleId,
            mileage = 50500,
            dateTimestamp = 1700000000000L,
            note = "سوخت‌گیری"
        )

        val schedule = MaintenanceScheduleBackupDto(
            id = "schedule-uuid-1",
            vehicleId = vehicleId,
            partName = "روغن موتور",
            catalogItemId = "cat-engine-oil",
            category = "مایعات و روانکارها",
            kmInterval = 5000,
            timeIntervalMonths = 6,
            warningThresholdKm = 500,
            warningThresholdDays = 14,
            isEnabled = true
        )

        return BackupPayload(
            schemaVersion = 1,
            appName = "My Car",
            packageName = "com.mycar.app",
            exportTimestamp = 1700000000000L,
            data = BackupDataContent(
                vehicles = listOf(vehicle),
                serviceRecords = listOf(service),
                fuelRecords = listOf(fuel),
                mileageRecords = listOf(mileage),
                maintenanceSchedules = listOf(schedule)
            )
        )
    }

    @Test
    fun testSerializationAndDeserializationRoundtrip() {
        val originalPayload = createSamplePayload()
        val jsonString = BackupSerializer.serialize(originalPayload)

        assertNotNull(jsonString)
        assertTrue(jsonString.contains("\"schemaVersion\": 1"))
        assertTrue(jsonString.contains("\"appName\": \"My Car\""))
        assertTrue(jsonString.contains("پژو ۲۰۶"))
        assertTrue(jsonString.contains("روغن موتور"))

        val validation = BackupSerializer.deserializeAndValidate(jsonString)
        assertTrue("Expected Success, got: $validation", validation is ValidationResult.Success)

        val restoredPayload = (validation as ValidationResult.Success).payload
        assertEquals(1, restoredPayload.schemaVersion)
        assertEquals("My Car", restoredPayload.appName)
        assertEquals("com.mycar.app", restoredPayload.packageName)

        // Verify vehicles
        assertEquals(1, restoredPayload.data.vehicles.size)
        val v = restoredPayload.data.vehicles[0]
        assertEquals(vehicleId, v.id)
        assertEquals("پژو ۲۰۶", v.name)
        assertEquals(55000, v.currentMileage)
        assertEquals(50.0, v.fuelCapacityLiters, 0.001)

        // Verify service records
        assertEquals(1, restoredPayload.data.serviceRecords.size)
        val s = restoredPayload.data.serviceRecords[0]
        assertEquals("service-uuid-1", s.id)
        assertEquals(vehicleId, s.vehicleId)
        assertEquals("روغن موتور", s.partName)
        assertEquals(650000L, s.costTotal)

        // Verify fuel records
        assertEquals(1, restoredPayload.data.fuelRecords.size)
        val f = restoredPayload.data.fuelRecords[0]
        assertEquals("fuel-uuid-1", f.id)
        assertEquals(38.0, f.liters, 0.001)
        assertEquals(7.6, f.calculatedConsumptionLPer100Km!!, 0.05)

        // Verify mileage records
        assertEquals(1, restoredPayload.data.mileageRecords.size)
        val m = restoredPayload.data.mileageRecords[0]
        assertEquals(50500, m.mileage)

        // Verify schedules
        assertEquals(1, restoredPayload.data.maintenanceSchedules.size)
        val ms = restoredPayload.data.maintenanceSchedules[0]
        assertEquals(5000, ms.kmInterval)
    }

    @Test
    fun testRejectsMalformedJson() {
        val result = BackupSerializer.deserializeAndValidate("not a valid json")
        assertTrue(result is ValidationResult.MalformedFile)
    }

    @Test
    fun testRejectsMissingSchemaVersion() {
        val json = """{"appName": "My Car", "data": {}}"""
        val result = BackupSerializer.deserializeAndValidate(json)
        assertTrue(result is ValidationResult.MalformedFile)
    }

    @Test
    fun testRejectsUnsupportedSchemaVersion() {
        val json = """{"schemaVersion": 99, "appName": "My Car", "data": {}}"""
        val result = BackupSerializer.deserializeAndValidate(json)
        assertTrue(result is ValidationResult.UnsupportedSchema)
        assertEquals(99, (result as ValidationResult.UnsupportedSchema).version)
    }

    @Test
    fun testRejectsOrphanRecordsWithInvalidVehicleId() {
        val json = """
        {
          "schemaVersion": 1,
          "appName": "My Car",
          "packageName": "com.mycar.app",
          "data": {
            "vehicles": [],
            "serviceRecords": [
              {
                "id": "s1",
                "vehicleId": "non-existent-v",
                "partName": "روغن موتور",
                "mileage": 1000
              }
            ]
          }
        }
        """.trimIndent()

        val result = BackupSerializer.deserializeAndValidate(json)
        assertTrue(result is ValidationResult.MalformedFile)
    }

    @Test
    fun testEmptyPayloadIsValid() {
        val emptyPayload = BackupPayload(
            schemaVersion = 1,
            data = BackupDataContent()
        )
        val json = BackupSerializer.serialize(emptyPayload)
        val result = BackupSerializer.deserializeAndValidate(json)
        assertTrue(result is ValidationResult.Success)
        val payload = (result as ValidationResult.Success).payload
        assertTrue(payload.data.vehicles.isEmpty())
        assertTrue(payload.data.serviceRecords.isEmpty())
        assertTrue(payload.data.fuelRecords.isEmpty())
    }
}
