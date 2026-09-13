import React, { useState, useMemo } from 'react';
import { Layers, Calendar, Gauge, DollarSign, Clock, ArrowLeft } from 'lucide-react';
import { Vehicle, ServiceRecord, CatalogItem, VehicleMaintenanceSchedule } from '../types';
import { formatCurrency, formatMileage, formatWithCommas } from '../utils/formatters';
import { formatToJalali } from '../utils/persianDate';

interface PartHistoryViewProps {
  vehicle: Vehicle;
  catalogItems: CatalogItem[];
  schedules: VehicleMaintenanceSchedule[];
  serviceRecords: ServiceRecord[];
  onAddNewServiceForPart: (partName: string, catalogItemId?: string) => void;
}

export const PartHistoryView: React.FC<PartHistoryViewProps> = ({
  vehicle,
  catalogItems,
  schedules,
  serviceRecords,
  onAddNewServiceForPart,
}) => {
  // Available unique parts for this vehicle
  const availableParts = useMemo(() => {
    const set = new Map<string, { name: string; catalogItemId?: string; category: string }>();

    // From catalog
    catalogItems.forEach((item) => {
      set.set(item.name, { name: item.name, catalogItemId: item.id, category: item.category });
    });

    // From actual records
    serviceRecords
      .filter((r) => r.vehicleId === vehicle.id)
      .forEach((r) => {
        if (!set.has(r.partName)) {
          set.set(r.partName, { name: r.partName, catalogItemId: r.catalogItemId, category: r.category });
        }
      });

    return Array.from(set.values());
  }, [catalogItems, serviceRecords, vehicle.id]);

  const [selectedPartName, setSelectedPartName] = useState<string>(
    availableParts[0]?.name || 'روغن موتور'
  );

  const selectedPartInfo = availableParts.find((p) => p.name === selectedPartName);

  // Filter records for selected part
  const partRecords = useMemo(() => {
    return serviceRecords
      .filter((r) => r.vehicleId === vehicle.id && r.partName === selectedPartName)
      .sort((a, b) => b.mileage - a.mileage || b.dateTimestamp - a.dateTimestamp);
  }, [serviceRecords, vehicle.id, selectedPartName]);

  // Statistics
  const totalSpent = partRecords.reduce((sum, r) => sum + r.totalCost, 0);

  // Average interval calculation
  const avgIntervalKm = useMemo(() => {
    if (partRecords.length < 2) return null;
    const sortedAsc = [...partRecords].sort((a, b) => a.mileage - b.mileage);
    let totalDiff = 0;
    for (let i = 1; i < sortedAsc.length; i++) {
      totalDiff += sortedAsc[i].mileage - sortedAsc[i - 1].mileage;
    }
    return Math.round(totalDiff / (sortedAsc.length - 1));
  }, [partRecords]);

  // Schedule deadline
  const schedule = schedules.find(
    (s) => s.vehicleId === vehicle.id && (s.partName === selectedPartName || s.catalogItemId === selectedPartInfo?.catalogItemId)
  );

  const latestRecord = partRecords[0];
  const nextMileageDeadline = latestRecord
    ? latestRecord.mileage + (schedule ? schedule.kmInterval : 8000)
    : vehicle.currentMileage + (schedule ? schedule.kmInterval : 8000);

  const remainingKm = nextMileageDeadline - vehicle.currentMileage;

  return (
    <div className="space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
          <Layers className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
          <span>تاریخچه هر قطعه - {vehicle.name}</span>
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
          بررسی سیر تاریخی، فواصل تعویض و تحلیل مخارج اختصاصی برای یک قطعه مشخص
        </p>
      </div>

      {/* Part Selector Bar */}
      <div className="bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div className="flex-1 max-w-md">
          <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
            انتخاب قطعه یا عنوان سرویس:
          </label>
          <select
            value={selectedPartName}
            onChange={(e) => setSelectedPartName(e.target.value)}
            className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 font-bold text-cyan-700 dark:text-cyan-300 focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            {availableParts.map((p) => (
              <option key={p.name} value={p.name}>
                {p.name} ({p.category})
              </option>
            ))}
          </select>
        </div>

        <button
          onClick={() => onAddNewServiceForPart(selectedPartName, selectedPartInfo?.catalogItemId)}
          className="self-start sm:self-end bg-cyan-600 hover:bg-cyan-700 text-white font-bold text-xs sm:text-sm px-4 py-2 rounded-xl transition shadow-xs"
        >
          + ثبت سابقه برای «{selectedPartName}»
        </button>
      </div>

      {/* Statistics Cards for this Part */}
      <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            مجموع هزینه پرداختی برای این قطعه:
          </div>
          <div className="text-xl font-black text-slate-900 dark:text-slate-100">
            {formatCurrency(totalSpent)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            در طول {partRecords.length} بار سرویس یا تعویض
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            میانگین فاصله بین تعویض‌ها:
          </div>
          <div className="text-xl font-black text-slate-900 dark:text-slate-100 font-mono">
            {avgIntervalKm ? `${formatWithCommas(avgIntervalKm)} کیلومتر` : 'داده ناکافی'}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            {avgIntervalKm
              ? 'بر اساس سوابق متوالی ثبت شده'
              : 'حداقل نیاز به ۲ بار تعویض ثبت‌شده'}
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            موعد سرویس بعدی:
          </div>
          <div className="text-xl font-black text-cyan-600 dark:text-cyan-400 font-mono">
            {formatMileage(nextMileageDeadline)}
          </div>
          <div className="text-[11px] font-bold mt-1">
            {remainingKm > 0 ? (
              <span className="text-emerald-600">{formatWithCommas(remainingKm)} کیلومتر باقی‌مانده</span>
            ) : (
              <span className="text-rose-600">{formatWithCommas(Math.abs(remainingKm))} کیلومتر عقب افتاده!</span>
            )}
          </div>
        </div>
      </div>

      {/* Part Records Timeline / List */}
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-xs">
        <h3 className="font-extrabold text-slate-900 dark:text-slate-100 text-base mb-4">
          سوابق تعویض و تعمیرات «{selectedPartName}» ({partRecords.length} مورد)
        </h3>

        {partRecords.length === 0 ? (
          <div className="text-center py-10 text-slate-400 text-xs">
            هنوز هیچ سابقه سرویس یا تعویضی برای این قطعه در خودرو «{vehicle.name}» ثبت نشده است.
          </div>
        ) : (
          <div className="relative border-r-2 border-cyan-500/30 mr-3 pr-5 space-y-6">
            {partRecords.map((r, idx) => (
              <div key={r.id} className="relative">
                {/* Timeline node */}
                <div className="absolute -right-[27px] top-1.5 w-3.5 h-3.5 rounded-full bg-cyan-600 ring-4 ring-cyan-100 dark:ring-cyan-950" />

                <div className="bg-slate-50 dark:bg-slate-800/60 p-4 rounded-xl border border-slate-200 dark:border-slate-700/60 space-y-2">
                  <div className="flex flex-wrap items-center justify-between gap-2">
                    <div className="flex items-center gap-2">
                      <span className="text-xs font-bold text-cyan-700 dark:text-cyan-300 bg-cyan-50 dark:bg-cyan-950/60 px-2 py-0.5 rounded-md">
                        {r.operationType}
                      </span>
                      <span className="font-mono text-xs text-slate-500 dark:text-slate-400">
                        {formatToJalali(r.dateTimestamp, true)}
                      </span>
                    </div>

                    <div className="text-left font-black text-sm text-slate-900 dark:text-slate-100">
                      {formatCurrency(r.totalCost)}
                    </div>
                  </div>

                  <div className="grid grid-cols-2 sm:grid-cols-4 gap-2 text-xs text-slate-600 dark:text-slate-400 pt-1">
                    <div>
                      <span className="text-slate-400 block">کیلومتر:</span>
                      <strong className="text-slate-800 dark:text-slate-200 font-mono">
                        {formatMileage(r.mileage)}
                      </strong>
                    </div>
                    <div>
                      <span className="text-slate-400 block">قیمت قطعه:</span>
                      <strong className="text-slate-800 dark:text-slate-200">
                        {formatCurrency(r.partPrice)}
                      </strong>
                    </div>
                    <div>
                      <span className="text-slate-400 block">اجرت:</span>
                      <strong className="text-slate-800 dark:text-slate-200">
                        {formatCurrency(r.laborCost)}
                      </strong>
                    </div>
                    <div>
                      <span className="text-slate-400 block">تعمیرگاه:</span>
                      <strong className="text-slate-800 dark:text-slate-200">
                        {r.repairShop || '—'}
                      </strong>
                    </div>
                  </div>

                  {r.description && (
                    <p className="text-xs text-slate-600 dark:text-slate-300 pt-1 border-t border-slate-200 dark:border-slate-700">
                      {r.description}
                    </p>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};
