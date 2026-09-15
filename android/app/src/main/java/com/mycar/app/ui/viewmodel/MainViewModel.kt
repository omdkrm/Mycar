package com.mycar.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.mycar.app.data.model.*
import com.mycar.app.data.repository.CarRepository
import com.mycar.app.data.repository.ReminderItem
import com.mycar.app.data.repository.VehicleStats
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(private val repository: CarRepository) : ViewModel() {

    val vehicles: StateFlow<List<Vehicle>> = repository.allVehicles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedVehicleId = MutableStateFlow<String?>(null)

    val activeVehicle: StateFlow<Vehicle?> = combine(vehicles, _selectedVehicleId) { list, id ->
        if (id != null) list.find { it.id == id } ?: list.firstOrNull()
        else list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val services: StateFlow<List<ServiceRecord>> = activeVehicle
        .flatMapLatest { vehicle ->
            if (vehicle != null) repository.getServices(vehicle.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val fuelRecords: StateFlow<List<FuelRecord>> = activeVehicle
        .flatMapLatest { vehicle ->
            if (vehicle != null) repository.getFuelRecords(vehicle.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val schedules: StateFlow<List<MaintenanceSchedule>> = activeVehicle
        .flatMapLatest { vehicle ->
            if (vehicle != null) repository.getSchedules(vehicle.id)
            else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _reminders = MutableStateFlow<List<ReminderItem>>(emptyList())
    val reminders: StateFlow<List<ReminderItem>> = _reminders.asStateFlow()

    private val _stats = MutableStateFlow<VehicleStats?>(null)
    val stats: StateFlow<VehicleStats?> = _stats.asStateFlow()

    init {
        viewModelScope.launch {
            activeVehicle.collect { vehicle ->
                if (vehicle != null) {
                    refreshVehicleCalculations(vehicle)
                } else {
                    _reminders.value = emptyList()
                    _stats.value = null
                }
            }
        }
    }

    private suspend fun refreshVehicleCalculations(vehicle: Vehicle) {
        _reminders.value = repository.calculateReminders(vehicle)
        _stats.value = repository.calculateStats(vehicle)
    }

    fun selectVehicle(vehicle: Vehicle) {
        _selectedVehicleId.value = vehicle.id
    }

    fun addVehicle(
        name: String,
        model: String,
        year: String,
        plate: String,
        mileage: Int,
        fuelCapacity: Double,
        fuelType: String,
        color: String
    ) {
        viewModelScope.launch {
            val vehicle = Vehicle(
                name = name,
                model = model,
                year = year,
                plateNumber = plate,
                currentMileage = mileage,
                fuelCapacityLiters = fuelCapacity,
                fuelType = fuelType,
                color = color,
                isDefault = vehicles.value.isEmpty()
            )
            repository.addVehicle(vehicle)
            _selectedVehicleId.value = vehicle.id
        }
    }

    fun updateVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.updateVehicle(vehicle)
            refreshVehicleCalculations(vehicle)
        }
    }

    fun deleteVehicle(vehicle: Vehicle) {
        viewModelScope.launch {
            repository.deleteVehicle(vehicle)
            if (_selectedVehicleId.value == vehicle.id) {
                _selectedVehicleId.value = null
            }
        }
    }

    fun addService(
        vehicleId: String,
        partName: String,
        catalogItemId: String?,
        category: String,
        mileage: Int,
        dateTimestamp: Long,
        costTotal: Long,
        partCost: Long,
        laborCost: Long,
        brand: String,
        center: String,
        invoice: String,
        notes: String
    ) {
        viewModelScope.launch {
            val service = ServiceRecord(
                vehicleId = vehicleId,
                partName = partName,
                catalogItemId = catalogItemId,
                serviceCategory = category,
                mileage = mileage,
                dateTimestamp = dateTimestamp,
                costTotal = costTotal,
                partCost = partCost,
                laborCost = laborCost,
                brand = brand,
                serviceCenter = center,
                invoiceNumber = invoice,
                notes = notes
            )
            repository.addService(service)
            activeVehicle.value?.let { refreshVehicleCalculations(it) }
        }
    }

    fun updateService(service: ServiceRecord) {
        viewModelScope.launch {
            repository.updateService(service)
            activeVehicle.value?.let { refreshVehicleCalculations(it) }
        }
    }

    fun deleteService(service: ServiceRecord) {
        viewModelScope.launch {
            repository.deleteService(service)
            activeVehicle.value?.let { refreshVehicleCalculations(it) }
        }
    }

    fun addFuelRecord(
        vehicleId: String,
        dateTimestamp: Long,
        mileage: Int,
        liters: Double,
        costPerLiter: Long,
        totalCost: Long,
        isFullTank: Boolean,
        fuelType: String,
        station: String,
        notes: String
    ) {
        viewModelScope.launch {
            val record = FuelRecord(
                vehicleId = vehicleId,
                dateTimestamp = dateTimestamp,
                mileage = mileage,
                liters = liters,
                costPerLiter = costPerLiter,
                totalCost = totalCost,
                isFullTank = isFullTank,
                fuelType = fuelType,
                gasStation = station,
                notes = notes
            )
            repository.addFuelRecord(record)
            activeVehicle.value?.let { refreshVehicleCalculations(it) }
        }
    }

    fun deleteFuelRecord(record: FuelRecord) {
        viewModelScope.launch {
            repository.deleteFuelRecord(record)
            activeVehicle.value?.let { refreshVehicleCalculations(it) }
        }
    }

    val backupManager = repository.backupManager

    suspend fun createBackupJson(): String = repository.createBackupJson()

    suspend fun restoreBackup(jsonString: String): com.mycar.app.data.backup.RestoreResult {
        val result = repository.restoreFromJson(jsonString)
        if (result is com.mycar.app.data.backup.RestoreResult.Success) {
            _selectedVehicleId.value = null
            _reminders.value = emptyList()
            _stats.value = null
        }
        return result
    }

    suspend fun writeBackupToUri(context: android.content.Context, uri: android.net.Uri, json: String): Boolean {
        return repository.backupManager.writeBackupToUri(context, uri, json)
    }

    suspend fun readBackupFromUri(context: android.content.Context, uri: android.net.Uri): String? {
        return repository.backupManager.readBackupFromUri(context, uri)
    }

    fun clearAllUserData() {
        viewModelScope.launch {
            repository.clearAllUserData()
            _selectedVehicleId.value = null
            _reminders.value = emptyList()
            _stats.value = null
        }
    }
}

class MainViewModelFactory(private val repository: CarRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
