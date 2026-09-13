import React, { useState } from 'react';
import { Settings, Plus, Wrench, Check, Edit3, Trash2, X, Power } from 'lucide-react';
import { Vehicle, VehicleMaintenanceSchedule, CatalogItem, PartCategory } from '../types';
import { formatWithCommas, parseNumericInput, normalizeDigits } from '../utils/formatters';

interface SchedulesViewProps {
  vehicle: Vehicle;
  schedules: VehicleMaintenanceSchedule[];
  catalogItems: CatalogItem[];
  onSaveSchedule: (schedule: Partial<VehicleMaintenanceSchedule>) => { success: boolean; message?: string };
  onToggleSchedule: (id: string, isEnabled: boolean) => void;
  onDeleteSchedule: (id: string) => void;
}

export const SchedulesView: React.FC<SchedulesViewProps> = ({
  vehicle,
  schedules,
  catalogItems,
  onSaveSchedule,
  onToggleSchedule,
  onDeleteSchedule,
}) => {
  const [editingSchedule, setEditingSchedule] = useState<VehicleMaintenanceSchedule | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);

  // Modal fields
  const [partName, setPartName] = useState('');
  const [category, setCategory] = useState<PartCategory>('موتور');
  const [kmIntervalStr, setKmIntervalStr] = useState('8,000');
  const [monthIntervalStr, setMonthIntervalStr] = useState('6');
  const [description, setDescription] = useState('');
  const [modalError, setModalError] = useState('');

  const vehicleSchedules = schedules.filter((s) => s.vehicleId === vehicle.id);

  const handleOpenModal = (sched?: VehicleMaintenanceSchedule) => {
    if (sched) {
      setEditingSchedule(sched);
      setPartName(sched.partName);
      setCategory(sched.category);
      setKmIntervalStr(formatWithCommas(sched.kmInterval));
      setMonthIntervalStr(String(sched.timeIntervalMonths));
      setDescription(sched.description || '');
    } else {
      setEditingSchedule(null);
      setPartName('');
      setCategory('موتور');
      setKmIntervalStr('8,000');
      setMonthIntervalStr('6');
      setDescription('');
    }
    setModalError('');
    setIsModalOpen(true);
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setModalError('');

    if (!partName.trim()) {
      setModalError('لطفا نام قطعه یا سرویس را وارد کنید.');
      return;
    }

    const km = parseNumericInput(kmIntervalStr);
    const months = parseInt(normalizeDigits(monthIntervalStr), 10);

    if (km <= 0 || isNaN(km)) {
      setModalError('فاصله کیلومتر معتبر نیست.');
      return;
    }
    if (months <= 0 || isNaN(months)) {
      setModalError('فاصله زمانی (ماه) معتبر نیست.');
      return;
    }

    const res = onSaveSchedule({
      id: editingSchedule ? editingSchedule.id : undefined,
      vehicleId: vehicle.id,
      partName: partName.trim(),
      category,
      kmInterval: km,
      timeIntervalMonths: months,
      description: description.trim() || undefined,
      isEnabled: editingSchedule ? editingSchedule.isEnabled : true,
    });

    if (res.success) {
      setIsModalOpen(false);
    } else {
      setModalError(res.message || 'خطا در ثبت دوره سرویس.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Settings className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
            <span>تنظیم دوره‌های سرویس - {vehicle.name}</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            سفارشی‌سازی فواصل تعویض، پیمایش و زمان هر قطعه بر اساس شرایط رانندگی و کاتالوگ
          </p>
        </div>

        <button
          onClick={() => handleOpenModal()}
          className="self-start sm:self-auto bg-cyan-600 hover:bg-cyan-700 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-xs transition flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          <span>+ افزودن سرویس دوره‌ای سفارشی</span>
        </button>
      </div>

      {/* Schedules Grid */}
      <div className="grid grid-cols-1 md:grid-cols-2 xl:grid-cols-3 gap-4">
        {vehicleSchedules.map((sched) => (
          <div
            key={sched.id}
            className={`p-5 rounded-2xl border transition shadow-xs flex flex-col justify-between ${
              sched.isEnabled
                ? 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
                : 'bg-slate-50/70 dark:bg-slate-900/40 border-slate-200 dark:border-slate-800 opacity-60'
            }`}
          >
            <div>
              <div className="flex items-start justify-between gap-3">
                <div>
                  <h3 className="font-extrabold text-base text-slate-900 dark:text-slate-100">
                    {sched.partName}
                  </h3>
                  <span className="text-[10px] text-cyan-700 dark:text-cyan-300 bg-cyan-50 dark:bg-cyan-950/60 px-2 py-0.5 rounded-md font-bold">
                    {sched.category}
                  </span>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => onToggleSchedule(sched.id, !sched.isEnabled)}
                    className={`p-1.5 rounded-lg transition ${
                      sched.isEnabled
                        ? 'text-emerald-600 hover:bg-emerald-50 dark:hover:bg-emerald-950/40'
                        : 'text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-800'
                    }`}
                    title={sched.isEnabled ? 'غیرفعال‌سازی یادآوری این قطعه' : 'فعال‌سازی مجدد'}
                  >
                    <Power className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => handleOpenModal(sched)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800"
                    title="ویرایش فاصله کیلومتر و زمان"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => onDeleteSchedule(sched.id)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50"
                    title="حذف این دوره"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>

              {/* Intervals Info */}
              <div className="grid grid-cols-2 gap-3 mt-4 pt-3 border-t border-slate-100 dark:border-slate-800 text-xs">
                <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/60">
                  <span className="text-slate-400 block mb-1">فاصله پیمایش:</span>
                  <strong className="text-cyan-600 dark:text-cyan-400 text-sm font-mono">
                    هر {formatWithCommas(sched.kmInterval)} ک‌م
                  </strong>
                </div>

                <div className="p-2.5 rounded-xl bg-slate-50 dark:bg-slate-800/60">
                  <span className="text-slate-400 block mb-1">فاصله زمانی:</span>
                  <strong className="text-slate-800 dark:text-slate-200 text-sm font-mono">
                    هر {sched.timeIntervalMonths} ماه
                  </strong>
                </div>
              </div>

              {sched.description && (
                <p className="text-[11px] text-slate-500 mt-3 line-clamp-2">
                  {sched.description}
                </p>
              )}
            </div>

            <div className="mt-4 pt-2 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between text-[11px] text-slate-400">
              <span>وضعیت: {sched.isEnabled ? 'فعال در یادآوری‌ها' : 'غیرفعال'}</span>
            </div>
          </div>
        ))}
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50">
              <div className="flex items-center gap-2 font-bold text-cyan-600 dark:text-cyan-400">
                <Wrench className="w-5 h-5" />
                <span>{editingSchedule ? 'ویرایش دوره سرویس' : 'افزودن دوره سرویس سفارشی'}</span>
              </div>
              <button
                onClick={() => setIsModalOpen(false)}
                className="p-1 text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmit} className="p-6 space-y-4 overflow-y-auto">
              {modalError && (
                <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs font-bold text-rose-600">
                  {modalError}
                </div>
              )}

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  نام قطعه یا سرویس:
                </label>
                <input
                  type="text"
                  required
                  placeholder="مثال: روغن موتور، شمع، تسمه تایم..."
                  value={partName}
                  onChange={(e) => setPartName(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  دسته‌بندی:
                </label>
                <select
                  value={category}
                  onChange={(e) => setCategory(e.target.value as PartCategory)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                >
                  <option value="موتور">موتور</option>
                  <option value="ترمز">ترمز</option>
                  <option value="جلوبندی و تعلیق">جلوبندی و تعلیق</option>
                  <option value="سرویس‌های دوره‌ای">سرویس‌های دوره‌ای</option>
                  <option value="سایر">سایر</option>
                </select>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    فاصله کیلومتر:
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="مثال: 8,000"
                    value={kmIntervalStr}
                    onChange={(e) => {
                      const num = parseInt(normalizeDigits(e.target.value).replace(/,/g, ''), 10);
                      setKmIntervalStr(isNaN(num) ? '' : formatWithCommas(num));
                    }}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left font-bold"
                  />
                  <span className="text-[10px] text-slate-400 mt-0.5 block">کیلومتر</span>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    فاصله زمانی (ماه):
                  </label>
                  <input
                    type="number"
                    min="1"
                    max="60"
                    required
                    placeholder="مثال: 6"
                    value={monthIntervalStr}
                    onChange={(e) => setMonthIntervalStr(normalizeDigits(e.target.value))}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left font-bold"
                  />
                  <span className="text-[10px] text-slate-400 mt-0.5 block">تعداد ماه</span>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  توضیحات و مشخصات توصیه شده کارخانه:
                </label>
                <input
                  type="text"
                  placeholder="مثال: سطح کیفی SN یا SM با ویسکوزیته 10W40"
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                />
              </div>

              <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setIsModalOpen(false)}
                  className="px-4 py-2 text-xs font-bold rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100"
                >
                  انصراف
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white shadow-xs"
                >
                  {editingSchedule ? 'ذخیره تغییرات' : 'افزودن به برنامه'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
