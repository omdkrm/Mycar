package com.mycar.app.ui.screens

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
import com.mycar.app.data.model.Vehicle
import com.mycar.app.data.repository.ReminderItem
import com.mycar.app.data.repository.ReminderStatus
import com.mycar.app.ui.theme.*

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
        ReminderStatus.OVERDUE -> StatusRose to "نیازمند تعویض فوری"
        ReminderStatus.APPROACHING -> StatusAmber to "نزدیک موعد سرویس"
        ReminderStatus.HEALTHY -> StatusGreen to "وضعیت مطلوب"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.partName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(statusColor.copy(alpha = 0.15f))
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
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = if (item.kmRemaining <= 0) "کارکرد مازاد: ${formatNumber(Math.abs(item.kmRemaining))} کیلومتر"
                    else "باقیمانده تا سرویس: ${formatNumber(item.kmRemaining)} کیلومتر",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
                Text(
                    text = "دوره سرویس: هر ${formatNumber(item.schedule.kmInterval)} کیلومتر",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = {
                    val total = item.schedule.kmInterval.toFloat()
                    val done = (item.schedule.kmInterval - item.kmRemaining).toFloat()
                    (done / total).coerceIn(0f, 1f)
                },
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = statusColor,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}
