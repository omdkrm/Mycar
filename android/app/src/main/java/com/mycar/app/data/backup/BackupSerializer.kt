package com.mycar.app.data.backup

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

sealed class ValidationResult {
    data class Success(val payload: BackupPayload) : ValidationResult()
    data class UnsupportedSchema(val version: Int) : ValidationResult()
    data class MalformedFile(val message: String) : ValidationResult()
}

object BackupSerializer {

    const val SUPPORTED_SCHEMA_VERSION = 1
    const val DEFAULT_APP_NAME = "My Car"

    /**
     * Serializes [BackupPayload] into formatted JSON string.
     */
    fun serialize(payload: BackupPayload): String {
        val root = JSONObject()
        root.put("schemaVersion", payload.schemaVersion)
        root.put("appName", payload.appName)
        root.put("packageName", payload.packageName)
        root.put("exportTimestamp", payload.exportTimestamp)

        val dataObj = JSONObject()

        // Vehicles
        val vehiclesArr = JSONArray()
        payload.data.vehicles.forEach { v ->
            val vObj = JSONObject().apply {
                put("id", v.id)
                put("name", v.name)
                put("model", v.model)
                put("year", v.year)
                put("plateNumber", v.plateNumber)
                put("currentMileage", v.currentMileage)
                put("fuelCapacityLiters", v.fuelCapacityLiters)
                put("fuelType", v.fuelType)
                put("color", v.color)
                put("vin", v.vin)
                put("notes", v.notes)
                put("isDefault", v.isDefault)
                put("createdAt", v.createdAt)
            }
            vehiclesArr.put(vObj)
        }
        dataObj.put("vehicles", vehiclesArr)

        // Service records
        val servicesArr = JSONArray()
        payload.data.serviceRecords.forEach { s ->
            val sObj = JSONObject().apply {
                put("id", s.id)
                put("vehicleId", s.vehicleId)
                put("partName", s.partName)
                if (s.catalogItemId != null) put("catalogItemId", s.catalogItemId) else put("catalogItemId", JSONObject.NULL)
                put("serviceCategory", s.serviceCategory)
                put("mileage", s.mileage)
                put("dateTimestamp", s.dateTimestamp)
                put("costTotal", s.costTotal)
                put("partCost", s.partCost)
                put("laborCost", s.laborCost)
                put("brand", s.brand)
                put("serviceCenter", s.serviceCenter)
                put("invoiceNumber", s.invoiceNumber)
                put("notes", s.notes)
                put("createdAt", s.createdAt)
            }
            servicesArr.put(sObj)
        }
        dataObj.put("serviceRecords", servicesArr)

        // Fuel records
        val fuelArr = JSONArray()
        payload.data.fuelRecords.forEach { f ->
            val fObj = JSONObject().apply {
                put("id", f.id)
                put("vehicleId", f.vehicleId)
                put("dateTimestamp", f.dateTimestamp)
                put("mileage", f.mileage)
                put("liters", f.liters)
                put("costPerLiter", f.costPerLiter)
                put("totalCost", f.totalCost)
                put("isFullTank", f.isFullTank)
                put("fuelType", f.fuelType)
                put("gasStation", f.gasStation)
                put("notes", f.notes)
                if (f.calculatedConsumptionLPer100Km != null) {
                    put("calculatedConsumptionLPer100Km", f.calculatedConsumptionLPer100Km)
                } else {
                    put("calculatedConsumptionLPer100Km", JSONObject.NULL)
                }
                put("createdAt", f.createdAt)
            }
            fuelArr.put(fObj)
        }
        dataObj.put("fuelRecords", fuelArr)

        // Mileage records
        val mileageArr = JSONArray()
        payload.data.mileageRecords.forEach { m ->
            val mObj = JSONObject().apply {
                put("id", m.id)
                put("vehicleId", m.vehicleId)
                put("mileage", m.mileage)
                put("dateTimestamp", m.dateTimestamp)
                put("note", m.note)
            }
            mileageArr.put(mObj)
        }
        dataObj.put("mileageRecords", mileageArr)

        // Maintenance schedules
        val scheduleArr = JSONArray()
        payload.data.maintenanceSchedules.forEach { ms ->
            val msObj = JSONObject().apply {
                put("id", ms.id)
                put("vehicleId", ms.vehicleId)
                put("partName", ms.partName)
                if (ms.catalogItemId != null) put("catalogItemId", ms.catalogItemId) else put("catalogItemId", JSONObject.NULL)
                put("category", ms.category)
                put("kmInterval", ms.kmInterval)
                put("timeIntervalMonths", ms.timeIntervalMonths)
                put("warningThresholdKm", ms.warningThresholdKm)
                put("warningThresholdDays", ms.warningThresholdDays)
                put("isEnabled", ms.isEnabled)
                put("intervalType", ms.intervalType)
            }
            scheduleArr.put(msObj)
        }
        dataObj.put("maintenanceSchedules", scheduleArr)

        root.put("data", dataObj)
        return root.toString(2)
    }

    /**
     * Parses and strictly validates a backup JSON string.
     */
    fun deserializeAndValidate(jsonString: String): ValidationResult {
        if (jsonString.isBlank()) {
            return ValidationResult.MalformedFile("محتوای فایل خالی است.")
        }

        val root: JSONObject = try {
            JSONObject(jsonString)
        } catch (e: JSONException) {
            return ValidationResult.MalformedFile("فرمت فایل JSON نامعتبر است: ${e.message}")
        }

        // 1. Verify schemaVersion
        if (!root.has("schemaVersion")) {
            return ValidationResult.MalformedFile("فیلد schemaVersion در فایل یافت نشد.")
        }
        val schemaVersion = root.optInt("schemaVersion", -1)
        if (schemaVersion <= 0 || schemaVersion > SUPPORTED_SCHEMA_VERSION) {
            return ValidationResult.UnsupportedSchema(schemaVersion)
        }

        // 2. Verify data container exists
        if (!root.has("data") || root.isNull("data")) {
            return ValidationResult.MalformedFile("بخش اطلاعات (data) در فایل موجود نیست.")
        }
        val dataObj = root.optJSONObject("data")
            ?: return ValidationResult.MalformedFile("بخش data نامعتبر است.")

        val appName = root.optString("appName", DEFAULT_APP_NAME)
        val packageName = root.optString("packageName", "com.mycar.app")
        val exportTimestamp = root.optLong("exportTimestamp", System.currentTimeMillis())

        // 3. Parse and validate Vehicles
        val vehiclesList = mutableListOf<VehicleBackupDto>()
        val vehicleIds = mutableSetOf<String>()

        val vehiclesArr = dataObj.optJSONArray("vehicles") ?: JSONArray()
        for (i in 0 until vehiclesArr.length()) {
            val vObj = vehiclesArr.optJSONObject(i)
                ?: return ValidationResult.MalformedFile("خودرو شماره $i نامعتبر است.")

            val id = vObj.optString("id")
            val name = vObj.optString("name")
            if (id.isBlank() || name.isBlank()) {
                return ValidationResult.MalformedFile("مشخصات خودرو ناقص است (شناسه یا نام خالی است).")
            }

            val currentMileage = vObj.optInt("currentMileage", 0)
            if (currentMileage < 0) {
                return ValidationResult.MalformedFile("کیلومتر خودرو نمی‌تواند منفی باشد.")
            }

            vehicleIds.add(id)
            vehiclesList.add(
                VehicleBackupDto(
                    id = id,
                    name = name,
                    model = vObj.optString("model", ""),
                    year = vObj.optString("year", ""),
                    plateNumber = vObj.optString("plateNumber", ""),
                    currentMileage = currentMileage,
                    fuelCapacityLiters = vObj.optDouble("fuelCapacityLiters", 50.0),
                    fuelType = vObj.optString("fuelType", "بنزین"),
                    color = vObj.optString("color", ""),
                    vin = vObj.optString("vin", ""),
                    notes = vObj.optString("notes", ""),
                    isDefault = vObj.optBoolean("isDefault", false),
                    createdAt = vObj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // 4. Parse and validate Service Records
        val serviceList = mutableListOf<ServiceRecordBackupDto>()
        val servicesArr = dataObj.optJSONArray("serviceRecords") ?: JSONArray()
        for (i in 0 until servicesArr.length()) {
            val sObj = servicesArr.optJSONObject(i)
                ?: return ValidationResult.MalformedFile("رکورد سرویس شماره $i نامعتبر است.")

            val id = sObj.optString("id")
            val vehicleId = sObj.optString("vehicleId")
            val partName = sObj.optString("partName")
            if (id.isBlank() || vehicleId.isBlank() || partName.isBlank()) {
                return ValidationResult.MalformedFile("رکورد سرویس فاقد فیلدهای الزامی است.")
            }

            // Referential integrity check
            if (!vehicleIds.contains(vehicleId)) {
                return ValidationResult.MalformedFile("رکورد سرویس به خودرویی ارجاع داده که در نسخه پشتیبان وجود ندارد.")
            }

            serviceList.add(
                ServiceRecordBackupDto(
                    id = id,
                    vehicleId = vehicleId,
                    partName = partName,
                    catalogItemId = if (sObj.isNull("catalogItemId")) null else sObj.optString("catalogItemId"),
                    serviceCategory = sObj.optString("serviceCategory", "دوره‌ای"),
                    mileage = sObj.optInt("mileage", 0),
                    dateTimestamp = sObj.optLong("dateTimestamp", System.currentTimeMillis()),
                    costTotal = sObj.optLong("costTotal", 0L),
                    partCost = sObj.optLong("partCost", 0L),
                    laborCost = sObj.optLong("laborCost", 0L),
                    brand = sObj.optString("brand", ""),
                    serviceCenter = sObj.optString("serviceCenter", ""),
                    invoiceNumber = sObj.optString("invoiceNumber", ""),
                    notes = sObj.optString("notes", ""),
                    createdAt = sObj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // 5. Parse and validate Fuel Records
        val fuelList = mutableListOf<FuelRecordBackupDto>()
        val fuelArr = dataObj.optJSONArray("fuelRecords") ?: JSONArray()
        for (i in 0 until fuelArr.length()) {
            val fObj = fuelArr.optJSONObject(i)
                ?: return ValidationResult.MalformedFile("رکورد سوخت شماره $i نامعتبر است.")

            val id = fObj.optString("id")
            val vehicleId = fObj.optString("vehicleId")
            if (id.isBlank() || vehicleId.isBlank()) {
                return ValidationResult.MalformedFile("رکورد سوخت فاقد شناسه یا شناسه خودرو است.")
            }

            // Referential integrity check
            if (!vehicleIds.contains(vehicleId)) {
                return ValidationResult.MalformedFile("رکورد سوخت به خودرویی ارجاع داده که در نسخه پشتیبان وجود ندارد.")
            }

            val liters = fObj.optDouble("liters", 0.0)
            if (liters <= 0.0 || liters.isNaN() || liters.isInfinite()) {
                return ValidationResult.MalformedFile("حجم سوخت در رکورد سوخت‌گیری نامعتبر است.")
            }

            val consumption = if (fObj.isNull("calculatedConsumptionLPer100Km")) {
                null
            } else {
                val c = fObj.optDouble("calculatedConsumptionLPer100Km")
                if (c.isNaN() || c.isInfinite() || c <= 0) null else c
            }

            fuelList.add(
                FuelRecordBackupDto(
                    id = id,
                    vehicleId = vehicleId,
                    dateTimestamp = fObj.optLong("dateTimestamp", System.currentTimeMillis()),
                    mileage = fObj.optInt("mileage", 0),
                    liters = liters,
                    costPerLiter = fObj.optLong("costPerLiter", 0L),
                    totalCost = fObj.optLong("totalCost", 0L),
                    isFullTank = fObj.optBoolean("isFullTank", true),
                    fuelType = fObj.optString("fuelType", "بنزین معمولی"),
                    gasStation = fObj.optString("gasStation", ""),
                    notes = fObj.optString("notes", ""),
                    calculatedConsumptionLPer100Km = consumption,
                    createdAt = fObj.optLong("createdAt", System.currentTimeMillis())
                )
            )
        }

        // 6. Parse and validate Mileage Records
        val mileageList = mutableListOf<MileageRecordBackupDto>()
        val mileageArr = dataObj.optJSONArray("mileageRecords") ?: JSONArray()
        for (i in 0 until mileageArr.length()) {
            val mObj = mileageArr.optJSONObject(i)
                ?: return ValidationResult.MalformedFile("رکورد کارکرد شماره $i نامعتبر است.")

            val id = mObj.optString("id")
            val vehicleId = mObj.optString("vehicleId")
            if (id.isBlank() || vehicleId.isBlank()) {
                return ValidationResult.MalformedFile("رکورد کیلومتر فاقد شناسه است.")
            }

            if (!vehicleIds.contains(vehicleId)) {
                return ValidationResult.MalformedFile("رکورد کیلومتر به خودرویی ارجاع داده که در نسخه پشتیبان وجود ندارد.")
            }

            mileageList.add(
                MileageRecordBackupDto(
                    id = id,
                    vehicleId = vehicleId,
                    mileage = mObj.optInt("mileage", 0),
                    dateTimestamp = mObj.optLong("dateTimestamp", System.currentTimeMillis()),
                    note = mObj.optString("note", "")
                )
            )
        }

        // 7. Parse and validate Maintenance Schedules
        val scheduleList = mutableListOf<MaintenanceScheduleBackupDto>()
        val scheduleArr = dataObj.optJSONArray("maintenanceSchedules") ?: JSONArray()
        for (i in 0 until scheduleArr.length()) {
            val msObj = scheduleArr.optJSONObject(i)
                ?: return ValidationResult.MalformedFile("تنظیمات یادآوری شماره $i نامعتبر است.")

            val id = msObj.optString("id")
            val vehicleId = msObj.optString("vehicleId")
            val partName = msObj.optString("partName")
            if (id.isBlank() || vehicleId.isBlank() || partName.isBlank()) {
                return ValidationResult.MalformedFile("تنظیمات یادآوری فاقد فیلدهای الزامی است.")
            }

            if (!vehicleIds.contains(vehicleId)) {
                return ValidationResult.MalformedFile("تنظیمات یادآوری به خودرویی ارجاع داده که در نسخه پشتیبان وجود ندارد.")
            }

            scheduleList.add(
                MaintenanceScheduleBackupDto(
                    id = id,
                    vehicleId = vehicleId,
                    partName = partName,
                    catalogItemId = if (msObj.isNull("catalogItemId")) null else msObj.optString("catalogItemId"),
                    category = msObj.optString("category", "دوره‌ای"),
                    kmInterval = msObj.optInt("kmInterval", 5000),
                    timeIntervalMonths = msObj.optInt("timeIntervalMonths", 6),
                    warningThresholdKm = msObj.optInt("warningThresholdKm", 500),
                    warningThresholdDays = msObj.optInt("warningThresholdDays", 14),
                    isEnabled = msObj.optBoolean("isEnabled", true),
                    intervalType = msObj.optString("intervalType", ReminderIntervalType.COMBINED.name)
                )
            )
        }

        val payload = BackupPayload(
            schemaVersion = schemaVersion,
            appName = appName,
            packageName = packageName,
            exportTimestamp = exportTimestamp,
            data = BackupDataContent(
                vehicles = vehiclesList,
                serviceRecords = serviceList,
                fuelRecords = fuelList,
                mileageRecords = mileageList,
                maintenanceSchedules = scheduleList
            )
        )

        return ValidationResult.Success(payload)
    }
}
