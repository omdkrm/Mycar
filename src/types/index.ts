export type OperationType = 'تعویض' | 'سرویس' | 'تعمیر';

export type PartCategory = 'موتور' | 'ترمز' | 'جلوبندی و تعلیق' | 'سرویس‌های دوره‌ای' | 'سایر';

export type FuelType = 'معمولی' | 'سوپر';

export interface Vehicle {
  id: string;
  name: string; // نام یا عنوان خودرو
  brand: string; // برند (مثلا پژو، دنا، هیوندای)
  model: string; // مدل
  year: number; // سال ساخت (بدون جداکننده هزارگان مثل 1401 یا 2022)
  trim?: string; // تیپ (مثلا پلاس، TU5، اتومات)
  engineDisplacement?: string; // حجم موتور (مثلا 1645cc یا 1.6 لیتر)
  licensePlate?: string; // پلاک
  vin?: string; // شماره شاسی / VIN
  currentMileage: number; // کیلومتر فعلی (با جداکننده هزارگان)
  createdAt: number;
  updatedAt: number;
}

export interface MileageRecord {
  id: string;
  vehicleId: string;
  mileage: number;
  timestamp: number; // Date/Time UTC
  notes?: string;
}

export interface CatalogItem {
  id: string;
  name: string;
  category: PartCategory;
  recommendedKmInterval: number; // مثلا 10000 کیلومتر
  recommendedTimeIntervalMonths: number; // مثلا 6 یا 12 ماه
  warningThresholdKm: number; // آستانه هشدار کیلومتر مثلا 500 کیلومتر
  warningThresholdDays: number; // آستانه هشدار روز مثلا 14 روز
  description?: string;
  isCustom?: boolean;
}

export interface VehicleMaintenanceSchedule {
  id: string;
  vehicleId: string;
  catalogItemId?: string;
  partName: string;
  category: PartCategory;
  kmInterval: number;
  timeIntervalMonths: number;
  warningThresholdKm?: number;
  warningThresholdDays?: number;
  description?: string;
  isEnabled?: boolean;
}

export interface ServiceRecord {
  id: string;
  vehicleId: string;
  catalogItemId?: string;
  partName: string;
  category: PartCategory;
  operationType: OperationType;
  dateTimestamp: number;
  mileage: number;
  partPrice: number; // قیمت قطعه
  laborCost: number; // اجرت
  totalCost: number; // مجموع هزینه = قیمت قطعه + اجرت
  repairShop?: string; // تعمیرگاه یا ارائه‌دهنده سرویس
  description?: string;
  invoicePhotoUrl?: string; // عکس فاکتور یا قطعه (اختیاری)
  nextMileage?: number; // کیلومتر سرویس بعدی
  nextDueDateTimestamp?: number; // تاریخ سرویس بعدی
  createdAt: number;
}

export type ReminderStatus = 'healthy' | 'approaching' | 'overdue';

export interface ReminderItem {
  id: string;
  vehicleId: string;
  scheduleId?: string;
  catalogItemId?: string;
  partName: string;
  category: PartCategory;
  currentMileage: number;
  lastServiceMileage?: number;
  lastServiceDateTimestamp?: number;
  dueMileage?: number;
  dueDateTimestamp?: number;
  kmRemaining?: number;
  daysRemaining?: number;
  status: ReminderStatus; // healthy (سبز), approaching (نارنجی), overdue (قرمز)
  statusMessage: string;
}

export interface FuelRecord {
  id: string;
  vehicleId: string;
  dateTimestamp: number;
  mileage: number;
  liters: number; // لیتر بنزین
  fuelQuantity?: number; // سازگاری
  totalCost: number; // هزینه کل (تومان)
  pricePerLiter?: number; // قیمت هر لیتر
  fuelType?: FuelType;
  isFullTank?: boolean;
  gasStation?: string;
  notes?: string;
  note?: string;
  createdAt: number;
}

export interface ExpenseSummary {
  totalCost: number;
  partsCost: number;
  laborCost: number;
  fuelCost: number;
  otherCost: number;
  byCategory: Record<string, number>;
}

export interface AppSettings {
  theme: 'light' | 'dark' | 'system';
  currency?: string; // تومان
  currencyUnit?: 'تومان' | 'ریال';
  distanceUnit?: string; // کیلومتر
  notificationsEnabled?: boolean;
  notifyDaysBefore?: number; // e.g. 7
  notifyKmBefore?: number; // e.g. 500
}

export interface BackupData {
  version: string;
  exportDate: string;
  vehicles: Vehicle[];
  mileageRecords: MileageRecord[];
  catalogItems: CatalogItem[];
  schedules: VehicleMaintenanceSchedule[];
  serviceRecords: ServiceRecord[];
  fuelRecords: FuelRecord[];
  settings: AppSettings;
}

export type NavTab =
  | 'vehicles'
  | 'dashboard'
  | 'register-service'
  | 'service-history'
  | 'part-history'
  | 'reminders'
  | 'fuel'
  | 'schedules'
  | 'reports'
  | 'settings';
