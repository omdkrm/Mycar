import {
  Vehicle,
  ServiceRecord,
  FuelRecord,
  VehicleMaintenanceSchedule,
  ReminderItem,
  ReminderStatus,
  ExpenseSummary,
} from '../types';
import { formatWithCommas } from './formatters';

/**
 * Calculates maintenance deadlines and reminder status for all schedules of a vehicle
 */
export function calculateReminders(
  vehicle: Vehicle,
  schedules: VehicleMaintenanceSchedule[],
  serviceRecords: ServiceRecord[]
): ReminderItem[] {
  const vehicleRecords = serviceRecords.filter((r) => r.vehicleId === vehicle.id);

  return schedules
    .filter((s) => s.vehicleId === vehicle.id && s.isEnabled !== false)
    .map((schedule) => {
      // Find latest service record for this specific part or catalog item
      const matchingRecords = vehicleRecords
        .filter((r) => (schedule.catalogItemId && r.catalogItemId === schedule.catalogItemId) || r.partName === schedule.partName)
        .sort((a, b) => b.mileage - a.mileage || b.dateTimestamp - a.dateTimestamp);

      const latestRecord = matchingRecords[0];

      const lastServiceMileage = latestRecord ? latestRecord.mileage : vehicle.currentMileage;
      const lastServiceDateTimestamp = latestRecord ? latestRecord.dateTimestamp : vehicle.createdAt;

      // Calculate next due targets
      const dueMileage = lastServiceMileage + schedule.kmInterval;

      // Date interval in milliseconds (approx 30.44 days per month)
      const monthsMs = schedule.timeIntervalMonths * 30.44 * 24 * 60 * 60 * 1000;
      const dueDateTimestamp = lastServiceDateTimestamp + monthsMs;

      // Current remaining distance and time
      const kmRemaining = dueMileage - vehicle.currentMileage;
      const now = Date.now();
      const msRemaining = dueDateTimestamp - now;
      const daysRemaining = Math.round(msRemaining / (1000 * 60 * 60 * 24));

      // Warning thresholds
      const warningThresholdKm = schedule.warningThresholdKm || 500;
      const warningThresholdDays = schedule.warningThresholdDays || 14;

      // Determine status according to prompt requirements:
      // Overdue: kmRemaining <= 0 OR daysRemaining < 0
      // Approaching: kmRemaining <= warningThresholdKm OR daysRemaining <= warningThresholdDays
      // Healthy: otherwise
      let status: ReminderStatus = 'healthy';
      let statusMessage = '';

      const isKmOverdue = kmRemaining < 0;
      const isTimeOverdue = daysRemaining < 0;
      const isKmDue = kmRemaining === 0;
      const isTimeDue = daysRemaining === 0;

      const isKmApproaching = kmRemaining <= warningThresholdKm && kmRemaining > 0;
      const isTimeApproaching = daysRemaining <= warningThresholdDays && daysRemaining > 0;

      if (isKmOverdue || isTimeOverdue) {
        status = 'overdue';
        if (isKmOverdue && Math.abs(kmRemaining) > 0) {
          statusMessage = `${schedule.partName} ${formatWithCommas(Math.abs(kmRemaining))} کیلومتر عقب افتاده است`;
        } else {
          statusMessage = `مهلت زمانی ${schedule.partName} سپری شده است (${Math.abs(daysRemaining)} روز گذشته)`;
        }
      } else if (isKmDue || isTimeDue) {
        status = 'overdue';
        statusMessage = `زمان سرویس ${schedule.partName} فرا رسیده است`;
      } else if (isKmApproaching || isTimeApproaching) {
        status = 'approaching';
        if (isKmApproaching) {
          statusMessage = `سرویس ${schedule.partName} نزدیک است (${formatWithCommas(kmRemaining)} کیلومتر باقی‌مانده)`;
        } else {
          statusMessage = `سرویس ${schedule.partName} نزدیک است (${daysRemaining} روز باقی‌مانده)`;
        }
      } else {
        status = 'healthy';
        statusMessage = `${formatWithCommas(kmRemaining)} کیلومتر یا ${daysRemaining} روز تا موعد`;
      }

      return {
        id: `rem-${schedule.id}`,
        vehicleId: vehicle.id,
        scheduleId: schedule.id,
        catalogItemId: schedule.catalogItemId,
        partName: schedule.partName,
        category: schedule.category,
        currentMileage: vehicle.currentMileage,
        lastServiceMileage,
        lastServiceDateTimestamp,
        dueMileage,
        dueDateTimestamp,
        kmRemaining,
        daysRemaining,
        status,
        statusMessage,
      };
    })
    .sort((a, b) => {
      // Sort: overdue first, then approaching, then healthy
      const score = (item: ReminderItem) =>
        item.status === 'overdue' ? 0 : item.status === 'approaching' ? 1 : 2;
      const diff = score(a) - score(b);
      if (diff !== 0) return diff;
      return (a.kmRemaining ?? 0) - (b.kmRemaining ?? 0);
    });
}

// Alias for App.tsx
export const calculateMaintenanceReminders = calculateReminders;

/**
 * Calculates average fuel consumption in Liters per 100 km across consecutive fuel records
 */
export function calculateFuelConsumption(
  fuelRecords: FuelRecord[],
  vehicleId: string
): {
  averageLitersPer100Km: number | null;
  totalFuelLiters: number;
  totalFuelCost: number;
  recordsCount: number;
  costPerKm: number | null;
} {
  const records = fuelRecords
    .filter((r) => r.vehicleId === vehicleId)
    .sort((a, b) => a.mileage - b.mileage || a.dateTimestamp - b.dateTimestamp);

  const totalFuelCost = records.reduce((sum, r) => sum + (r.totalCost || 0), 0);
  const totalFuelLiters = records.reduce((sum, r) => sum + (r.liters || r.fuelQuantity || 0), 0);

  if (records.length < 2) {
    return {
      averageLitersPer100Km: null,
      totalFuelLiters,
      totalFuelCost,
      recordsCount: records.length,
      costPerKm: null,
    };
  }

  // Consecutive segments
  let validDistance = 0;
  let validFuel = 0;

  for (let i = 1; i < records.length; i++) {
    const prev = records[i - 1];
    const curr = records[i];
    const distance = curr.mileage - prev.mileage;
    const vol = curr.liters || curr.fuelQuantity || 0;
    if (distance > 0 && vol > 0) {
      validDistance += distance;
      validFuel += vol;
    }
  }

  const averageLitersPer100Km =
    validDistance > 0 && validFuel > 0 ? Math.round(((validFuel / validDistance) * 100) * 10) / 10 : null;

  const costPerKm = validDistance > 0 && totalFuelCost > 0 ? Math.round(totalFuelCost / validDistance) : null;

  return {
    averageLitersPer100Km,
    totalFuelLiters,
    totalFuelCost,
    recordsCount: records.length,
    costPerKm,
  };
}

/**
 * Computes expense aggregates for a vehicle or across all vehicles
 */
export function calculateExpenses(
  serviceRecords: ServiceRecord[],
  fuelRecords: FuelRecord[],
  vehicleId?: string
): ExpenseSummary {
  const filteredServices = vehicleId
    ? serviceRecords.filter((r) => r.vehicleId === vehicleId)
    : serviceRecords;

  const filteredFuel = vehicleId
    ? fuelRecords.filter((r) => r.vehicleId === vehicleId)
    : fuelRecords;

  let partsCost = 0;
  let laborCost = 0;
  let totalServices = 0;
  const byCategory: Record<string, number> = {};

  filteredServices.forEach((r) => {
    partsCost += r.partPrice || 0;
    laborCost += r.laborCost || 0;
    totalServices += r.totalCost || 0;
    byCategory[r.category] = (byCategory[r.category] || 0) + (r.totalCost || 0);
  });

  const fuelCost = filteredFuel.reduce((sum, r) => sum + (r.totalCost || 0), 0);
  if (fuelCost > 0) {
    byCategory['سوخت'] = fuelCost;
  }

  const totalCost = totalServices + fuelCost;

  return {
    totalCost,
    partsCost,
    laborCost,
    fuelCost,
    otherCost: 0,
    byCategory,
  };
}
