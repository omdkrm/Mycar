package com.mycar.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mycar.app.data.model.CatalogData
import com.mycar.app.data.model.ServiceRecord
import com.mycar.app.data.model.Vehicle
import com.mycar.app.data.util.PersianDateHelper
import com.mycar.app.data.util.PriceFormatter
import com.mycar.app.ui.components.PersianDatePickerDialog
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceHistoryScreen(
    activeVehicle: Vehicle?,
    services: List<ServiceRecord>,
    onAddService: (String, String, String?, String, Int, Long, Long, Long, Long, String, String, String, String) -> Unit,
    onUpdateService: (ServiceRecord) -> Unit = {},
    onDeleteService: (ServiceRecord) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }
    var serviceToEdit by remember { mutableStateOf<ServiceRecord?>(null) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            if (activeVehicle != null) {
                FloatingActionButton(
                    onClick = {
                        serviceToEdit = null
                        showDialog = true
                    },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "ثبت سرویس")
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
        } else if (services.isEmpty()) {
            EmptyServices(
                onAddClick = {
                    serviceToEdit = null
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
                items(services, key = { it.id }) { service ->
                    ServiceItemCard(
                        service = service,
                        onEdit = {
                            serviceToEdit = service
                            showDialog = true
                        },
                        onDelete = { onDeleteService(service) }
                    )
                }
            }
        }
    }

    if (showDialog && activeVehicle != null) {
        ServiceFormDialog(
            initialService = serviceToEdit,
            defaultMileage = activeVehicle.currentMileage,
            onDismiss = {
                showDialog = false
                serviceToEdit = null
            },
            onSave = { partName, catId, cat, km, dateTimestamp, cost, brand, center, notes ->
                val currentEdit = serviceToEdit
                if (currentEdit != null) {
                    val updated = currentEdit.copy(
                        partName = partName,
                        catalogItemId = catId,
                        serviceCategory = cat,
                        mileage = km,
                        dateTimestamp = dateTimestamp,
                        costTotal = cost,
                        partCost = cost,
                        laborCost = 0L,
                        brand = brand,
                        serviceCenter = center,
                        notes = notes
                    )
                    onUpdateService(updated)
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("تغییرات با موفقیت ذخیره شد.")
                    }
                } else {
                    onAddService(
                        activeVehicle.id,
                        partName,
                        catId,
                        cat,
                        km,
                        dateTimestamp,
                        cost,
                        cost,
                        0L,
                        brand,
                        center,
                        "",
                        notes
                    )
                    coroutineScope.launch {
                        snackbarHostState.showSnackbar("سرویس با موفقیت ثبت شد.")
                    }
                }
                showDialog = false
                serviceToEdit = null
            }
        )
    }
}

@Composable
fun EmptyServices(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Build,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "هنوز سابقه سرویس ثبت نشده است.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "تعویض روغن، فیلترها، شمع یا تعمیرات خود را ثبت کنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "ثبت اولین سرویس", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ServiceItemCard(
    service: ServiceRecord,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
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
                Text(
                    text = service.partName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "تاریخ: ${PersianDateHelper.formatJalali(service.dateTimestamp)} | کیلومتر: ${PriceFormatter.formatWithSeparators(service.mileage)} | هزینه: ${PriceFormatter.formatCostWithUnit(service.costTotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (service.brand.isNotBlank() || service.serviceCenter.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "برند: ${service.brand.ifBlank { "نامشخص" }} | تعمیرگاه: ${service.serviceCenter.ifBlank { "-" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (service.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "توضیحات: ${service.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Edit,
                        contentDescription = "ویرایش",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.DeleteOutline,
                        contentDescription = "حذف سرویس",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceFormDialog(
    initialService: ServiceRecord? = null,
    defaultMileage: Int,
    onDismiss: () -> Unit,
    onSave: (
        partName: String,
        selectedCatalogId: String?,
        category: String,
        mileage: Int,
        dateTimestamp: Long,
        costTotal: Long,
        brand: String,
        center: String,
        notes: String
    ) -> Unit
) {
    val isEditMode = initialService != null

    var partName by remember { mutableStateOf(initialService?.partName ?: "") }
    var selectedCatalogId by remember { mutableStateOf(initialService?.catalogItemId) }
    var category by remember { mutableStateOf(initialService?.serviceCategory ?: "دوره‌ای") }
    var selectedTimestamp by remember {
        mutableStateOf(initialService?.dateTimestamp ?: System.currentTimeMillis())
    }
    var showDatePicker by remember { mutableStateOf(false) }
    var mileageStr by remember {
        mutableStateOf((initialService?.mileage ?: defaultMileage).toString())
    }
    var costStr by remember {
        mutableStateOf(
            initialService?.let { PriceFormatter.formatWithSeparators(it.costTotal) } ?: ""
        )
    }
    var brand by remember { mutableStateOf(initialService?.brand ?: "") }
    var center by remember { mutableStateOf(initialService?.serviceCenter ?: "") }
    var notes by remember { mutableStateOf(initialService?.notes ?: "") }

    var partNameError by remember { mutableStateOf<String?>(null) }
    var dateError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "ویرایش هزینه و سرویس" else "ثبت سرویس خودرو",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Predefined catalog picker - horizontally scrollable chip row
                Text(
                    text = "انتخاب از قطعات و سرویس‌های استاندارد:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                val catalogSample = CatalogData.items.take(6)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    catalogSample.forEach { item ->
                        SuggestionChip(
                            onClick = {
                                partName = item.name
                                selectedCatalogId = item.id
                                category = item.category
                                partNameError = null
                            },
                            label = {
                                Text(
                                    text = item.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    maxLines = 1
                                )
                            },
                            colors = SuggestionChipDefaults.suggestionChipColors(
                                containerColor = if (selectedCatalogId == item.id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                labelColor = if (selectedCatalogId == item.id) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                            ),
                            border = SuggestionChipDefaults.suggestionChipBorder(
                                enabled = true,
                                borderColor = if (selectedCatalogId == item.id) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                }

                // 1. Part / Service Name
                OutlinedTextField(
                    value = partName,
                    onValueChange = {
                        partName = it
                        selectedCatalogId = null
                        if (it.isNotBlank()) partNameError = null
                    },
                    label = { Text("نام قطعه / سرویس") },
                    singleLine = true,
                    isError = partNameError != null,
                    supportingText = partNameError?.let { { Text(it, color = MaterialTheme.colorScheme.error) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // 2. Date Field (Persian/Jalali date) - Interactive Tap to Open Calendar
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
                        label = { Text("تاریخ") },
                        placeholder = { Text("مثال: ۱۴۰۳/۰۶/۲۵") },
                        singleLine = true,
                        isError = dateError != null,
                        supportingText = {
                            if (dateError != null) {
                                Text(dateError!!, color = MaterialTheme.colorScheme.error)
                            } else {
                                Text(
                                    text = PersianDateHelper.formatJalaliFull(selectedTimestamp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
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
                            disabledBorderColor = if (dateError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.outline,
                            disabledLabelColor = if (dateError != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            disabledLeadingIconColor = MaterialTheme.colorScheme.primary,
                            disabledTrailingIconColor = MaterialTheme.colorScheme.primary,
                            disabledSupportingTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Full-width clickable overlay to capture tap anywhere on the date field
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showDatePicker = true }
                    )
                }

                // Quick Date Helpers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {
                            selectedTimestamp = System.currentTimeMillis()
                            dateError = null
                        },
                        label = { Text("امروز") }
                    )
                    AssistChip(
                        onClick = {
                            selectedTimestamp = System.currentTimeMillis() - 86400000L
                            dateError = null
                        },
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

                // 3. Mileage at service
                OutlinedTextField(
                    value = mileageStr,
                    onValueChange = { mileageStr = PriceFormatter.toEnglishDigits(it) },
                    label = { Text("کیلومتر هنگام سرویس") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Price / Cost Field (Thousands separators while typing)
                OutlinedTextField(
                    value = costStr,
                    onValueChange = {
                        costStr = PriceFormatter.formatInputAsYouType(it)
                    },
                    label = { Text("هزینه کل (تومان)") },
                    placeholder = { Text("مثال: ۱۵۰,۰۰۰") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    supportingText = {
                        val parsed = PriceFormatter.parseCost(costStr)
                        if (parsed > 0) {
                            Text(
                                PriceFormatter.formatCostWithUnit(parsed),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text("مبلغ به تومان بدون علامت منفی")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                // 5. Brand
                OutlinedTextField(
                    value = brand,
                    onValueChange = { brand = it },
                    label = { Text("برند قطعه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 6. Workshop / Service Center
                OutlinedTextField(
                    value = center,
                    onValueChange = { center = it },
                    label = { Text("نام تعمیرگاه") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 7. Description / Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("توضیحات") },
                    placeholder = { Text("مثال: تعویض روغن و فیلتر، بررسی سطح مایعات") },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    var hasError = false
                    if (partName.isBlank()) {
                        partNameError = "نام قطعه یا سرویس نمی‌تواند خالی باشد"
                        hasError = true
                    }
                    if (!hasError) {
                        val km = PriceFormatter.cleanNumericString(mileageStr).toIntOrNull() ?: defaultMileage
                        val cost = PriceFormatter.parseCost(costStr)
                        onSave(
                            partName.trim(),
                            selectedCatalogId,
                            category,
                            km,
                            selectedTimestamp,
                            cost,
                            brand.trim(),
                            center.trim(),
                            notes.trim()
                        )
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text(
                    text = if (isEditMode) "ذخیره تغییرات" else "ثبت",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )

    if (showDatePicker) {
        PersianDatePickerDialog(
            initialTimestamp = selectedTimestamp,
            onDismiss = { showDatePicker = false },
            onConfirm = { newTimestamp ->
                selectedTimestamp = newTimestamp
                showDatePicker = false
                dateError = null
            }
        )
    }
}
