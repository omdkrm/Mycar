package com.mycar.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalGasStation
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mycar.app.data.model.FuelRecord
import com.mycar.app.data.model.Vehicle
import com.mycar.app.data.util.FuelCalculator
import com.mycar.app.data.util.PersianDateHelper
import com.mycar.app.data.util.PriceFormatter
import com.mycar.app.ui.components.AppDialog
import com.mycar.app.ui.components.AppShapes
import com.mycar.app.ui.components.PersianDatePickerDialog
import kotlinx.coroutines.launch

private val FUEL_TYPES = listOf("بنزین معمولی", "بنزین سوپر", "گازوئیل", "گاز CNG")
private const val DEFAULT_COST_PER_LITER = 3000L
private const val ONE_DAY_MILLIS = 86_400_000L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(
    activeVehicle: Vehicle?,
    fuelRecords: List<FuelRecord>,
    onAddFuelRecord: (String, Long, Int, Double, Long, Long, Boolean, String, String, String) -> Unit,
    onUpdateFuelRecord: (FuelRecord) -> Unit = {},
    onDeleteFuelRecord: (FuelRecord) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var recordToEdit by remember { mutableStateOf<FuelRecord?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (activeVehicle != null) {
                FloatingActionButton(
                    onClick = {
                        recordToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ثبت سوخت‌گیری")
                }
            }
        }
    ) { padding ->
        if (activeVehicle == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ابتدا یک خودرو را انتخاب یا ثبت کنید.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else if (fuelRecords.isEmpty()) {
            EmptyFuel(
                onAddClick = {
                    recordToEdit = null
                    showDialog = true
                },
                modifier = Modifier.padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(fuelRecords, key = { it.id }) { record ->
                    FuelItemCard(
                        record = record,
                        onEdit = {
                            recordToEdit = record
                            showDialog = true
                        },
                        onDelete = { onDeleteFuelRecord(record) }
                    )
                }
            }
        }
    }

    if (showDialog && activeVehicle != null) {
        FuelFormDialog(
            initialRecord = recordToEdit,
            defaultMileage = activeVehicle.currentMileage,
            defaultCostPerLiter = fuelRecords.firstOrNull()?.costPerLiter ?: DEFAULT_COST_PER_LITER,
            onDismiss = {
                showDialog = false
                recordToEdit = null
            },
            onSave = { dateTimestamp, km, liters, costPerLiter, totalCost, isFull, fuelType, station, notes ->
                val editing = recordToEdit
                if (editing != null) {
                    onUpdateFuelRecord(
                        editing.copy(
                            dateTimestamp = dateTimestamp,
                            mileage = km,
                            liters = liters,
                            costPerLiter = costPerLiter,
                            totalCost = totalCost,
                            isFullTank = isFull,
                            fuelType = fuelType,
                            gasStation = station,
                            notes = notes
                        )
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تغییرات با موفقیت ذخیره شد.")
                    }
                } else {
                    onAddFuelRecord(
                        activeVehicle.id,
                        dateTimestamp,
                        km,
                        liters,
                        costPerLiter,
                        totalCost,
                        isFull,
                        fuelType,
                        station,
                        notes
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("سوخت‌گیری با موفقیت ثبت شد.")
                    }
                }
                showDialog = false
                recordToEdit = null
            }
        )
    }
}

@Composable
fun EmptyFuel(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.LocalGasStation,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "هنوز سابقه سوخت‌گیری ثبت نشده است.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "برای محاسبه دقیق مصرف سوخت و هزینه‌ها، بنزین مصرفی را ثبت کنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            shape = AppShapes.Button,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "ثبت اولین سوخت‌گیری", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun FuelItemCard(
    record: FuelRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = AppShapes.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${formatLiters(record.liters)} لیتر (${record.fuelType})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (record.isFullTank) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("باک پر", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تاریخ: ${PersianDateHelper.formatJalali(record.dateTimestamp)} | کیلومتر: ${PriceFormatter.formatWithSeparators(record.mileage)} | هزینه: ${PriceFormatter.formatCostWithUnit(record.totalCost)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (record.gasStation.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "جایگاه: ${record.gasStation}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (record.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "توضیحات: ${record.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                record.calculatedConsumptionLPer100Km?.let { consumption ->
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "مصرف: ${FuelCalculator.formatConsumption(consumption, "fa")}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "ویرایش سوخت‌گیری",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "حذف سوخت‌گیری",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Add / edit dialog for a refueling record.
 * Includes the Jalali refueling date, validation messages and every field of the record.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelFormDialog(
    initialRecord: FuelRecord?,
    defaultMileage: Int,
    defaultCostPerLiter: Long,
    onDismiss: () -> Unit,
    onSave: (
        dateTimestamp: Long,
        mileage: Int,
        liters: Double,
        costPerLiter: Long,
        totalCost: Long,
        isFullTank: Boolean,
        fuelType: String,
        station: String,
        notes: String
    ) -> Unit
) {
    val isEditMode = initialRecord != null

    var selectedTimestamp by remember {
        mutableStateOf(initialRecord?.dateTimestamp ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }

    var mileageStr by remember {
        mutableStateOf((initialRecord?.mileage ?: defaultMileage).toString())
    }
    var litersStr by remember {
        mutableStateOf(initialRecord?.let { formatLiters(it.liters) } ?: "")
    }
    var costPerLiterStr by remember {
        mutableStateOf(
            PriceFormatter.formatWithSeparators(initialRecord?.costPerLiter ?: defaultCostPerLiter)
        )
    }
    var totalCostStr by remember {
        mutableStateOf(initialRecord?.let { PriceFormatter.formatWithSeparators(it.totalCost) } ?: "")
    }
    var isFullTank by remember { mutableStateOf(initialRecord?.isFullTank ?: true) }
    var fuelType by remember { mutableStateOf(initialRecord?.fuelType ?: FUEL_TYPES.first()) }
    var gasStation by remember { mutableStateOf(initialRecord?.gasStation ?: "") }
    var notes by remember { mutableStateOf(initialRecord?.notes ?: "") }

    var mileageError by remember { mutableStateOf<String?>(null) }
    var litersError by remember { mutableStateOf<String?>(null) }

    fun recalculateTotal(litersText: String, costPerLiterText: String) {
        val liters = parseLiters(litersText) ?: 0.0
        val costPerLiter = PriceFormatter.parseCost(costPerLiterText)
        if (liters > 0.0 && costPerLiter > 0L) {
            totalCostStr = PriceFormatter.formatWithSeparators((liters * costPerLiter).toLong())
        }
    }

    val handleSave: () -> Unit = {
        val km = PriceFormatter.cleanNumericString(mileageStr).toIntOrNull()
        val liters = parseLiters(litersStr)
        var hasError = false

        if (km == null || km <= 0) {
            mileageError = "کیلومتر فعلی را به‌صورت عدد صحیح وارد کنید."
            hasError = true
        }
        if (liters == null || liters <= 0.0) {
            litersError = "حجم سوخت باید بیشتر از صفر باشد."
            hasError = true
        }

        if (!hasError && km != null && liters != null) {
            val costPerLiter = PriceFormatter.parseCost(costPerLiterStr)
            val enteredTotal = PriceFormatter.parseCost(totalCostStr)
            val total = if (enteredTotal > 0L) enteredTotal else (liters * costPerLiter).toLong()
            onSave(
                selectedTimestamp,
                km,
                liters,
                costPerLiter,
                total,
                isFullTank,
                fuelType,
                gasStation.trim(),
                notes.trim()
            )
        }
    }

    AppDialog(
        title = if (isEditMode) "ویرایش سوخت‌گیری" else "ثبت سوخت‌گیری",
        onDismiss = onDismiss,
        confirmText = if (isEditMode) "ذخیره تغییرات" else "ثبت",
        onConfirm = handleSave
    ) {
        // 1. Refueling date (Jalali) - tap anywhere on the field to open the calendar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(4.dp))
        ) {
            OutlinedTextField(
                value = PersianDateHelper.formatJalali(selectedTimestamp),
                onValueChange = {},
                readOnly = true,
                enabled = false,
                label = { Text("تاریخ سوخت‌گیری") },
                singleLine = true,
                supportingText = {
                    Text(
                        text = PersianDateHelper.formatJalaliFull(selectedTimestamp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(
                            Icons.Default.DateRange,
                            contentDescription = "انتخاب تاریخ از تقویم",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface,
                    disabledBorderColor = MaterialTheme.colorScheme.outline,
                    disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                    disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                    disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { showDatePicker = true }
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            AssistChip(
                onClick = { selectedTimestamp = System.currentTimeMillis() },
                label = { Text("امروز") }
            )
            AssistChip(
                onClick = { selectedTimestamp = System.currentTimeMillis() - ONE_DAY_MILLIS },
                label = { Text("دیروز") }
            )
            AssistChip(
                onClick = { showDatePicker = true },
                label = { Text("انتخاب از تقویم") },
                leadingIcon = {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            )
        }

        // 2. Odometer
        OutlinedTextField(
            value = mileageStr,
            onValueChange = {
                mileageStr = PriceFormatter.toEnglishDigits(it)
                mileageError = null
            },
            label = { Text("کیلومتر فعلی") },
            singleLine = true,
            isError = mileageError != null,
            supportingText = { mileageError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // 3. Liters and price per liter
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = litersStr,
                onValueChange = {
                    litersStr = PriceFormatter.toEnglishDigits(it)
                    litersError = null
                    recalculateTotal(litersStr, costPerLiterStr)
                },
                label = { Text("حجم سوخت (لیتر)") },
                singleLine = true,
                isError = litersError != null,
                supportingText = { litersError?.let { Text(it, color = MaterialTheme.colorScheme.error) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = costPerLiterStr,
                onValueChange = {
                    costPerLiterStr = PriceFormatter.formatInputAsYouType(it)
                    recalculateTotal(litersStr, costPerLiterStr)
                },
                label = { Text("نرخ هر لیتر (تومان)") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f)
            )
        }

        // 4. Total cost
        OutlinedTextField(
            value = totalCostStr,
            onValueChange = { totalCostStr = PriceFormatter.formatInputAsYouType(it) },
            label = { Text("مبلغ کل سوخت (تومان)") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        // 5. Fuel type
        Text(
            text = "نوع سوخت:",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FUEL_TYPES.forEach { type ->
                FilterChip(
                    selected = fuelType == type,
                    onClick = { fuelType = type },
                    label = { Text(text = type, style = MaterialTheme.typography.labelMedium, maxLines = 1) }
                )
            }
        }

        // 6. Full tank
        Row(verticalAlignment = Alignment.CenterVertically) {
            Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
            Text("باک پر شده است", style = MaterialTheme.typography.bodyMedium)
        }

        // 7. Gas station and notes (optional)
        OutlinedTextField(
            value = gasStation,
            onValueChange = { gasStation = it },
            label = { Text("جایگاه سوخت (اختیاری)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = { Text("توضیحات (اختیاری)") },
            minLines = 2,
            maxLines = 4,
            modifier = Modifier.fillMaxWidth()
        )
    }

    if (showDatePicker) {
        PersianDatePickerDialog(
            initialTimestamp = selectedTimestamp,
            onDismiss = { showDatePicker = false },
            onConfirm = { newTimestamp ->
                selectedTimestamp = newTimestamp
                showDatePicker = false
            }
        )
    }
}

/** Parses a liters amount typed with Persian/English digits and either decimal separator. */
private fun parseLiters(input: String): Double? {
    val normalized = PriceFormatter.toEnglishDigits(input)
        .replace('٫', '.')
        .replace(',', '.')
        .trim()
    return normalized.toDoubleOrNull()
}

/** Shows whole numbers without a trailing ".0" (35.0 -> "35"). */
private fun formatLiters(liters: Double): String {
    return if (liters % 1.0 == 0.0) liters.toLong().toString() else liters.toString()
}
