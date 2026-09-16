package com.mycar.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mycar.app.data.model.Vehicle
import com.mycar.app.data.repository.ReminderItem
import com.mycar.app.data.repository.ReminderStatus
import com.mycar.app.data.repository.VehicleStats
import com.mycar.app.data.util.FuelCalculator
import com.mycar.app.ui.theme.*
import java.text.NumberFormat
import java.util.Locale

@Composable
fun DashboardScreen(
    vehicles: List<Vehicle>,
    activeVehicle: Vehicle?,
    stats: VehicleStats?,
    reminders: List<ReminderItem>,
    onNavigateToVehicles: () -> Unit,
    onNavigateToServices: () -> Unit,
    onNavigateToFuel: () -> Unit,
    onNavigateToReminders: () -> Unit
) {
    if (vehicles.isEmpty() || activeVehicle == null) {
        EmptyDashboard(onAddVehicle = onNavigateToVehicles)
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Active Vehicle Card
            item {
                ActiveVehicleCard(
                    vehicle = activeVehicle,
                    onSwitchVehicle = onNavigateToVehicles
                )
            }

            // Quick Stats Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "کارکرد فعلی",
                        value = "${formatNumber(activeVehicle.currentMileage)} کیلومتر",
                        icon = Icons.Default.Speed,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "مجموع هزینه‌ها",
                        value = "${formatNumber(stats?.totalExpense ?: 0)} تومان",
                        icon = Icons.Default.AccountBalanceWallet,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    StatCard(
                        title = "میانگین مصرف",
                        value = FuelCalculator.formatConsumption(stats?.avgConsumptionLPer100Km),
                        icon = Icons.Default.LocalGasStation,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "هزینه هر کیلومتر",
                        value = stats?.costPerKm?.let { "${formatNumber(it.toLong())} ت" } ?: "نامشخص",
                        icon = Icons.Default.Timeline,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Quick Actions
            item {
                Text(
                    text = "دسترسی سریع",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionButton(
                        title = "ثبت سرویس",
                        icon = Icons.Default.Build,
                        onClick = onNavigateToServices,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "سوخت‌گیری",
                        icon = Icons.Default.LocalGasStation,
                        onClick = onNavigateToFuel,
                        modifier = Modifier.weight(1f)
                    )
                    QuickActionButton(
                        title = "یادآوری‌ها",
                        icon = Icons.Default.Notifications,
                        onClick = onNavigateToReminders,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Urgent Reminders
            val urgentReminders = reminders.filter { it.status != ReminderStatus.HEALTHY }.take(3)
            if (urgentReminders.isNotEmpty()) {
                item {
                    Text(
                        text = "هشدارهای سرویس",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                items(urgentReminders) { item ->
                    ReminderCard(item = item, onClick = onNavigateToReminders)
                }
            }
        }
    }
}

@Composable
fun EmptyDashboard(onAddVehicle: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.DirectionsCar,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "هنوز خودرویی ثبت نکرده‌اید.",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "برای شروع ثبت سرویس‌ها، سوخت و یادآوری‌ها، اولین خودروی خود را اضافه کنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddVehicle,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            )
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "ثبت اولین خودرو", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun ActiveVehicleCard(vehicle: Vehicle, onSwitchVehicle: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSwitchVehicle() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = Color.White
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (vehicle.model.isNotBlank() || vehicle.year.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${vehicle.model} - مدل ${vehicle.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.92f)
                    )
                }
                if (vehicle.plateNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "پلاک: ${vehicle.plateNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }
            IconButton(onClick = onSwitchVehicle) {
                Icon(
                    imageVector = Icons.Default.SwapHoriz,
                    contentDescription = "تعویض خودرو",
                    tint = Color.White
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun QuickActionButton(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ReminderCard(item: ReminderItem, onClick: () -> Unit) {
    val (statusColor, statusLabel) = when (item.status) {
        ReminderStatus.OVERDUE -> StatusRose to "نیازمند تعویض فوری"
        ReminderStatus.APPROACHING -> StatusAmber to "نزدیک موعد سرویس"
        ReminderStatus.HEALTHY -> StatusGreen to "وضعیت مطلوب"
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = item.partName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    val typeLabel = when (item.intervalType) {
                        com.mycar.app.data.model.ReminderIntervalType.TIME -> "زمانی"
                        com.mycar.app.data.model.ReminderIntervalType.MILEAGE -> "کیلومتری"
                        com.mycar.app.data.model.ReminderIntervalType.COMBINED -> "ترکیبی"
                        com.mycar.app.data.model.ReminderIntervalType.NONE -> ""
                    }
                    if (typeLabel.isNotBlank()) {
                        Text(
                            text = "($typeLabel)",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                val descriptionText = when (item.intervalType) {
                    com.mycar.app.data.model.ReminderIntervalType.TIME -> {
                        val dateStr = com.mycar.app.data.util.PersianDateHelper.formatJalali(item.dueDateTimestamp)
                        when {
                            item.daysRemaining < 0 -> "${formatNumber(Math.abs(item.daysRemaining))} روز گذشته (سررسید: $dateStr)"
                            item.daysRemaining == 0 -> "امروز سررسید است (تاریخ: $dateStr)"
                            else -> "${formatNumber(item.daysRemaining)} روز مانده (سررسید: $dateStr)"
                        }
                    }
                    com.mycar.app.data.model.ReminderIntervalType.MILEAGE -> {
                        if (item.kmRemaining <= 0) "${formatNumber(Math.abs(item.kmRemaining))} کیلومتر گذشته"
                        else "${formatNumber(item.kmRemaining)} کیلومتر مانده"
                    }
                    com.mycar.app.data.model.ReminderIntervalType.COMBINED -> {
                        val dateStr = com.mycar.app.data.util.PersianDateHelper.formatJalali(item.dueDateTimestamp)
                        when {
                            item.kmRemaining <= 0 && item.daysRemaining < 0 ->
                                "${formatNumber(Math.abs(item.kmRemaining))} کیلومتر و ${formatNumber(Math.abs(item.daysRemaining))} روز گذشته"
                            item.kmRemaining <= 0 ->
                                "${formatNumber(Math.abs(item.kmRemaining))} کیلومتر گذشته"
                            item.daysRemaining < 0 ->
                                "${formatNumber(Math.abs(item.daysRemaining))} روز گذشته (موعد: $dateStr)"
                            else ->
                                "${formatNumber(item.kmRemaining)} کیلومتر یا ${formatNumber(item.daysRemaining)} روز مانده"
                        }
                    }
                    com.mycar.app.data.model.ReminderIntervalType.NONE -> ""
                }
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.12f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = statusLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = statusColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

fun formatNumber(number: Long): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

fun formatNumber(number: Int): String {
    return NumberFormat.getNumberInstance(Locale.US).format(number)
}

