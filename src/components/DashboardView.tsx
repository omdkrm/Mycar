import React from 'react';
import {
  Gauge,
  Fuel,
  Wrench,
  AlertCircle,
  Clock,
  CheckCircle2,
  DollarSign,
  Plus,
  ArrowLeft,
  ChevronLeft,
  Car,
  Calendar,
  Layers,
  BarChart3,
  FileSpreadsheet,
} from 'lucide-react';
import { Vehicle, ReminderItem, ServiceRecord, FuelRecord } from '../types';
import { formatMileage, formatCurrency, formatYear, formatConsumption, formatWithCommas } from '../utils/formatters';
import { formatToJalali } from '../utils/persianDate';
import { calculateFuelConsumption, calculateExpenses } from '../utils/calculations';

interface DashboardViewProps {
  vehicle: Vehicle;
  reminders: ReminderItem[];
  serviceRecords: ServiceRecord[];
  fuelRecords: FuelRecord[];
  onOpenMileageModal: () => void;
  onOpenRegisterService: () => void;
  onOpenRegisterFuel: () => void;
  onNavigateTab: (tab: any) => void;
  onCompleteReminder: (reminder: ReminderItem) => void;
}

export const DashboardView: React.FC<DashboardViewProps> = ({
  vehicle,
  reminders,
  serviceRecords,
  fuelRecords,
  onOpenMileageModal,
  onOpenRegisterService,
  onOpenRegisterFuel,
  onNavigateTab,
  onCompleteReminder,
}) => {
  const fuelStats = calculateFuelConsumption(fuelRecords, vehicle.id);
  const expenseStats = calculateExpenses(serviceRecords, fuelRecords, vehicle.id);

  const vehicleServices = serviceRecords
    .filter((r) => r.vehicleId === vehicle.id)
    .sort((a, b) => b.dateTimestamp - a.dateTimestamp);

  const vehicleFuel = fuelRecords
    .filter((r) => r.vehicleId === vehicle.id)
    .sort((a, b) => b.dateTimestamp - a.dateTimestamp);

  const overdueReminders = reminders.filter((r) => r.status === 'overdue');
  const approachingReminders = reminders.filter((r) => r.status === 'approaching');
  const healthyReminders = reminders.filter((r) => r.status === 'healthy');

  return (
    <div className="space-y-6">
      {/* Vehicle Hero Header */}
      <div className="bg-gradient-to-l from-cyan-900 via-slate-900 to-slate-950 text-white rounded-3xl p-6 sm:p-8 shadow-xl relative overflow-hidden border border-cyan-800/40">
        <div className="absolute top-0 right-0 w-96 h-96 bg-cyan-500/10 rounded-full blur-3xl -mr-20 -mt-20 pointer-events-none"></div>

        <div className="relative z-10 flex flex-col md:flex-row md:items-center justify-between gap-6">
          <div>
            <div className="flex items-center gap-2 mb-2">
              <span className="px-2.5 py-1 rounded-md bg-cyan-500/20 text-cyan-300 text-xs font-bold border border-cyan-500/30">
                {vehicle.brand}
              </span>
              <span className="text-xs text-slate-400 font-mono">
                سال ساخت: {formatYear(vehicle.year)}
              </span>
              {vehicle.trim && (
                <span className="text-xs text-slate-400">
                  • {vehicle.trim}
                </span>
              )}
            </div>

            <h1 className="text-2xl sm:text-3xl font-black text-white flex items-center gap-3">
              <span>{vehicle.name}</span>
            </h1>

            {vehicle.licensePlate && (
              <div className="mt-3 inline-flex items-center gap-2 bg-white text-slate-900 px-3 py-1 rounded-md font-mono text-xs font-bold border border-slate-300">
                <span>پلاک:</span>
                <span>{vehicle.licensePlate}</span>
              </div>
            )}
          </div>

          {/* Quick Mileage display & action */}
          <div className="bg-white/10 backdrop-blur-md rounded-2xl p-4 sm:p-5 border border-white/10 flex flex-col sm:items-end gap-2">
            <span className="text-xs text-cyan-200">کیلومتر فعلی خودرو:</span>
            <div className="text-2xl sm:text-3xl font-black font-mono text-cyan-300">
              {formatMileage(vehicle.currentMileage)}
            </div>
            <button
              onClick={onOpenMileageModal}
              className="mt-1 flex items-center gap-1.5 bg-cyan-500 hover:bg-cyan-400 text-slate-950 font-extrabold text-xs px-4 py-2 rounded-xl transition shadow-md active:scale-98"
            >
              <Gauge className="w-4 h-4" />
              <span>به‌روزرسانی کیلومتر</span>
            </button>
          </div>
        </div>

        {/* Quick Actions Row */}
        <div className="grid grid-cols-3 gap-2 sm:gap-3 mt-6 pt-6 border-t border-white/10">
          <button
            onClick={onOpenRegisterService}
            className="flex flex-col sm:flex-row items-center justify-center gap-2 bg-cyan-600/30 hover:bg-cyan-600/50 border border-cyan-400/30 text-cyan-100 py-2.5 px-3 rounded-xl text-xs sm:text-sm font-bold transition"
          >
            <Wrench className="w-4 h-4 text-cyan-300" />
            <span>ثبت سرویس جدید</span>
          </button>

          <button
            onClick={onOpenRegisterFuel}
            className="flex flex-col sm:flex-row items-center justify-center gap-2 bg-emerald-600/20 hover:bg-emerald-600/40 border border-emerald-400/30 text-emerald-100 py-2.5 px-3 rounded-xl text-xs sm:text-sm font-bold transition"
          >
            <Fuel className="w-4 h-4 text-emerald-300" />
            <span>ثبت سوخت‌گیری</span>
          </button>

          <button
            onClick={() => onNavigateTab('reports')}
            className="flex flex-col sm:flex-row items-center justify-center gap-2 bg-white/10 hover:bg-white/20 border border-white/10 text-white py-2.5 px-3 rounded-xl text-xs sm:text-sm font-bold transition"
          >
            <BarChart3 className="w-4 h-4 text-slate-300" />
            <span>گزارش هزینه‌ها</span>
          </button>
        </div>
      </div>

      {/* Metric Cards Grid */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        {/* Total Expenses */}
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 text-xs font-semibold">
            <span>مجموع هزینه‌های خودرو</span>
            <DollarSign className="w-4 h-4 text-cyan-600" />
          </div>
          <div className="my-2">
            <div className="text-xl sm:text-2xl font-black text-slate-900 dark:text-slate-100">
              {formatCurrency(expenseStats.totalCost)}
            </div>
            <div className="text-[11px] text-slate-400 mt-1">
              قطعات: {formatCurrency(expenseStats.partsCost)} • اجرت: {formatCurrency(expenseStats.laborCost)}
            </div>
          </div>
          <button
            onClick={() => onNavigateTab('reports')}
            className="text-[11px] font-bold text-cyan-600 dark:text-cyan-400 hover:underline flex items-center gap-1 self-start"
          >
            <span>مشاهده جزئیات گزارش</span>
            <ChevronLeft className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Fuel Consumption */}
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-col justify-between">
          <div className="flex items-center justify-between text-slate-500 dark:text-slate-400 text-xs font-semibold">
            <span>میانگین مصرف سوخت</span>
            <Fuel className="w-4 h-4 text-emerald-600" />
          </div>
          <div className="my-2">
            <div className="text-xl sm:text-2xl font-black text-slate-900 dark:text-slate-100 font-mono">
              {formatConsumption(fuelStats.averageLitersPer100Km)}
            </div>
            <div className="text-[11px] text-slate-400 mt-1">
              کل سوخت ثبت‌شده: {fuelStats.totalFuelLiters} لیتر ({fuelStats.recordsCount} بار)
            </div>
          </div>
          <button
            onClick={() => onNavigateTab('fuel')}
            className="text-[11px] font-bold text-emerald-600 dark:text-emerald-400 hover:underline flex items-center gap-1 self-start"
          >
            <span>مدیریت سوخت‌گیری</span>
            <ChevronLeft className="w-3.5 h-3.5" />
          </button>
        </div>

        {/* Overdue Maintenance */}
        <div
          onClick={() => onNavigateTab('reminders')}
          className={`p-5 rounded-2xl border cursor-pointer transition shadow-xs flex flex-col justify-between ${
            overdueReminders.length > 0
              ? 'bg-rose-50/70 dark:bg-rose-950/30 border-rose-200 dark:border-rose-900'
              : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
          }`}
        >
          <div className="flex items-center justify-between text-xs font-semibold">
            <span className={overdueReminders.length > 0 ? 'text-rose-700 dark:text-rose-300' : 'text-slate-500 dark:text-slate-400'}>
              سرویس‌های عقب‌افتاده
            </span>
            <AlertCircle className={`w-4 h-4 ${overdueReminders.length > 0 ? 'text-rose-600 animate-pulse' : 'text-slate-400'}`} />
          </div>
          <div className="my-2">
            <div className={`text-2xl font-black ${overdueReminders.length > 0 ? 'text-rose-600 dark:text-rose-400' : 'text-slate-900 dark:text-slate-100'}`}>
              {overdueReminders.length} مورد
            </div>
            <div className="text-[11px] text-slate-500 mt-1">
              {overdueReminders.length > 0 ? 'نیاز فوری به رسیدگی و سرویس' : 'وضعیت عادی و بدون تاخیر'}
            </div>
          </div>
          <span className="text-[11px] font-bold text-rose-600 dark:text-rose-400 flex items-center gap-1">
            <span>مشاهده یادآوری‌ها</span>
            <ChevronLeft className="w-3.5 h-3.5" />
          </span>
        </div>

        {/* Upcoming Maintenance */}
        <div
          onClick={() => onNavigateTab('reminders')}
          className={`p-5 rounded-2xl border cursor-pointer transition shadow-xs flex flex-col justify-between ${
            approachingReminders.length > 0
              ? 'bg-amber-50/70 dark:bg-amber-950/30 border-amber-200 dark:border-amber-900'
              : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
          }`}
        >
          <div className="flex items-center justify-between text-xs font-semibold">
            <span className={approachingReminders.length > 0 ? 'text-amber-700 dark:text-amber-300' : 'text-slate-500 dark:text-slate-400'}>
              سرویس‌های نزدیک
            </span>
            <Clock className={`w-4 h-4 ${approachingReminders.length > 0 ? 'text-amber-600' : 'text-slate-400'}`} />
          </div>
          <div className="my-2">
            <div className={`text-2xl font-black ${approachingReminders.length > 0 ? 'text-amber-600 dark:text-amber-400' : 'text-slate-900 dark:text-slate-100'}`}>
              {approachingReminders.length} مورد
            </div>
            <div className="text-[11px] text-slate-500 mt-1">
              {approachingReminders.length > 0 ? 'نزدیک به کیلومتر یا موعد مقرر' : 'هیچ سرویسی در آستانه نیست'}
            </div>
          </div>
          <span className="text-[11px] font-bold text-amber-600 dark:text-amber-400 flex items-center gap-1">
            <span>مشاهده برنامه‌ها</span>
            <ChevronLeft className="w-3.5 h-3.5" />
          </span>
        </div>
      </div>

      {/* Two Column Layout: Reminders & Recent Services */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Urgent & Upcoming Reminders Section */}
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-xs">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
            <div className="flex items-center gap-2">
              <Clock className="w-5 h-5 text-cyan-600 dark:text-cyan-400" />
              <h3 className="font-extrabold text-slate-900 dark:text-slate-100 text-sm sm:text-base">
                یادآوری‌ها و وضعیت سرویس‌ها
              </h3>
            </div>
            <button
              onClick={() => onNavigateTab('reminders')}
              className="text-xs font-bold text-cyan-600 dark:text-cyan-400 hover:underline flex items-center gap-1"
            >
              <span>همه یادآوری‌ها</span>
              <ChevronLeft className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="mt-4 space-y-3">
            {reminders.slice(0, 4).map((rem) => {
              const isOverdue = rem.status === 'overdue';
              const isApproaching = rem.status === 'approaching';

              return (
                <div
                  key={rem.id}
                  className={`p-3.5 rounded-xl border flex items-center justify-between gap-3 transition ${
                    isOverdue
                      ? 'bg-rose-50/80 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900'
                      : isApproaching
                      ? 'bg-amber-50/80 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900'
                      : 'bg-emerald-50/60 dark:bg-emerald-950/30 border-emerald-200 dark:border-emerald-900'
                  }`}
                >
                  <div className="space-y-1 flex-1">
                    <div className="flex items-center gap-2">
                      <span
                        className={`w-2.5 h-2.5 rounded-full ${
                          isOverdue ? 'bg-rose-500' : isApproaching ? 'bg-amber-500' : 'bg-emerald-500'
                        }`}
                      />
                      <span className="font-bold text-xs sm:text-sm text-slate-900 dark:text-slate-100">
                        {rem.partName}
                      </span>
                      <span className="text-[10px] text-slate-400 px-1.5 py-0.5 rounded-md bg-white/70 dark:bg-slate-800">
                        {rem.category}
                      </span>
                    </div>
                    <div
                      className={`text-xs font-semibold ${
                        isOverdue
                          ? 'text-rose-600 dark:text-rose-400'
                          : isApproaching
                          ? 'text-amber-600 dark:text-amber-400'
                          : 'text-emerald-700 dark:text-emerald-400'
                      }`}
                    >
                      {rem.statusMessage}
                    </div>
                  </div>

                  <button
                    onClick={() => onCompleteReminder(rem)}
                    className="shrink-0 bg-white dark:bg-slate-800 hover:bg-cyan-600 hover:text-white dark:hover:bg-cyan-600 text-slate-700 dark:text-slate-200 text-xs font-bold py-1.5 px-3 rounded-lg border border-slate-200 dark:border-slate-700 shadow-2xs transition"
                  >
                    انجام شد
                  </button>
                </div>
              );
            })}

            {reminders.length === 0 && (
              <div className="text-center py-6 text-slate-400 text-xs">
                در حال حاضر یادآوری فعالی ندارید.
              </div>
            )}
          </div>
        </div>

        {/* Recent Service Records Section */}
        <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-5 shadow-xs">
          <div className="flex items-center justify-between pb-3 border-b border-slate-100 dark:border-slate-800">
            <div className="flex items-center gap-2">
              <Wrench className="w-5 h-5 text-cyan-600 dark:text-cyan-400" />
              <h3 className="font-extrabold text-slate-900 dark:text-slate-100 text-sm sm:text-base">
                آخرین سرویس‌های ثبت‌شده
              </h3>
            </div>
            <button
              onClick={() => onNavigateTab('service-history')}
              className="text-xs font-bold text-cyan-600 dark:text-cyan-400 hover:underline flex items-center gap-1"
            >
              <span>تاریخچه کامل</span>
              <ChevronLeft className="w-3.5 h-3.5" />
            </button>
          </div>

          <div className="mt-4 space-y-3">
            {vehicleServices.slice(0, 4).map((rec) => (
              <div
                key={rec.id}
                className="p-3.5 rounded-xl border border-slate-100 dark:border-slate-800 bg-slate-50/60 dark:bg-slate-800/40 flex items-center justify-between gap-3"
              >
                <div className="space-y-0.5">
                  <div className="flex items-center gap-2">
                    <span className="font-bold text-xs sm:text-sm text-slate-900 dark:text-slate-100">
                      {rec.partName}
                    </span>
                    <span className="text-[10px] font-bold text-cyan-700 dark:text-cyan-300 bg-cyan-50 dark:bg-cyan-950/60 px-2 py-0.5 rounded-md">
                      {rec.operationType}
                    </span>
                  </div>
                  <div className="text-xs text-slate-500 dark:text-slate-400 flex items-center gap-2">
                    <span>{formatToJalali(rec.dateTimestamp)}</span>
                    <span>•</span>
                    <span className="font-mono">{formatMileage(rec.mileage)}</span>
                  </div>
                </div>

                <div className="text-left">
                  <div className="text-xs sm:text-sm font-extrabold text-slate-900 dark:text-slate-100">
                    {formatCurrency(rec.totalCost)}
                  </div>
                  {rec.repairShop && (
                    <div className="text-[10px] text-slate-400 truncate max-w-[120px]">
                      {rec.repairShop}
                    </div>
                  )}
                </div>
              </div>
            ))}

            {vehicleServices.length === 0 && (
              <div className="text-center py-6 text-slate-400 text-xs">
                هنوز سابقه‌ای ثبت نشده است.
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
