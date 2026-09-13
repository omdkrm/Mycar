package com.mycar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mycar.app.data.model.Vehicle
import com.mycar.app.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VehiclesScreen(
    vehicles: List<Vehicle>,
    activeVehicle: Vehicle?,
    onSelectVehicle: (Vehicle) -> Unit,
    onAddVehicle: (String, String, String, String, Int, Double, String, String) -> Unit,
    onDeleteVehicle: (Vehicle) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

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
                        onDelete = { onDeleteVehicle(vehicle) }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        AddVehicleDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { name, model, year, plate, km, capacity, fuelType, color ->
                onAddVehicle(name, model, year, plate, km, capacity, fuelType, color)
                showAddDialog = false
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
        Icon(Icons.Default.DirectionsCar, contentDescription = null, modifier = Modifier.size(64.dp), tint = CyanPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "هنوز خودرویی ثبت نکرده‌اید.", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "برای شروع ثبت هزینه‌ها، خودروی خود را تعریف کنید.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "ثبت اولین خودرو")
        }
    }
}

@Composable
fun VehicleItemCard(
    vehicle: Vehicle,
    isSelected: Boolean,
    onSelect: () -> Unit,
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
                        fontWeight = FontWeight.Bold
                    )
                    if (isSelected) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Badge(containerColor = CyanPrimary) {
                            Text("خودروی فعال", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${vehicle.model} - سال ${vehicle.year} | ${formatNumber(vehicle.currentMileage)} کیلومتر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (vehicle.plateNumber.isNotBlank()) {
                    Text(
                        text = "پلاک: ${vehicle.plateNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف خودرو", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddVehicleDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, String, String, Int, Double, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var plate by remember { mutableStateOf("") }
    var mileageStr by remember { mutableStateOf("") }
    var capacityStr by remember { mutableStateOf("50") }
    var fuelType by remember { mutableStateOf("بنزین") }
    var color by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "ثبت خودرو جدید", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام خودرو (مثال: سمند EF7)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = model,
                    onValueChange = { model = it },
                    label = { Text("تیپ / مدل") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = year,
                        onValueChange = { year = it },
                        label = { Text("سال ساخت") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = mileageStr,
                        onValueChange = { mileageStr = it },
                        label = { Text("کارکرد (کیلومتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = plate,
                    onValueChange = { plate = it },
                    label = { Text("شماره پلاک") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val km = mileageStr.toIntOrNull() ?: 0
                        val cap = capacityStr.toDoubleOrNull() ?: 50.0
                        onConfirm(name, model, year, plate, km, cap, fuelType, color)
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
            ) {
                Text("ثبت")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        }
    )
}
