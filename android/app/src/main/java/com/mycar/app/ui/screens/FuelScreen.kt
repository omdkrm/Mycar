package com.mycar.app.ui.screens

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
import com.mycar.app.data.model.FuelRecord
import com.mycar.app.data.model.Vehicle
import com.mycar.app.ui.theme.CyanPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelScreen(
    activeVehicle: Vehicle?,
    fuelRecords: List<FuelRecord>,
    onAddFuelRecord: (String, Long, Int, Double, Long, Long, Boolean, String, String, String) -> Unit,
    onDeleteFuelRecord: (FuelRecord) -> Unit
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
                Text("ابتدا یک خودرو را انتخاب یا ثبت کنید.")
            }
        } else if (fuelRecords.isEmpty()) {
            EmptyFuel(onAddClick = { showAddDialog = true }, modifier = Modifier.padding(padding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(fuelRecords) { record ->
                    FuelItemCard(record = record, onDelete = { onDeleteFuelRecord(record) })
                }
            }
        }
    }

    if (showAddDialog && activeVehicle != null) {
        AddFuelDialog(
            defaultMileage = activeVehicle.currentMileage,
            onDismiss = { showAddDialog = false },
            onConfirm = { km, liters, costPerLiter, totalCost, isFull, fuelType, station, notes ->
                onAddFuelRecord(
                    activeVehicle.id,
                    System.currentTimeMillis(),
                    km,
                    liters,
                    costPerLiter,
                    totalCost,
                    isFull,
                    fuelType,
                    station,
                    notes
                )
                showAddDialog = false
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
        Icon(Icons.Default.LocalGasStation, contentDescription = null, modifier = Modifier.size(64.dp), tint = CyanPrimary)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "هنوز سابقه سوخت‌گیری ثبت نشده است.", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "برای محاسبه دقیق مصرف سوخت و هزینه‌ها، بنزین مصرفی را ثبت کنید.", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = onAddClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "ثبت اولین سوخت‌گیری")
        }
    }
}

@Composable
fun FuelItemCard(record: FuelRecord, onDelete: () -> Unit) {
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
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${record.liters} لیتر (${record.fuelType})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (record.isFullTank) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Badge(containerColor = CyanPrimary) {
                            Text("باک پر", color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "کیلومتر: ${formatNumber(record.mileage)} | هزینه: ${formatNumber(record.totalCost)} تومان",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                record.calculatedConsumptionLPer100Km?.let { consumption ->
                    Text(
                        text = "مصرف: %.1f لیتر در ۱۰۰ کیلومتر".format(consumption),
                        style = MaterialTheme.typography.labelSmall,
                        color = CyanPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.DeleteOutline, contentDescription = "حذف سوخت‌گیری", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddFuelDialog(
    defaultMileage: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Double, Long, Long, Boolean, String, String, String) -> Unit
) {
    var mileageStr by remember { mutableStateOf(defaultMileage.toString()) }
    var litersStr by remember { mutableStateOf("") }
    var costPerLiterStr by remember { mutableStateOf("3000") }
    var totalCostStr by remember { mutableStateOf("") }
    var isFullTank by remember { mutableStateOf(true) }
    var fuelType by remember { mutableStateOf("بنزین معمولی") }
    var gasStation by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("ثبت سوخت‌گیری", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = mileageStr,
                    onValueChange = { mileageStr = it },
                    label = { Text("کیلومتر فعلی") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = litersStr,
                        onValueChange = {
                            litersStr = it
                            val l = it.toDoubleOrNull() ?: 0.0
                            val cpl = costPerLiterStr.toLongOrNull() ?: 0L
                            if (l > 0 && cpl > 0) {
                                totalCostStr = (l * cpl).toLong().toString()
                            }
                        },
                        label = { Text("حجم سوخت (لیتر)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = costPerLiterStr,
                        onValueChange = {
                            costPerLiterStr = it
                            val l = litersStr.toDoubleOrNull() ?: 0.0
                            val cpl = it.toLongOrNull() ?: 0L
                            if (l > 0 && cpl > 0) {
                                totalCostStr = (l * cpl).toLong().toString()
                            }
                        },
                        label = { Text("نرخ هر لیتر (تومان)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = totalCostStr,
                    onValueChange = { totalCostStr = it },
                    label = { Text("مبلغ کل سوخت (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
                    Text("باک پر شده است", style = MaterialTheme.typography.bodyMedium)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val km = mileageStr.toIntOrNull() ?: defaultMileage
                    val l = litersStr.toDoubleOrNull() ?: 0.0
                    val cpl = costPerLiterStr.toLongOrNull() ?: 3000L
                    val total = totalCostStr.toLongOrNull() ?: (l * cpl).toLong()
                    if (l > 0) {
                        onConfirm(km, l, cpl, total, isFullTank, fuelType, gasStation, notes)
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
