package com.mycar.app.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycar.app.data.model.ReminderIntervalType
import com.mycar.app.data.model.Vehicle
import com.mycar.app.data.repository.ReminderItem
import com.mycar.app.data.repository.ReminderStatus
import com.mycar.app.data.util.PersianDateHelper
import com.mycar.app.ui.theme.*
import com.mycar.app.util.formatNumber

@Composable
fun RemindersScreen(
    activeVehicle: Vehicle?,
    reminders: List<ReminderItem>,
    onNavigateToServices: () -> Unit
) {
    if (activeVehicle == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ابتدا یک خودرو را انتخاب یا ثبت کنید.")
        }
    } else if (reminders.isEmpty()) {
        EmptyReminders()
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            item {
                Text(
                    text = "وضعیت سرویس‌های ${activeVehicle.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            items(reminders) { item ->
                FullReminderCard(item = item, onClick = onNavigateToServices)
            }
        }
    }
}

@Composable
fun EmptyReminders() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(64.dp), tint = StatusGreen)
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = "در حال حاضر یادآوری فعالی ندارید.", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "تمامی قطعات و سرویس‌های خودرو در وضعیت مناسبی قرار دارند.", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FullReminderCard(item: ReminderItem, onClick: () -> Unit) {
    val (statusColor, statusLabel) = when (item.status) {
        ReminderStatus.OVERDUE -> StatusRose to "نیازمند اقدام فوری"
        ReminderStatus.APPROACHING -> StatusAmber to "نزدیک موعد"
        ReminderStatus.HEALTHY -> StatusGreen to "وضعیت مطلوب"
    }

    val typeLabel = when (item.intervalType) {
        ReminderIntervalType.TIME -> "یادآوری زمانی"
        ReminderIntervalType.MILEAGE -> "یادآوری کیلومتری"
        ReminderIntervalType.COMBINED -> "یادآوری ترکیبی (کیلومتر یا زمان)"
        ReminderIntervalType.NONE -> "بدون یادآور"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = item.partName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
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

            Spacer(modifier = Modifier.height(10.dp))

            when (item.intervalType) {
                ReminderIntervalType.TIME -> {
                    val dateStr = PersianDateHelper.formatJalali(item.dueDateTimestamp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = when {
                                item.daysRemaining < 0 -> "${formatNumber(Math.abs(item.daysRemaining))} روز گذشته از موعد"
                                item.daysRemaining == 0 -> "امروز سررسید است"
                                else -> "${formatNumber(item.daysRemaining)} روز باقی‌مانده تا سررسید"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "سررسید: $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "دوره یادآوری: هر ${item.schedule.timeIntervalMonths} ماه",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                ReminderIntervalType.MILEAGE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (item.kmRemaining <= 0) "کارکرد مازاد: ${formatNumber(Math.abs(item.kmRemaining))} کیلومتر"
                            else "باقیمانده تا سرویس: ${formatNumber(item.kmRemaining)} کیلومتر",
                            style = MaterialTheme.typography.bodySmall,
                            color = statusColor,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "دوره: هر ${formatNumber(item.schedule.kmInterval)} کیلومتر",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ReminderIntervalType.COMBINED -> {
                    val dateStr = PersianDateHelper.formatJalali(item.dueDateTimestamp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (item.kmRemaining <= 0) "${formatNumber(Math.abs(item.kmRemaining))} کیلومتر گذشته"
                            else "${formatNumber(item.kmRemaining)} کیلومتر مانده",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.kmRemaining <= 0) StatusRose else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = when {
                                item.daysRemaining < 0 -> "${formatNumber(Math.abs(item.daysRemaining))} روز گذشته"
                                item.daysRemaining == 0 -> "سررسید امروز"
                                else -> "${formatNumber(item.daysRemaining)} روز مانده"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = if (item.daysRemaining < 0) StatusRose else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "دوره: هر ${formatNumber(item.schedule.kmInterval)} کیلومتر یا ${item.schedule.timeIntervalMonths} ماه",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "سررسید: $dateStr",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                ReminderIntervalType.NONE -> {}
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress indicator
            LinearProgressIndicator(
                progress = {
                    when (item.intervalType) {
                        ReminderIntervalType.TIME -> {
                            val totalDays = (item.schedule.timeIntervalMonths * 30.4).coerceAtLeast(1.0)
                            val daysPassed = totalDays - item.daysRemaining
                            (daysPassed / totalDays).toFloat().coerceIn(0f, 1f)
                        }
                        ReminderIntervalType.MILEAGE -> {
                            val total = item.schedule.kmInterval.toFloat().coerceAtLeast(1f)
                            val done = (item.schedule.kmInterval - item.kmRemaining).toFloat()
                            (done / total).coerceIn(0f, 1f)
                        }
                        ReminderIntervalType.COMBINED -> {
                            val totalKm = item.schedule.kmInterval.toFloat().coerceAtLeast(1f)
                            val doneKm = (item.schedule.kmInterval - item.kmRemaining).toFloat()
                            val kmProg = (doneKm / totalKm).coerceIn(0f, 1f)

                            val totalDays = (item.schedule.timeIntervalMonths * 30.4).coerceAtLeast(1.0)
                            val daysPassed = totalDays - item.daysRemaining
                            val timeProg = (daysPassed / totalDays).toFloat().coerceIn(0f, 1f)

                            maxOf(kmProg, timeProg)
                        }
                        ReminderIntervalType.NONE -> 0f
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
