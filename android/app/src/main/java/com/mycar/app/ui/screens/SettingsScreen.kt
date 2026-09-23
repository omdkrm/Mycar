package com.mycar.app.ui.screens

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mycar.app.data.backup.BackupSerializer
import com.mycar.app.data.backup.RestoreResult
import com.mycar.app.data.backup.ValidationResult
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsScreen(
    onClearAllData: () -> Unit,
    onCreateBackup: suspend () -> String = { "" },
    onWriteBackupToUri: suspend (Context, Uri, String) -> Boolean = { _, _, _ -> false },
    onReadBackupFromUri: suspend (Context, Uri) -> String? = { _, _ -> null },
    onRestoreBackup: suspend (String) -> RestoreResult = { RestoreResult.InvalidFile }
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showConfirmClearDialog by remember { mutableStateOf(false) }
    var showConfirmRestoreDialog by remember { mutableStateOf(false) }
    var pendingRestoreJson by remember { mutableStateOf<String?>(null) }
    var feedbackMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    // SAF Document Creator for export
    val createDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val json = onCreateBackup()
                    val success = onWriteBackupToUri(context, uri, json)
                    feedbackMessage = if (success) {
                        "نسخه پشتیبان با موفقیت ذخیره شد."
                    } else {
                        "ذخیره نسخه پشتیبان انجام نشد."
                    }
                } catch (e: Exception) {
                    feedbackMessage = "ذخیره نسخه پشتیبان انجام نشد."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // SAF Document Opener for import
    val openDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            isLoading = true
            coroutineScope.launch {
                try {
                    val json = onReadBackupFromUri(context, uri)
                    if (json.isNullOrBlank()) {
                        feedbackMessage = "فایل نسخه پشتیبان معتبر نیست."
                        return@launch
                    }

                    // Strict pre-validation before altering or confirming anything
                    when (val validation = BackupSerializer.deserializeAndValidate(json)) {
                        is ValidationResult.UnsupportedSchema -> {
                            feedbackMessage = "نسخه این فایل پشتیبان با نسخه فعلی برنامه سازگار نیست."
                        }
                        is ValidationResult.MalformedFile -> {
                            feedbackMessage = "فایل نسخه پشتیبان معتبر نیست."
                        }
                        is ValidationResult.Success -> {
                            // Hold validated JSON in memory and prompt user for confirmation
                            pendingRestoreJson = json
                            showConfirmRestoreDialog = true
                        }
                    }
                } catch (e: Exception) {
                    feedbackMessage = "فایل نسخه پشتیبان معتبر نیست."
                } finally {
                    isLoading = false
                }
            }
        }
    }

    // Observe feedback messages and trigger toast & snackbar
    LaunchedEffect(feedbackMessage) {
        feedbackMessage?.let { msg ->
            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            snackbarHostState.showSnackbar(msg)
            feedbackMessage = null
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "تنظیمات و اطلاعات برنامه",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Section 1: Backup and Restore (پشتیبان‌گیری و بازیابی)
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Backup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "پشتیبان‌گیری و بازیابی",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Text(
                            text = "تمامی اطلاعات خودروها، سوابق سرویس‌ها، سوخت‌گیری‌ها، قطعات و بازه‌های زمانی را به صورت فایل آفلاین JSON ذخیره یا بازیابی کنید.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Divider(color = MaterialTheme.colorScheme.outlineVariant)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            // Export Button (ایجاد نسخه پشتیبان)
                            Button(
                                onClick = {
                                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                                    val defaultFileName = "MyCar_Backup_${dateFormat.format(Date())}.json"
                                    createDocumentLauncher.launch(defaultFileName)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = Color.White
                                ),
                                enabled = !isLoading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Backup,
                                        contentDescription = "ایجاد نسخه پشتیبان",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "ایجاد نسخه پشتیبان",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Import Button (بازیابی نسخه پشتیبان)
                            OutlinedButton(
                                onClick = {
                                    openDocumentLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .defaultMinSize(minHeight = 44.dp),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.primary
                                ),
                                enabled = !isLoading,
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Restore,
                                        contentDescription = "بازیابی نسخه پشتیبان",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "بازیابی نسخه پشتیبان",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Section 2: Technical Specifications
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("مشخصات فنی نرم‌افزار", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نام برنامه:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("My Car (خودروی من)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("بسته نرم‌افزاری (Package):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("com.mycar.app", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("قلم برنامه (Font):", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Vazirmatn (وزیرمتن فارسی)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("پایگاه داده:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Room SQLite (کاملاً آفلاین)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Divider(color = MaterialTheme.colorScheme.outline)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("نسخه:", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("1.0.0 (نسخه نهایی)", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }

            // Section 3: Data Management / Clear
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("مدیریت داده‌ها", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "پاکسازی کلی داده‌ها برای بازگرداندن پایگاه داده به حالت نصب اولیه و خالی.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = { showConfirmClearDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("پاکسازی کامل پایگاه داده", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Snackbar Host for status notifications
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        )
    }

    // Confirmation Dialog for Restore
    if (showConfirmRestoreDialog) {
        AlertDialog(
            onDismissRequest = {
                showConfirmRestoreDialog = false
                pendingRestoreJson = null
            },
            title = {
                Text(
                    text = "بازیابی نسخه پشتیبان",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "با بازیابی نسخه پشتیبان، اطلاعات فعلی برنامه جایگزین میشود. آیا مطمئن هستید؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val json = pendingRestoreJson
                        showConfirmRestoreDialog = false
                        pendingRestoreJson = null
                        if (json != null) {
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    val result = onRestoreBackup(json)
                                    feedbackMessage = when (result) {
                                        is RestoreResult.Success -> "بازیابی نسخه پشتیبان با موفقیت انجام شد."
                                        is RestoreResult.InvalidFile -> "فایل نسخه پشتیبان معتبر نیست."
                                        is RestoreResult.UnsupportedSchema -> "نسخه این فایل پشتیبان با نسخه فعلی برنامه سازگار نیست."
                                        is RestoreResult.Failure -> "بازیابی انجام نشد و اطلاعات فعلی بدون تغییر باقی ماند."
                                    }
                                } catch (e: Exception) {
                                    feedbackMessage = "بازیابی انجام نشد و اطلاعات فعلی بدون تغییر باقی ماند."
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("بازیابی", fontWeight = FontWeight.Bold, color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showConfirmRestoreDialog = false
                        pendingRestoreJson = null
                    }
                ) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }

    // Confirmation Dialog for Clear All
    if (showConfirmClearDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmClearDialog = false },
            title = { Text("تایید حذف اطلاعات", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("آیا اطمینان دارید؟ تمامی اطلاعات خودروها، سرویس‌ها و هزینه‌ها حذف خواهند شد و پایگاه داده خالی می‌شود.", color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                Button(
                    onClick = {
                        onClearAllData()
                        showConfirmClearDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("بله، پاکسازی شود", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmClearDialog = false }) {
                    Text("انصراف", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        )
    }
}
