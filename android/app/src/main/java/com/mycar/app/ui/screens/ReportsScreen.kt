package com.mycar.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycar.app.data.model.Vehicle
import com.mycar.app.data.repository.VehicleStats
import com.mycar.app.ui.theme.CyanDark
import com.mycar.app.ui.theme.CyanPrimary

@Composable
fun ReportsScreen(
    activeVehicle: Vehicle?,
    stats: VehicleStats?
) {
    if (activeVehicle == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("ابتدا یک خودرو را انتخاب یا ثبت کنید.")
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "گزارش مالی و عملکرد ${activeVehicle.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CyanPrimary)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(text = "مجموع کل مخارج خودرو", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "${formatNumber(stats?.totalExpense ?: 0)} تومان",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            item {
                Text(text = "تفکیک هزینه‌ها", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                val serviceTotal = stats?.totalServiceCost ?: 0L
                val fuelTotal = stats?.totalFuelCost ?: 0L
                val grandTotal = (serviceTotal + fuelTotal).coerceAtLeast(1L).toFloat()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("هزینه سرویس و قطعات:")
                            Text("${formatNumber(serviceTotal)} تومان (${(serviceTotal / grandTotal * 100).toInt()}%)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { serviceTotal / grandTotal },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = CyanPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("هزینه سوخت (بنزین):")
                            Text("${formatNumber(fuelTotal)} تومان (${(fuelTotal / grandTotal * 100).toInt()}%)", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { fuelTotal / grandTotal },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                            color = CyanDark,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }

            item {
                Text(text = "شاخص‌های اقتصادی", style = MaterialTheme.typography.titleSmall)
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("میانگین مصرف سوخت:")
                            Text(
                                text = stats?.avgConsumptionLPer100Km?.let { "%.1f L/100km".format(it) } ?: "داده ناکافی",
                                fontWeight = FontWeight.Bold,
                                color = CyanPrimary
                            )
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مجموع بنزین مصرفی:")
                            Text("${stats?.totalLiters ?: 0.0} لیتر", fontWeight = FontWeight.Bold)
                        }
                        Divider()
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("مسافت تحت بررسی:")
                            Text("${formatNumber(stats?.totalDistanceDrivenKm ?: 0)} کیلومتر", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}
