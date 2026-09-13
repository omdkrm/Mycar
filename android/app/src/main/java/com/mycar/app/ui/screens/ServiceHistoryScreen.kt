package com.mycar.app.ui.screens

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
import com.mycar.app.data.model.CatalogData
import com.mycar.app.data.model.ServiceRecord
import com.mycar.app.data.model.Vehicle
import com.mycar.app.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServiceHistoryScreen(
    activeVehicle: Vehicle?,
    services: List<ServiceRecord>,
    onAddService: (String, String, String?, String, Int, Long, Long, Long, Long, String, String, String, String) -> Unit,
    onDeleteService: (ServiceRecord) -> Unit
) {
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        floatingActionButton = {
            if (activeVehicle != null) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = CyanPrimary,
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
                Text("ابتدا یک خودرو را انتخاب یا ثبت کنید.")
            }
        } else if (services.isEmpty()) {
            EmptyServices(onAddClick = { showAddDialog = true }, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(services) { service ->
                    ServiceItemCard(service = service, onDelete = { onDeleteService(service) })
                }
            }
        }
    }

    if (showAddDialog && activeVehicle != null) {
        AddServiceDialog(
            defaultMileage = activeVehicle.currentMileage,
            onDismiss = { showAddDialog = false },
            onConfirm = { partName, catId, cat, km, cost, brand, center, notes ->
                onAddService(
                    activeVehicle.id,
                    partName,
                    catId,
                    cat,
                    km,
                    System.currentTimeMillis(),
                    cost,
                    cost,
                    0L,
                    brand,
                    center,
                    "",
                    notes
                )
                showAddDialog = false
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
        Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(64.dp), tint = CyanPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "هنوز سابقه سرویس ثبت نشده است.", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "تعویض روغن، فیلترها، شمع یا تعمیرات خود را ثبت کنید.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "ثبت اولین سرویس")
        }
    }
}

@Composable
fun ServiceItemCard(service: ServiceRecord, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "کیلومتر: ${formatNumber(service.mileage)} | هزینه: ${formatNumber(service.costTotal)} تومان",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                if (service.brand.isNotBlank() || service.serviceCenter.isNotBlank()) {
                    Text(
                        text = "برند: ${service.brand.ifBlank { "نامشخص" }} | تعمیرگاه: ${service.serviceCenter.ifBlank { "-" }}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف سرویس", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddServiceDialog(
    defaultMileage: Int,
    onDismiss: () -> Unit,
    onConfirm: (String, String?, String, Int, Long, String, String, String) -> Unit
) {
    var partName by remember { mutableStateOf("") }
    var selectedCatalogId by remember { mutableStateOf<String?>(null) }
    var category by remember { mutableStateOf("دوره‌ای") }
    var mileageStr by remember { mutableStateOf(defaultMileage.toString()) }
    var costStr by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var center by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت سرویس خودرو", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Predefined catalog picker
                Text("انتخاب از قطعات و سرویس‌های استاندارد:", style = MaterialTheme.typography.labelSmall)
                val catalogSample = CatalogData.items.take(4)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    catalogSample.forEach { item ->
                        SuggestionChip(
                            onClick = {
                                partName = item.name
                                selectedCatalogId = item.id
                                category = item.category
                            },
                            label = { Text(item.name, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                OutlinedTextField(
                    value = partName,
                    onValueChange = {
                        partName = it
                        selectedCatalogId = null
                    },
                    label = { Text("نام قطعه / سرویس") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = mileageStr,
                    onValueChange = { mileageStr = it },
                    label = { Text("کیلومتر هنگام سرویس") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = costStr,
                    onValueChange = { costStr = it },
                    label = { Text("هزینه کل (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = brand,
                        onValueChange = { brand = it },
                        label = { Text("برند قطعه") },
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = center,
                        onValueChange = { center = it },
                        label = { Text("نام تعمیرگاه") },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (partName.isNotBlank()) {
                        val km = mileageStr.toIntOrNull() ?: defaultMileage
                        val cost = costStr.toLongOrNull() ?: 0L
                        onConfirm(partName, selectedCatalogId, category, km, cost, brand, center, notes)
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
