package com.mycar.app.ui.screens

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
                        value = stats?.avgConsumptionLPer100Km?.let { "%.1f L/100km".format(it) } ?: "داده ناکافی",
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
            tint = CyanPrimary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "هنوز خودرویی ثبت نکرده‌اید.",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "برای شروع ثبت سرویس‌ها، سوخت و یادآوری‌ها، اولین خودروی خود را اضافه کنید.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondaryLight
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onAddVehicle,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = CyanPrimary)
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "ثبت اولین خودرو", style = MaterialTheme.typography.labelLarge)
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
        colors = CardDefaults.cardColors(containerColor = CyanPrimary)
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
                    color = Color.White
                )
                if (vehicle.model.isNotBlank() || vehicle.year.isNotBlank()) {
                    Text(
                        text = "${vehicle.model} - مدل ${vehicle.year}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }
                if (vehicle.plateNumber.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "پلاک: ${vehicle.plateNumber}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Icon(imageVector = icon, contentDescription = null, tint = CyanPrimary, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = CyanPrimary)
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun ReminderCard(item: ReminderItem, onClick: () -> Unit) {
    val statusColor = when (item.status) {
        ReminderStatus.OVERDUE -> StatusRose
        ReminderStatus.APPROACHING -> StatusAmber
        ReminderStatus.HEALTHY -> StatusGreen
    }
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = item.partName, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                val kmText = if (item.kmRemaining <= 0) "${formatNumber(Math.abs(item.kmRemaining))} کیلومتر گذشته" else "${formatNumber(item.kmRemaining)} کیلومتر مانده"
                Text(text = kmText, style = MaterialTheme.typography.bodySmall, color = statusColor)
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(statusColor.copy(alpha = 0.15f))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = if (item.status == ReminderStatus.OVERDUE) "موعد گذشته" else "نزدیک موعد",
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
