package com.mycar.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mycar.app.data.model.Vehicle
import com.mycar.app.ui.components.IranPlate
import com.mycar.app.ui.components.IranPlateBadge
import com.mycar.app.ui.components.IranPlateInput
import com.mycar.app.ui.components.IranPlateParser
import com.mycar.app.ui.components.PlateType
import com.mycar.app.ui.theme.CyanPrimary
import com.mycar.app.util.formatNumber

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    vehicles: List<Vehicle>,
    activeVehicle: Vehicle?,
    onSelectVehicle: (Vehicle) -> Unit,
    onAddVehicle: (String, String, String, String, Int, Double, String, String) -> Unit,
    onUpdateVehicle: (Vehicle) -> Unit = {},
    onDeleteVehicle: (Vehicle) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var vehicleToEdit by remember { mutableStateOf<Vehicle?>(null) }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = CyanPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = "افزودن خودرو")
            }
        }
    ) { padding ->
        if (vehicles.isEmpty()) {
            EmptyVehicles(onAddClick = { showAddDialog = true }, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(vehicles) { vehicle ->
                    val isSelected = vehicle.id == activeVehicle?.id
                    VehicleItemCard(
                        vehicle = vehicle,
                        isSelected = isSelected,
                        onSelect = { onSelectVehicle(vehicle) },
                        onEdit = { vehicleToEdit = vehicle },
                        onDelete = { onDeleteVehicle(vehicle) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        VehicleFormDialog(
            vehicleToEdit = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, model, year, plate, km, capacity, fuelType, color ->
                onAddVehicle(name, model, year, plate, km, capacity, fuelType, color)
                showAddDialog = false
            }
        )
    }

    vehicleToEdit?.let { vehicle ->
        VehicleFormDialog(
            vehicleToEdit = vehicle,
            onDismiss = { vehicleToEdit = null },
            onSave = { name, model, year, plate, km, capacity, fuelType, color ->
                onUpdateVehicle(
                    vehicle.copy(
                        name = name,
                        model = model,
                        year = year,
                        plateNumber = plate,
                        currentMileage = km,
                        fuelCapacityLiters = capacity,
                        fuelType = fuelType,
                        color = color
                    )
                )
                vehicleToEdit = null
            }
        )
    }
}

@Composable
fun EmptyVehicles(onAddClick: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "هنوز خودرویی ثبت نکرده‌اید.", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "برای شروع ثبت هزینه‌ها، خودروی خود را تعریف کنید.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary, contentColor = Color.White)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "ثبت اولین خودرو", fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun VehicleItemCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
        )
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
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = MaterialTheme.colorScheme.primary) {
                            Text("خودروی فعال", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                // Note: Manufacturing year must NOT receive thousands separators; mileage uses formatNumber
                Text(
                    text = "${vehicle.model} - سال ساخت ${vehicle.year} | ${formatNumber(vehicle.currentMileage)} کیلومتر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (vehicle.plateNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    IranPlateBadge(plateNumber = vehicle.plateNumber)
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onEdit) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "ویرایش خودرو",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "حذف خودرو",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Dialog for adding or editing a vehicle.
 * LocalLayoutDirection = LayoutDirection.Rtl is explicitly applied at the root
 * and across all dialog slots to guarantee RTL presentation from the very first frame without flicker.
 */
@Composable
fun VehicleFormDialog(
    vehicleToEdit: Vehicle?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String, Int, Double, String, String) -> Unit
) {
    val isEditing = vehicleToEdit != null

    var name by remember { mutableStateOf(vehicleToEdit?.name ?: "") }
    var model by remember { mutableStateOf(vehicleToEdit?.model ?: "") }
    var year by remember { mutableStateOf(vehicleToEdit?.year ?: "") }
    var mileageStr by remember { mutableStateOf(vehicleToEdit?.currentMileage?.toString() ?: "") }
    var capacityStr by remember { mutableStateOf(vehicleToEdit?.fuelCapacityLiters?.toInt()?.toString() ?: "50") }
    var fuelType by remember { mutableStateOf(vehicleToEdit?.fuelType ?: "بنزین") }
    var color by remember { mutableStateOf(vehicleToEdit?.color ?: "") }

    // Parse stored plate or initialize a new one
    val initialParsedPlate = remember(vehicleToEdit?.plateNumber) {
        vehicleToEdit?.plateNumber?.let { IranPlateParser.parse(it) }
    }
    val existingUnparsedPlate = remember(vehicleToEdit?.plateNumber) {
        if (vehicleToEdit != null && vehicleToEdit.plateNumber.isNotBlank() && initialParsedPlate == null) {
            vehicleToEdit.plateNumber
        } else {
            ""
        }
    }

    var iranPlate by remember {
        mutableStateOf(
            initialParsedPlate ?: IranPlate(
                threeDigits = "",
                letter = "ب",
                twoDigits = "",
                provinceCode = "",
                type = PlateType.PERSONAL
            )
        )
    }

    var validationError by remember { mutableStateOf<String?>(null) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Text(
                        text = if (isEditing) "ویرایش خودرو" else "ثبت خودرو جدید",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            text = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = name,
                            onValueChange = {
                                name = it
                                validationError = null
                            },
                            label = { Text("نام خودرو (مثال: سمند EF7)") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = model,
                            onValueChange = { model = it },
                            label = { Text("تیپ / مدل") },
                            singleLine = true,
                            textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Rtl),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = year,
                                onValueChange = { input ->
                                    val digitsOnly = IranPlateParser.toEnglishDigits(input).filter { it.isDigit() }
                                    if (digitsOnly.length <= 4) {
                                        year = digitsOnly
                                    }
                                },
                                label = { Text("سال ساخت") },
                                placeholder = { Text("مثال: 1402") },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = mileageStr,
                                onValueChange = { input ->
                                    val digitsOnly = IranPlateParser.toEnglishDigits(input).filter { it.isDigit() }
                                    mileageStr = digitsOnly
                                },
                                label = { Text("کارکرد (کیلومتر)") },
                                placeholder = { Text("مثال: 45000") },
                                singleLine = true,
                                textStyle = LocalTextStyle.current.copy(textDirection = TextDirection.Ltr),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // Structured Iranian National License Plate Component
                        Divider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        if (existingUnparsedPlate.isNotBlank() && !iranPlate.isPartiallyFilled) {
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "پلاک فعلی: $existingUnparsedPlate",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "(قالب قدیمی)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }

                        IranPlateInput(
                            plate = iranPlate,
                            onPlateChange = {
                                iranPlate = it
                                validationError = null
                            }
                        )

                        if (validationError != null) {
                            Text(
                                text = validationError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            },
            confirmButton = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    Button(
                        onClick = {
                            if (name.isBlank()) {
                                validationError = "لطفاً نام خودرو را وارد کنید."
                                return@Button
                            }

                            // Validate Iranian License Plate
                            val finalPlate: String
                            if (iranPlate.isPartiallyFilled) {
                                if (!iranPlate.isValid) {
                                    validationError = "لطفاً تمام بخش‌های پلاک ملی (۳ رقم، ۱ حرف، ۲ رقم و کد استان) را به طور کامل وارد نمایید."
                                    return@Button
                                }
                                finalPlate = iranPlate.toNormalizedString()
                            } else if (existingUnparsedPlate.isNotBlank()) {
                                // Keep old unparsed plate safe if untouched
                                finalPlate = existingUnparsedPlate
                            } else {
                                finalPlate = ""
                            }

                            val km = mileageStr.toIntOrNull() ?: 0
                            val cap = capacityStr.toDoubleOrNull() ?: 50.0
                            onSave(name.trim(), model.trim(), year.trim(), finalPlate, km, cap, fuelType, color.trim())
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(if (isEditing) "ذخیره تغییرات" else "ثبت خودرو", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    TextButton(onClick = onDismiss) {
                        Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        )
    }
}
