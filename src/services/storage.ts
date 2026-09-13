import {
  Vehicle,
  MileageRecord,
  CatalogItem,
  VehicleMaintenanceSchedule,
  ServiceRecord,
  FuelRecord,
  AppSettings,
  BackupData,
} from '../types';
import { DEFAULT_CATALOG_ITEMS } from '../utils/catalogData';

const STORAGE_KEYS = {
  VEHICLES: 'mycar_vehicles',
  MILEAGE_RECORDS: 'mycar_mileage_records',
  CATALOG_ITEMS: 'mycar_catalog_items',
  SCHEDULES: 'mycar_schedules',
  SERVICE_RECORDS: 'mycar_service_records',
  FUEL_RECORDS: 'mycar_fuel_records',
  SETTINGS: 'mycar_settings',
  SELECTED_VEHICLE_ID: 'mycar_selected_vehicle_id',
};

export const DEFAULT_SETTINGS: AppSettings = {
  theme: 'light',
  currency: 'تومان',
  currencyUnit: 'تومان',
  distanceUnit: 'کیلومتر',
  notificationsEnabled: true,
  notifyDaysBefore: 14,
  notifyKmBefore: 500,
};

export class StorageService {
  static purgeTestDataIfPresent(): void {
    try {
      const CLEANUP_FLAG = 'mycar_test_data_purged_v3';
      const isCleaned = localStorage.getItem(CLEANUP_FLAG);

      const rawVehicles = localStorage.getItem(STORAGE_KEYS.VEHICLES);
      const rawServices = localStorage.getItem(STORAGE_KEYS.SERVICE_RECORDS);
      const rawFuels = localStorage.getItem(STORAGE_KEYS.FUEL_RECORDS);
      const rawMileages = localStorage.getItem(STORAGE_KEYS.MILEAGE_RECORDS);
      const rawSchedules = localStorage.getItem(STORAGE_KEYS.SCHEDULES);

      // Detect if legacy demo test vehicle or records exist
      const hasDemoVeh = rawVehicles && (rawVehicles.includes('veh-dena-plus-1402') || rawVehicles.includes('دنا پلاس توربو'));
      const hasDemoServices = rawServices && (rawServices.includes('srv-1') || rawServices.includes('کاسترول'));
      const hasDemoFuels = rawFuels && (rawFuels.includes('fuel-1') || rawFuels.includes('یادگار امام'));
      const hasDemoMileages = rawMileages && rawMileages.includes('veh-dena-plus-1402');

      if (!isCleaned || hasDemoVeh || hasDemoServices || hasDemoFuels || hasDemoMileages) {
        // Filter out test vehicle
        if (rawVehicles) {
          try {
            const list: Vehicle[] = JSON.parse(rawVehicles);
            const filtered = list.filter((v) => v.id !== 'veh-dena-plus-1402' && !v.name?.includes('دنا پلاس توربو'));
            localStorage.setItem(STORAGE_KEYS.VEHICLES, JSON.stringify(filtered));
          } catch {
            localStorage.setItem(STORAGE_KEYS.VEHICLES, JSON.stringify([]));
          }
        } else {
          localStorage.setItem(STORAGE_KEYS.VEHICLES, JSON.stringify([]));
        }

        // Filter out test services
        if (rawServices) {
          try {
            const list: ServiceRecord[] = JSON.parse(rawServices);
            const filtered = list.filter((s) => s.vehicleId !== 'veh-dena-plus-1402' && !['srv-1', 'srv-2', 'srv-4'].includes(s.id));
            localStorage.setItem(STORAGE_KEYS.SERVICE_RECORDS, JSON.stringify(filtered));
          } catch {
            localStorage.setItem(STORAGE_KEYS.SERVICE_RECORDS, JSON.stringify([]));
          }
        } else {
          localStorage.setItem(STORAGE_KEYS.SERVICE_RECORDS, JSON.stringify([]));
        }

        // Filter out test fuels
        if (rawFuels) {
          try {
            const list: FuelRecord[] = JSON.parse(rawFuels);
            const filtered = list.filter((f) => f.vehicleId !== 'veh-dena-plus-1402' && !['fuel-1', 'fuel-2', 'fuel-3'].includes(f.id));
            localStorage.setItem(STORAGE_KEYS.FUEL_RECORDS, JSON.stringify(filtered));
          } catch {
            localStorage.setItem(STORAGE_KEYS.FUEL_RECORDS, JSON.stringify([]));
          }
        } else {
          localStorage.setItem(STORAGE_KEYS.FUEL_RECORDS, JSON.stringify([]));
        }

        // Filter out test mileages
        if (rawMileages) {
          try {
            const list: MileageRecord[] = JSON.parse(rawMileages);
            const filtered = list.filter((m) => m.vehicleId !== 'veh-dena-plus-1402' && !['m-1', 'm-2', 'm-3', 'm-4'].includes(m.id));
            localStorage.setItem(STORAGE_KEYS.MILEAGE_RECORDS, JSON.stringify(filtered));
          } catch {
            localStorage.setItem(STORAGE_KEYS.MILEAGE_RECORDS, JSON.stringify([]));
          }
        } else {
          localStorage.setItem(STORAGE_KEYS.MILEAGE_RECORDS, JSON.stringify([]));
        }

        // Filter out test schedules
        if (rawSchedules) {
          try {
            const list: VehicleMaintenanceSchedule[] = JSON.parse(rawSchedules);
            const filtered = list.filter((s) => s.vehicleId !== 'veh-dena-plus-1402');
            localStorage.setItem(STORAGE_KEYS.SCHEDULES, JSON.stringify(filtered));
          } catch {
            localStorage.setItem(STORAGE_KEYS.SCHEDULES, JSON.stringify([]));
          }
        } else {
          localStorage.setItem(STORAGE_KEYS.SCHEDULES, JSON.stringify([]));
        }

        // Clear selected vehicle if it was the test vehicle
        const selectedId = localStorage.getItem(STORAGE_KEYS.SELECTED_VEHICLE_ID);
        if (selectedId === 'veh-dena-plus-1402') {
          localStorage.removeItem(STORAGE_KEYS.SELECTED_VEHICLE_ID);
        }

        // Ensure catalog items exist
        if (!localStorage.getItem(STORAGE_KEYS.CATALOG_ITEMS)) {
          localStorage.setItem(STORAGE_KEYS.CATALOG_ITEMS, JSON.stringify(DEFAULT_CATALOG_ITEMS));
        }

        localStorage.setItem(CLEANUP_FLAG, 'true');
      }
    } catch (e) {
      console.error('Error during test data purge:', e);
    }
  }

  static clearAllUserData(): void {
    localStorage.setItem(STORAGE_KEYS.VEHICLES, JSON.stringify([]));
    localStorage.setItem(STORAGE_KEYS.MILEAGE_RECORDS, JSON.stringify([]));
    localStorage.setItem(STORAGE_KEYS.SERVICE_RECORDS, JSON.stringify([]));
    localStorage.setItem(STORAGE_KEYS.FUEL_RECORDS, JSON.stringify([]));
    localStorage.setItem(STORAGE_KEYS.SCHEDULES, JSON.stringify([]));
    localStorage.removeItem(STORAGE_KEYS.SELECTED_VEHICLE_ID);
    localStorage.setItem(STORAGE_KEYS.CATALOG_ITEMS, JSON.stringify(DEFAULT_CATALOG_ITEMS));
  }

  static loadAll() {
    this.purgeTestDataIfPresent();

    return {
      vehicles: this.getVehicles(),
      serviceRecords: this.getServiceRecords(),
      fuelRecords: this.getFuelRecords(),
      schedules: this.getSchedules(),
      catalogItems: this.getCatalogItems(),
      mileageHistory: this.getMileageRecords(),
      settings: this.getSettings(),
    };
  }

  static getVehicles(): Vehicle[] {
    const raw = localStorage.getItem(STORAGE_KEYS.VEHICLES);
    if (!raw) {
      return [];
    }
    try {
      return JSON.parse(raw);
    } catch {
      return [];
    }
  }

  static saveVehicles(vehicles: Vehicle[]): void {
    localStorage.setItem(STORAGE_KEYS.VEHICLES, JSON.stringify(vehicles));
  }

  static saveVehicle(vehicle: Vehicle): void {
    const list = this.getVehicles();
    const idx = list.findIndex((v) => v.id === vehicle.id);
    if (idx >= 0) {
      list[idx] = vehicle;
    } else {
      list.push(vehicle);
    }
    this.saveVehicles(list);
  }

  static deleteVehicle(id: string): void {
    const list = this.getVehicles().filter((v) => v.id !== id);
    this.saveVehicles(list);

    // Delete associated records
    const services = this.getServiceRecords().filter((r) => r.vehicleId !== id);
    this.saveServiceRecords(services);

    const fuels = this.getFuelRecords().filter((r) => r.vehicleId !== id);
    this.saveFuelRecords(fuels);

    const scheds = this.getSchedules().filter((s) => s.vehicleId !== id);
    this.saveSchedules(scheds);

    const mileages = this.getMileageRecords().filter((m) => m.vehicleId !== id);
    this.saveMileageRecords(mileages);
  }

  static getMileageRecords(vehicleId?: string): MileageRecord[] {
    const raw = localStorage.getItem(STORAGE_KEYS.MILEAGE_RECORDS);
    if (!raw) return [];
    try {
      const records: MileageRecord[] = JSON.parse(raw);
      return vehicleId ? records.filter((r) => r.vehicleId === vehicleId) : records;
    } catch {
      return [];
    }
  }

  static saveMileageRecords(records: MileageRecord[]): void {
    localStorage.setItem(STORAGE_KEYS.MILEAGE_RECORDS, JSON.stringify(records));
  }

  static addMileageRecord(record: { vehicleId: string; mileage: number; timestamp?: number; notes?: string }): void {
    const records = this.getMileageRecords();
    const newRecord: MileageRecord = {
      id: `m-${Date.now()}-${Math.random().toString(36).substring(2, 7)}`,
      vehicleId: record.vehicleId,
      mileage: record.mileage,
      timestamp: record.timestamp || Date.now(),
      notes: record.notes,
    };
    records.push(newRecord);
    this.saveMileageRecords(records);

    // Update vehicle's current mileage if greater
    const vehicles = this.getVehicles();
    const vehicle = vehicles.find((v) => v.id === record.vehicleId);
    if (vehicle && record.mileage > vehicle.currentMileage) {
      vehicle.currentMileage = record.mileage;
      vehicle.updatedAt = Date.now();
      this.saveVehicles(vehicles);
    }
  }

  static getCatalogItems(): CatalogItem[] {
    const raw = localStorage.getItem(STORAGE_KEYS.CATALOG_ITEMS);
    if (!raw) return DEFAULT_CATALOG_ITEMS;
    try {
      return JSON.parse(raw);
    } catch {
      return DEFAULT_CATALOG_ITEMS;
    }
  }

  static saveCatalogItems(items: CatalogItem[]): void {
    localStorage.setItem(STORAGE_KEYS.CATALOG_ITEMS, JSON.stringify(items));
  }

  static getSchedules(vehicleId?: string): VehicleMaintenanceSchedule[] {
    const raw = localStorage.getItem(STORAGE_KEYS.SCHEDULES);
    if (!raw) return [];
    try {
      const schedules: VehicleMaintenanceSchedule[] = JSON.parse(raw);
      return vehicleId ? schedules.filter((s) => s.vehicleId === vehicleId) : schedules;
    } catch {
      return [];
    }
  }

  static saveSchedules(schedules: VehicleMaintenanceSchedule[]): void {
    localStorage.setItem(STORAGE_KEYS.SCHEDULES, JSON.stringify(schedules));
  }

  static saveSchedule(sched: VehicleMaintenanceSchedule): void {
    const list = this.getSchedules();
    const idx = list.findIndex((s) => s.id === sched.id);
    if (idx >= 0) {
      list[idx] = sched;
    } else {
      list.push(sched);
    }
    this.saveSchedules(list);
  }

  static deleteSchedule(id: string): void {
    const list = this.getSchedules().filter((s) => s.id !== id);
    this.saveSchedules(list);
  }

  static initializeSchedulesForVehicle(vehicleId: string): VehicleMaintenanceSchedule[] {
    const catalog = this.getCatalogItems();
    const allSchedules = this.getSchedules();

    // Remove existing for this vehicle if any
    const otherSchedules = allSchedules.filter((s) => s.vehicleId !== vehicleId);

    const newSchedules: VehicleMaintenanceSchedule[] = catalog.map((cat) => ({
      id: `sched-${vehicleId}-${cat.id}`,
      vehicleId,
      catalogItemId: cat.id,
      partName: cat.name,
      category: cat.category,
      kmInterval: cat.recommendedKmInterval,
      timeIntervalMonths: cat.recommendedTimeIntervalMonths,
      warningThresholdKm: cat.warningThresholdKm,
      warningThresholdDays: cat.warningThresholdDays,
      description: cat.description,
      isEnabled: true,
    }));

    const merged = [...otherSchedules, ...newSchedules];
    this.saveSchedules(merged);
    return newSchedules;
  }

  static getServiceRecords(vehicleId?: string): ServiceRecord[] {
    const raw = localStorage.getItem(STORAGE_KEYS.SERVICE_RECORDS);
    if (!raw) return [];
    try {
      const records: ServiceRecord[] = JSON.parse(raw);
      return vehicleId ? records.filter((r) => r.vehicleId === vehicleId) : records;
    } catch {
      return [];
    }
  }

  static saveServiceRecords(records: ServiceRecord[]): void {
    localStorage.setItem(STORAGE_KEYS.SERVICE_RECORDS, JSON.stringify(records));
  }

  static saveServiceRecord(rec: ServiceRecord): void {
    const list = this.getServiceRecords();
    const idx = list.findIndex((r) => r.id === rec.id);
    if (idx >= 0) {
      list[idx] = rec;
    } else {
      list.push(rec);
    }
    this.saveServiceRecords(list);
  }

  static deleteServiceRecord(id: string): void {
    const list = this.getServiceRecords().filter((r) => r.id !== id);
    this.saveServiceRecords(list);
  }

  static getFuelRecords(vehicleId?: string): FuelRecord[] {
    const raw = localStorage.getItem(STORAGE_KEYS.FUEL_RECORDS);
    if (!raw) return [];
    try {
      const records: FuelRecord[] = JSON.parse(raw);
      return vehicleId ? records.filter((r) => r.vehicleId === vehicleId) : records;
    } catch {
      return [];
    }
  }

  static saveFuelRecords(records: FuelRecord[]): void {
    localStorage.setItem(STORAGE_KEYS.FUEL_RECORDS, JSON.stringify(records));
  }

  static saveFuelRecord(rec: FuelRecord): void {
    const list = this.getFuelRecords();
    const idx = list.findIndex((r) => r.id === rec.id);
    if (idx >= 0) {
      list[idx] = rec;
    } else {
      list.push(rec);
    }
    this.saveFuelRecords(list);
  }

  static deleteFuelRecord(id: string): void {
    const list = this.getFuelRecords().filter((r) => r.id !== id);
    this.saveFuelRecords(list);
  }

  static getSettings(): AppSettings {
    const raw = localStorage.getItem(STORAGE_KEYS.SETTINGS);
    if (!raw) return DEFAULT_SETTINGS;
    try {
      return { ...DEFAULT_SETTINGS, ...JSON.parse(raw) };
    } catch {
      return DEFAULT_SETTINGS;
    }
  }

  static saveSettings(settings: Partial<AppSettings>): void {
    const current = this.getSettings();
    const updated = { ...current, ...settings };
    localStorage.setItem(STORAGE_KEYS.SETTINGS, JSON.stringify(updated));
  }

  static exportBackupJson(): string {
    const backup: BackupData = {
      version: '1.0.0',
      exportDate: new Date().toISOString(),
      vehicles: this.getVehicles(),
      mileageRecords: this.getMileageRecords(),
      catalogItems: this.getCatalogItems(),
      schedules: this.getSchedules(),
      serviceRecords: this.getServiceRecords(),
      fuelRecords: this.getFuelRecords(),
      settings: this.getSettings(),
    };
    return JSON.stringify(backup, null, 2);
  }

  static restoreBackupJson(jsonStr: string): { success: boolean; message?: string } {
    try {
      const backup = JSON.parse(jsonStr) as BackupData;
      if (!backup || !Array.isArray(backup.vehicles)) {
        return { success: false, message: 'ساختار فایل پشتیبان معتبر نمی‌باشد.' };
      }

      this.saveVehicles(backup.vehicles);
      this.saveMileageRecords(backup.mileageRecords || []);
      this.saveCatalogItems(backup.catalogItems || DEFAULT_CATALOG_ITEMS);
      this.saveSchedules(backup.schedules || []);
      this.saveServiceRecords(backup.serviceRecords || []);
      this.saveFuelRecords(backup.fuelRecords || []);
      if (backup.settings) {
        this.saveSettings(backup.settings);
      }
      return { success: true };
    } catch (e: any) {
      return { success: false, message: 'خطا در بارگذاری فایل: ' + e.message };
    }
  }
}
