package com.mycar.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycar.app.ui.theme.CyanPrimary

@Composable
fun SettingsScreen(
    onClearAllData: () -> Unit
) {
    var showConfirmClearDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "تنظیمات و اطلاعات برنامه",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("مشخصات فنی نرم‌افزار", style = MaterialTheme.typography.titleSmall, color = CyanPrimary)
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نام برنامه:")
                        Text("My Car (خودروی من)", fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("بسته نرم‌افزاری (Package):")
                        Text("com.mycar.app", fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("قلم برنامه (Font):")
                        Text("Vazirmatn (وزیرمتن فارسی)", fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("پایگاه داده:")
                        Text("Room SQLite (کاملاً آفلاین)", fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("نسخه:")
                        Text("1.0.0 (نسخه نهایی)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("مدیریت داده‌ها", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "پاکسازی کلی داده‌ها برای بازگرداندن پایگاه داده به حالت نصب اولیه و خالی.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { showConfirmClearDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("پاکسازی کامل پایگاه داده")
                    }
                }
            }
        }
    }

    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("تایید حذف اطلاعات") },
            text = { Text("آیا اطمینان دارید؟ تمامی اطلاعات خودروها، سرویس‌ها و هزینه‌ها حذف خواهند شد و پایگاه داده خالی می‌شود.") },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("بله، پاکسازی شود")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("انصراف")
                }
            }
        )
    }
}
