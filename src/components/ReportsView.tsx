import React, { useState, useMemo } from 'react';
import {
  BarChart3,
  FileSpreadsheet,
  Printer,
  Calendar,
  DollarSign,
  Fuel,
  Wrench,
  Percent,
  Download,
} from 'lucide-react';
import { Vehicle, ServiceRecord, FuelRecord, PartCategory } from '../types';
import { formatCurrency, formatMileage, formatWithCommas } from '../utils/formatters';
import { formatToJalali } from '../utils/persianDate';
import { calculateExpenses, calculateFuelConsumption } from '../utils/calculations';
import { exportToExcelCSV, exportToPrintableHTML } from '../utils/exporter';

interface ReportsViewProps {
  vehicle: Vehicle;
  serviceRecords: ServiceRecord[];
  fuelRecords: FuelRecord[];
}

export const ReportsView: React.FC<ReportsViewProps> = ({
  vehicle,
  serviceRecords,
  fuelRecords,
}) => {
  const [timeRange, setTimeRange] = useState<'all' | 'year' | 'month'>('all');
  const [categoryFilter, setCategoryFilter] = useState<string>('all');

  // Filter records by time
  const now = Date.now();
  const oneMonthAgo = now - 30 * 24 * 60 * 60 * 1000;
  const oneYearAgo = now - 365 * 24 * 60 * 60 * 1000;

  const filteredServices = useMemo(() => {
    return serviceRecords
      .filter((r) => r.vehicleId === vehicle.id)
      .filter((r) => {
        if (timeRange === 'month') return r.dateTimestamp >= oneMonthAgo;
        if (timeRange === 'year') return r.dateTimestamp >= oneYearAgo;
        return true;
      })
      .filter((r) => {
        if (categoryFilter === 'all') return true;
        return r.category === categoryFilter;
      });
  }, [serviceRecords, vehicle.id, timeRange, categoryFilter, oneMonthAgo, oneYearAgo]);

  const filteredFuel = useMemo(() => {
    if (categoryFilter !== 'all' && categoryFilter !== 'سوخت') return [];
    return fuelRecords
      .filter((r) => r.vehicleId === vehicle.id)
      .filter((r) => {
        if (timeRange === 'month') return r.dateTimestamp >= oneMonthAgo;
        if (timeRange === 'year') return r.dateTimestamp >= oneYearAgo;
        return true;
      });
  }, [fuelRecords, vehicle.id, timeRange, categoryFilter, oneMonthAgo, oneYearAgo]);

  // Calculations
  const expenseStats = calculateExpenses(filteredServices, filteredFuel, vehicle.id);
  const fuelStats = calculateFuelConsumption(filteredFuel, vehicle.id);

  const totalCost = expenseStats.totalCost;
  const partsCost = expenseStats.partsCost;
  const laborCost = expenseStats.laborCost;
  const fuelCost = expenseStats.fuelCost;

  // Percentage shares
  const partsPct = totalCost > 0 ? Math.round((partsCost / totalCost) * 100) : 0;
  const laborPct = totalCost > 0 ? Math.round((laborCost / totalCost) * 100) : 0;
  const fuelPct = totalCost > 0 ? Math.round((fuelCost / totalCost) * 100) : 0;

  // Top 5 most expensive services
  const topServices = [...filteredServices]
    .sort((a, b) => b.totalCost - a.totalCost)
    .slice(0, 5);

  // Handle Export
  const handleExportExcel = () => {
    exportToExcelCSV(vehicle, filteredServices, filteredFuel);
  };

  const handleExportPDF = () => {
    exportToPrintableHTML(vehicle, filteredServices, filteredFuel, expenseStats);
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <BarChart3 className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
            <span>گزارش‌ها، هزینه‌ها و خروجی اکسل/PDF - {vehicle.name}</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            تحلیل جامع مخارج قطعات، اجرت و سوخت به تفکیک دسته‌بندی و دریافت فایل گزارش رسمی
          </p>
        </div>

        {/* Export Buttons */}
        <div className="flex items-center gap-2">
          <button
            onClick={handleExportExcel}
            className="flex items-center gap-1.5 bg-emerald-700 hover:bg-emerald-800 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-xs transition"
          >
            <FileSpreadsheet className="w-4 h-4" />
            <span>خروجی اکسل (Excel / CSV)</span>
          </button>

          <button
            onClick={handleExportPDF}
            className="flex items-center gap-1.5 bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-xs transition"
          >
            <Printer className="w-4 h-4" />
            <span>گزارش چاپی / PDF</span>
          </button>
        </div>
      </div>

      {/* Filter Row */}
      <div className="bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs flex flex-wrap items-center gap-3">
        <div className="flex items-center gap-2">
          <span className="text-xs font-bold text-slate-700 dark:text-slate-300">بازه زمانی:</span>
          <div className="flex rounded-xl p-1 bg-slate-100 dark:bg-slate-800 text-xs font-bold">
            <button
              onClick={() => setTimeRange('all')}
              className={`px-3 py-1.5 rounded-lg transition ${timeRange === 'all' ? 'bg-cyan-600 text-white shadow-xs' : 'text-slate-600 dark:text-slate-400'}`}
            >
              همه زمان‌ها
            </button>
            <button
              onClick={() => setTimeRange('year')}
              className={`px-3 py-1.5 rounded-lg transition ${timeRange === 'year' ? 'bg-cyan-600 text-white shadow-xs' : 'text-slate-600 dark:text-slate-400'}`}
            >
              یک سال اخیر
            </button>
            <button
              onClick={() => setTimeRange('month')}
              className={`px-3 py-1.5 rounded-lg transition ${timeRange === 'month' ? 'bg-cyan-600 text-white shadow-xs' : 'text-slate-600 dark:text-slate-400'}`}
            >
              ماه جاری (۳۰ روز)
            </button>
          </div>
        </div>

        <div className="flex items-center gap-2 mr-auto">
          <span className="text-xs font-bold text-slate-700 dark:text-slate-300">دسته‌بندی:</span>
          <select
            value={categoryFilter}
            onChange={(e) => setCategoryFilter(e.target.value)}
            className="px-3 py-1.5 text-xs rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
          >
            <option value="all">همه دسته‌ها</option>
            <option value="موتور">موتور</option>
            <option value="ترمز">ترمز</option>
            <option value="جلوبندی و تعلیق">جلوبندی و تعلیق</option>
            <option value="سرویس‌های دوره‌ای">سرویس‌های دوره‌ای</option>
            <option value="سوخت">سوخت و بنزین</option>
            <option value="سایر">سایر</option>
          </select>
        </div>
      </div>

      {/* Main Expense Stats Cards */}
      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs text-slate-500 mb-1">مجموع کل هزینه‌ها:</div>
          <div className="text-2xl font-black text-slate-900 dark:text-slate-100">
            {formatCurrency(totalCost)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            شامل قطعات، اجرت و کل سوخت
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs text-slate-500 mb-1">هزینه قطعات و لوازم:</div>
          <div className="text-2xl font-black text-cyan-600 dark:text-cyan-400">
            {formatCurrency(partsCost)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            سهم از کل: {partsPct}٪
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs text-slate-500 mb-1">اجرت و دستمزد مکانیک:</div>
          <div className="text-2xl font-black text-amber-600 dark:text-amber-400">
            {formatCurrency(laborCost)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            سهم از کل: {laborPct}٪
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs text-slate-500 mb-1">هزینه بنزین و سوخت:</div>
          <div className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
            {formatCurrency(fuelCost)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            سهم از کل: {fuelPct}٪
          </div>
        </div>
      </div>

      {/* Visual Cost Breakdown Bar */}
      <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-4">
        <h3 className="font-extrabold text-sm text-slate-900 dark:text-slate-100">
          ترکیب مخارج (قطعات در برابر اجرت در برابر سوخت)
        </h3>

        {totalCost > 0 ? (
          <div className="space-y-3">
            <div className="w-full h-4 rounded-full overflow-hidden flex bg-slate-100 dark:bg-slate-800">
              <div
                style={{ width: `${partsPct}%` }}
                className="bg-cyan-600 h-full transition-all"
                title={`قطعات: ${partsPct}%`}
              />
              <div
                style={{ width: `${laborPct}%` }}
                className="bg-amber-500 h-full transition-all"
                title={`اجرت: ${laborPct}%`}
              />
              <div
                style={{ width: `${fuelPct}%` }}
                className="bg-emerald-500 h-full transition-all"
                title={`سوخت: ${fuelPct}%`}
              />
            </div>

            <div className="flex flex-wrap items-center gap-6 text-xs font-bold pt-1">
              <div className="flex items-center gap-2">
                <span className="w-3 h-3 rounded-md bg-cyan-600" />
                <span className="text-slate-700 dark:text-slate-300">
                  لوازم و قطعات ({partsPct}٪ - {formatCurrency(partsCost)})
                </span>
              </div>

              <div className="flex items-center gap-2">
                <span className="w-3 h-3 rounded-md bg-amber-500" />
                <span className="text-slate-700 dark:text-slate-300">
                  اجرت و دستمزد ({laborPct}٪ - {formatCurrency(laborCost)})
                </span>
              </div>

              <div className="flex items-center gap-2">
                <span className="w-3 h-3 rounded-md bg-emerald-500" />
                <span className="text-slate-700 dark:text-slate-300">
                  سوخت و بنزین ({fuelPct}٪ - {formatCurrency(fuelCost)})
                </span>
              </div>
            </div>
          </div>
        ) : (
          <div className="text-xs text-slate-400 py-4 text-center">
            هنوز هزینه‌ای در بازه انتخابی ثبت نشده است.
          </div>
        )}
      </div>

      {/* Two Column: Category Breakdown & Top Expensive Services */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Category Breakdown */}
        <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-4">
          <h3 className="font-extrabold text-sm text-slate-900 dark:text-slate-100">
            مخارج بر اساس دسته‌بندی قطعات
          </h3>

          <div className="space-y-3">
            {Object.entries(expenseStats.byCategory).map(([cat, amount]) => {
              const pct = totalCost > 0 ? Math.round((amount / totalCost) * 100) : 0;
              return (
                <div key={cat} className="space-y-1">
                  <div className="flex justify-between text-xs">
                    <span className="font-bold text-slate-700 dark:text-slate-300">{cat}</span>
                    <span className="font-mono text-slate-900 dark:text-slate-100 font-bold">
                      {formatCurrency(amount)} ({pct}٪)
                    </span>
                  </div>
                  <div className="w-full h-2 rounded-full bg-slate-100 dark:bg-slate-800 overflow-hidden">
                    <div
                      style={{ width: `${pct}%` }}
                      className="bg-cyan-500 h-full rounded-full transition-all"
                    />
                  </div>
                </div>
              );
            })}

            {Object.keys(expenseStats.byCategory).length === 0 && (
              <div className="text-xs text-slate-400 text-center py-4">داده‌ای موجود نیست.</div>
            )}
          </div>
        </div>

        {/* Top 5 Most Expensive Services */}
        <div className="bg-white dark:bg-slate-900 p-6 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-4">
          <h3 className="font-extrabold text-sm text-slate-900 dark:text-slate-100">
            گران‌ترین سرویس‌ها و قطعات تعویض‌شده
          </h3>

          <div className="space-y-3">
            {topServices.map((rec, i) => (
              <div
                key={rec.id}
                className="p-3 rounded-xl bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800 flex items-center justify-between text-xs"
              >
                <div className="flex items-center gap-2">
                  <span className="w-5 h-5 rounded-full bg-cyan-100 dark:bg-cyan-950 text-cyan-700 dark:text-cyan-300 font-bold flex items-center justify-center text-[11px]">
                    {i + 1}
                  </span>
                  <div>
                    <div className="font-bold text-slate-800 dark:text-slate-200">{rec.partName}</div>
                    <div className="text-[10px] text-slate-400">{formatToJalali(rec.dateTimestamp)} • {formatMileage(rec.mileage)}</div>
                  </div>
                </div>

                <div className="font-black text-sm text-slate-900 dark:text-slate-100">
                  {formatCurrency(rec.totalCost)}
                </div>
              </div>
            ))}

            {topServices.length === 0 && (
              <div className="text-xs text-slate-400 text-center py-4">سرویسی ثبت نشده است.</div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
