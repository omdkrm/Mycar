package com.mycar.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.mycar.app.ui.screens.*
import com.mycar.app.ui.theme.CyanPrimary
import com.mycar.app.ui.theme.MyCarTheme
import com.mycar.app.ui.viewmodel.MainViewModel
import com.mycar.app.ui.viewmodel.MainViewModelFactory

enum class Screen(val title: String, val icon: ImageVector) {
    DASHBOARD("داشبورد", Icons.Default.Dashboard),
    VEHICLES("خودروها", Icons.Default.DirectionsCar),
    SERVICES("سرویسها", Icons.Default.Build),
    FUEL("سوخت", Icons.Default.LocalGasStation),
    REMINDERS("یادآوری", Icons.Default.Notifications),
    REPORTS("گزارشها", Icons.Default.BarChart),
    SETTINGS("تنظیمات", Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory((application as MyCarApplication).repository)
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyCarTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    var currentScreen by remember { mutableStateOf(Screen.DASHBOARD) }

                    val vehicles by viewModel.vehicles.collectAsState()
                    val activeVehicle by viewModel.activeVehicle.collectAsState()
                    val services by viewModel.services.collectAsState()
                    val fuelRecords by viewModel.fuelRecords.collectAsState()
                    val reminders by viewModel.reminders.collectAsState()
                    val stats by viewModel.stats.collectAsState()

                    Scaffold(
                        topBar = {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = "My Car | ${currentScreen.title}",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                colors = TopAppBarDefaults.topAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    titleContentColor = MaterialTheme.colorScheme.onSurface
                                )
                            )
                        },
                        bottomBar = {
                            NavigationBar(
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 3.dp
                            ) {
                                Screen.values().forEach { screen ->
                                    val isSelected = currentScreen == screen
                                    NavigationBarItem(
                                        icon = {
                                            Icon(
                                                imageVector = screen.icon,
                                                contentDescription = screen.title
                                            )
                                        },
                                        label = {
                                            Text(
                                                text = screen.title,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                maxLines = 1,
                                                softWrap = false
                                            )
                                        },
                                        selected = isSelected,
                                        onClick = { currentScreen = screen },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = MaterialTheme.colorScheme.primary,
                                            selectedTextColor = MaterialTheme.colorScheme.primary,
                                            indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    )
                                }
                            }
                        }
                    ) { padding ->
                        Box(modifier = Modifier.padding(padding)) {
                            when (currentScreen) {
                                Screen.DASHBOARD -> DashboardScreen(
                                    vehicles = vehicles,
                                    activeVehicle = activeVehicle,
                                    stats = stats,
                                    reminders = reminders,
                                    onNavigateToVehicles = { currentScreen = Screen.VEHICLES },
                                    onNavigateToServices = { currentScreen = Screen.SERVICES },
                                    onNavigateToFuel = { currentScreen = Screen.FUEL },
                                    onNavigateToReminders = { currentScreen = Screen.REMINDERS }
                                )
                                Screen.VEHICLES -> VehiclesScreen(
                                    vehicles = vehicles,
                                    activeVehicle = activeVehicle,
                                    onSelectVehicle = { viewModel.selectVehicle(it) },
                                    onAddVehicle = { name, model, year, plate, km, cap, fuel, col ->
                                        viewModel.addVehicle(name, model, year, plate, km, cap, fuel, col)
                                    },
                                    onDeleteVehicle = { viewModel.deleteVehicle(it) }
                                )
                                Screen.SERVICES -> ServiceHistoryScreen(
                                    activeVehicle = activeVehicle,
                                    services = services,
                                    onAddService = { vId, pName, catId, cat, km, date, cTot, pCost, lCost, brand, center, inv, notes ->
                                        viewModel.addService(vId, pName, catId, cat, km, date, cTot, pCost, lCost, brand, center, inv, notes)
                                    },
                                    onDeleteService = { viewModel.deleteService(it) }
                                )
                                Screen.FUEL -> FuelScreen(
                                    activeVehicle = activeVehicle,
                                    fuelRecords = fuelRecords,
                                    onAddFuelRecord = { vId, date, km, liters, cpl, total, isFull, fType, station, notes ->
                                        viewModel.addFuelRecord(vId, date, km, liters, cpl, total, isFull, fType, station, notes)
                                    },
                                    onDeleteFuelRecord = { viewModel.deleteFuelRecord(it) }
                                )
                                Screen.REMINDERS -> RemindersScreen(
                                    activeVehicle = activeVehicle,
                                    reminders = reminders,
                                    onNavigateToServices = { currentScreen = Screen.SERVICES }
                                )
                                Screen.REPORTS -> ReportsScreen(
                                    activeVehicle = activeVehicle,
                                    stats = stats
                                )
                                Screen.SETTINGS -> SettingsScreen(
                                    onClearAllData = { viewModel.clearAllUserData() },
                                    onCreateBackup = { viewModel.createBackupJson() },
                                    onWriteBackupToUri = { ctx, uri, json -> viewModel.writeBackupToUri(ctx, uri, json) },
                                    onReadBackupFromUri = { ctx, uri -> viewModel.readBackupFromUri(ctx, uri) },
                                    onRestoreBackup = { json -> viewModel.restoreBackup(json) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
