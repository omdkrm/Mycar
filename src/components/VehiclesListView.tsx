import React from 'react';
import { Plus, Car, Calendar, Gauge, AlertCircle, Clock, Trash2, Edit3, ArrowLeft } from 'lucide-react';
import { Vehicle, ReminderItem } from '../types';
import { formatMileage, formatYear } from '../utils/formatters';

interface VehiclesListViewProps {
  vehicles: Vehicle[];
  selectedVehicleId: string | null;
  onSelectVehicle: (id: string) => void;
  onOpenDashboard: (id: string) => void;
  onAddVehicle: () => void;
  onEditVehicle: (vehicle: Vehicle) => void;
  onDeleteVehicle: (id: string, name: string) => void;
  remindersByVehicle: Record<string, ReminderItem[]>;
}

export const VehiclesListView: React.FC<VehiclesListViewProps> = ({
  vehicles,
  selectedVehicleId,
  onSelectVehicle,
  onOpenDashboard,
  onAddVehicle,
  onEditVehicle,
  onDeleteVehicle,
  remindersByVehicle,
}) => {
  return (
    <div className="space-y-6">
      {/* Top Banner / Title */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 bg-gradient-to-l from-cyan-600/10 via-cyan-500/5 to-transparent p-6 rounded-2xl border border-cyan-100 dark:border-cyan-950/60">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Car className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
            <span>خودروهای من</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            مدیریت ناوگان شخصی، سوابق سرویس‌ها، یادآوری‌ها و مخارج هر خودرو به صورت کاملاً مستقل
          </p>
        </div>
        <button
          onClick={onAddVehicle}
          className="self-start sm:self-auto flex items-center gap-2 bg-cyan-600 hover:bg-cyan-700 text-white font-bold px-5 py-2.5 rounded-xl shadow-md shadow-cyan-600/20 transition active:scale-98"
        >
          <Plus className="w-5 h-5" />
          <span>+ افزودن خودرو جدید</span>
        </button>
      </div>

      {/* Empty State */}
      {vehicles.length === 0 ? (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center space-y-4 shadow-xs">
          <div className="w-16 h-16 rounded-full bg-cyan-50 dark:bg-cyan-950 text-cyan-600 dark:text-cyan-400 flex items-center justify-center mx-auto">
            <Car className="w-8 h-8" />
          </div>
          <div className="space-y-1">
            <h3 className="text-lg font-bold text-slate-800 dark:text-slate-200">
              هنوز خودرویی ثبت نکرده‌اید.
            </h3>
            <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 max-w-md mx-auto">
              برای شروع، اولین خودروی خود را اضافه کنید تا بتوانید کیلومتر، سرویس‌ها، سوخت و یادآوری‌ها را مدیریت کنید.
            </p>
          </div>
          <button
            onClick={onAddVehicle}
            className="inline-flex items-center gap-2 bg-cyan-600 hover:bg-cyan-700 text-white font-bold px-6 py-2.5 rounded-xl shadow-md transition"
          >
            <Plus className="w-4 h-4" />
            <span>+ افزودن خودرو</span>
          </button>
        </div>
      ) : (
        /* Vehicles Grid */
        <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-5">
          {vehicles.map((v) => {
            const isSelected = v.id === selectedVehicleId;
            const vehicleReminders = remindersByVehicle[v.id] || [];
            const overdueCount = vehicleReminders.filter((r) => r.status === 'overdue').length;
            const upcomingCount = vehicleReminders.filter((r) => r.status === 'approaching').length;

            return (
              <div
                key={v.id}
                className={`bg-white dark:bg-slate-900 rounded-2xl border transition-all duration-200 overflow-hidden flex flex-col shadow-xs hover:shadow-md ${
                  isSelected
                    ? 'border-cyan-500 ring-2 ring-cyan-500/20 shadow-cyan-500/10'
                    : 'border-slate-200 dark:border-slate-800 hover:border-slate-300 dark:hover:border-slate-700'
                }`}
              >
                {/* Card Top / Header */}
                <div className="p-5 flex-1">
                  <div className="flex items-start justify-between gap-3">
                    <div>
                      <div className="flex items-center gap-2">
                        <h3 className="text-base font-extrabold text-slate-900 dark:text-slate-100 line-clamp-1">
                          {v.name}
                        </h3>
                        {isSelected && (
                          <span className="text-[10px] font-bold bg-cyan-100 dark:bg-cyan-950/80 text-cyan-700 dark:text-cyan-300 px-2 py-0.5 rounded-md">
                            فعال
                          </span>
                        )}
                      </div>
                      <div className="text-xs text-slate-500 dark:text-slate-400 mt-1 font-medium">
                        {v.brand} {v.model} {v.trim ? `• ${v.trim}` : ''}
                      </div>
                    </div>

                    {/* Actions Menu */}
                    <div className="flex items-center gap-1">
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onEditVehicle(v);
                        }}
                        className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                        title="ویرایش خودرو"
                      >
                        <Edit3 className="w-4 h-4" />
                      </button>
                      <button
                        onClick={(e) => {
                          e.stopPropagation();
                          onDeleteVehicle(v.id, v.name);
                        }}
                        className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition"
                        title="حذف خودرو"
                      >
                        <Trash2 className="w-4 h-4" />
                      </button>
                    </div>
                  </div>

                  {/* Specs row */}
                  <div className="grid grid-cols-2 gap-2 mt-4 pt-3 border-t border-slate-100 dark:border-slate-800/80 text-xs">
                    <div className="flex items-center gap-1.5 text-slate-600 dark:text-slate-400">
                      <Calendar className="w-4 h-4 text-slate-400" />
                      <span>سال:</span>
                      <strong className="text-slate-900 dark:text-slate-200 font-mono">
                        {formatYear(v.year)}
                      </strong>
                    </div>

                    <div className="flex items-center gap-1.5 text-slate-600 dark:text-slate-400">
                      <Gauge className="w-4 h-4 text-cyan-500" />
                      <span>کارکرد:</span>
                      <strong className="text-slate-900 dark:text-slate-200 font-bold">
                        {formatMileage(v.currentMileage)}
                      </strong>
                    </div>
                  </div>

                  {/* Badges for overdue and upcoming services */}
                  <div className="flex items-center gap-2 mt-4">
                    <div
                      className={`flex-1 flex items-center justify-center gap-1.5 py-1.5 px-2 rounded-xl text-xs font-bold ${
                        overdueCount > 0
                          ? 'bg-rose-50 dark:bg-rose-950/50 text-rose-600 dark:text-rose-400 border border-rose-200 dark:border-rose-900'
                          : 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400'
                      }`}
                    >
                      <AlertCircle className="w-3.5 h-3.5" />
                      <span>
                        {overdueCount > 0 ? `${overdueCount} سرویس عقب افتاده` : 'بدون تاخیر'}
                      </span>
                    </div>

                    <div
                      className={`flex-1 flex items-center justify-center gap-1.5 py-1.5 px-2 rounded-xl text-xs font-bold ${
                        upcomingCount > 0
                          ? 'bg-amber-50 dark:bg-amber-950/50 text-amber-600 dark:text-amber-400 border border-amber-200 dark:border-amber-900'
                          : 'bg-slate-100 dark:bg-slate-800 text-slate-500 dark:text-slate-400'
                      }`}
                    >
                      <Clock className="w-3.5 h-3.5" />
                      <span>{upcomingCount} در نوبت سرویس</span>
                    </div>
                  </div>
                </div>

                {/* Card Action Button */}
                <div className="p-3 bg-slate-50 dark:bg-slate-800/50 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between">
                  <button
                    onClick={() => {
                      onSelectVehicle(v.id);
                      onOpenDashboard(v.id);
                    }}
                    className="w-full flex items-center justify-center gap-2 bg-white dark:bg-slate-800 hover:bg-cyan-600 hover:text-white dark:hover:bg-cyan-600 text-cyan-600 dark:text-cyan-400 border border-cyan-200 dark:border-cyan-900 font-bold py-2 px-3 rounded-xl transition text-xs sm:text-sm group"
                  >
                    <span>مشاهده داشبورد خودرو</span>
                    <ArrowLeft className="w-4 h-4 transform group-hover:-translate-x-1 transition-transform" />
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};
