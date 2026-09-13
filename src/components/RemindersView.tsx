import React, { useState } from 'react';
import {
  CalendarClock,
  AlertCircle,
  Clock,
  CheckCircle2,
  Wrench,
  Check,
  X,
  Calculator,
  Upload,
} from 'lucide-react';
import { Vehicle, ReminderItem, ServiceRecord, OperationType } from '../types';
import { formatCurrency, formatMileage, formatWithCommas } from '../utils/formatters';
import { formatToJalali, parseJalaliStringToDate } from '../utils/persianDate';

interface RemindersViewProps {
  vehicle: Vehicle;
  reminders: ReminderItem[];
  onCompleteService: (record: Partial<ServiceRecord>) => { success: boolean; message?: string };
  onNavigateToScheduleSettings?: () => void;
}

export const RemindersView: React.FC<RemindersViewProps> = ({
  vehicle,
  reminders,
  onCompleteService,
}) => {
  const [filter, setFilter] = useState<'all' | 'overdue' | 'approaching' | 'healthy'>('all');

  // Completion Modal State
  const [completingReminder, setCompletingReminder] = useState<ReminderItem | null>(null);
  const [partPriceStr, setPartPriceStr] = useState('');
  const [laborCostStr, setLaborCostStr] = useState('');
  const [repairShop, setRepairShop] = useState('');
  const [description, setDescription] = useState('');
  const [invoicePhotoUrl, setInvoicePhotoUrl] = useState<string | undefined>(undefined);
  const [mileageStr, setMileageStr] = useState('');
  const [jalaliDateStr, setJalaliDateStr] = useState('');
  const [modalError, setModalError] = useState('');

  const overdueCount = reminders.filter((r) => r.status === 'overdue').length;
  const approachingCount = reminders.filter((r) => r.status === 'approaching').length;
  const healthyCount = reminders.filter((r) => r.status === 'healthy').length;

  const filteredReminders = reminders.filter((r) => {
    if (filter === 'all') return true;
    return r.status === filter;
  });

  const handleOpenCompleteModal = (reminder: ReminderItem) => {
    setCompletingReminder(reminder);
    setMileageStr(formatWithCommas(vehicle.currentMileage));
    setJalaliDateStr(formatToJalali(Date.now()));
    setPartPriceStr('');
    setLaborCostStr('');
    setRepairShop('');
    setDescription('');
    setInvoicePhotoUrl(undefined);
    setModalError('');
  };

  const handlePhotoUpload = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (file) {
      const reader = new FileReader();
      reader.onloadend = () => {
        setInvoicePhotoUrl(reader.result as string);
      };
      reader.readAsDataURL(file);
    }
  };

  const handleSubmitCompletion = (e: React.FormEvent) => {
    e.preventDefault();
    if (!completingReminder) return;

    const parsedMileage = parseInt(mileageStr.replace(/,/g, ''), 10);
    if (isNaN(parsedMileage) || parsedMileage <= 0) {
      setModalError('لطفا کیلومتر معتبر وارد کنید.');
      return;
    }

    const dateObj = parseJalaliStringToDate(jalaliDateStr);
    if (!dateObj) {
      setModalError('تاریخ شمسی نامعتبر است.');
      return;
    }

    const partPrice = parseInt(partPriceStr.replace(/,/g, ''), 10) || 0;
    const laborCost = parseInt(laborCostStr.replace(/,/g, ''), 10) || 0;
    const totalCost = partPrice + laborCost;

    const res = onCompleteService({
      vehicleId: vehicle.id,
      catalogItemId: completingReminder.catalogItemId,
      partName: completingReminder.partName,
      category: completingReminder.category,
      operationType: 'تعویض' as OperationType,
      dateTimestamp: dateObj.getTime(),
      mileage: parsedMileage,
      partPrice,
      laborCost,
      totalCost,
      repairShop: repairShop.trim() || undefined,
      description: description.trim() || undefined,
      invoicePhotoUrl,
    });

    if (res.success) {
      setCompletingReminder(null);
    } else {
      setModalError(res.message || 'خطا در ثبت انجام سرویس.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
          <CalendarClock className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
          <span>یادآوری‌های من - {vehicle.name}</span>
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
          بررسی موعد کیلومتری و زمانی قطعات و ثبت فوری سرویس انجام‌شده
        </p>
      </div>

      {/* Filter Tabs */}
      <div className="flex flex-wrap items-center gap-2">
        <button
          onClick={() => setFilter('all')}
          className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition ${
            filter === 'all'
              ? 'bg-cyan-600 text-white shadow-xs'
              : 'bg-white dark:bg-slate-900 text-slate-600 dark:text-slate-300 border border-slate-200 dark:border-slate-800'
          }`}
        >
          همه یادآوری‌ها ({reminders.length})
        </button>

        <button
          onClick={() => setFilter('overdue')}
          className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition flex items-center gap-1.5 ${
            filter === 'overdue'
              ? 'bg-rose-600 text-white shadow-xs'
              : 'bg-white dark:bg-slate-900 text-rose-600 border border-slate-200 dark:border-slate-800'
          }`}
        >
          <AlertCircle className="w-4 h-4" />
          <span>عقب‌افتاده ({overdueCount})</span>
        </button>

        <button
          onClick={() => setFilter('approaching')}
          className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition flex items-center gap-1.5 ${
            filter === 'approaching'
              ? 'bg-amber-600 text-white shadow-xs'
              : 'bg-white dark:bg-slate-900 text-amber-600 border border-slate-200 dark:border-slate-800'
          }`}
        >
          <Clock className="w-4 h-4" />
          <span>نزدیک به موعد ({approachingCount})</span>
        </button>

        <button
          onClick={() => setFilter('healthy')}
          className={`px-4 py-2 rounded-xl text-xs sm:text-sm font-bold transition flex items-center gap-1.5 ${
            filter === 'healthy'
              ? 'bg-emerald-600 text-white shadow-xs'
              : 'bg-white dark:bg-slate-900 text-emerald-600 border border-slate-200 dark:border-slate-800'
          }`}
        >
          <CheckCircle2 className="w-4 h-4" />
          <span>عادی و دارای فرصت ({healthyCount})</span>
        </button>
      </div>

      {/* Reminders Cards List */}
      {filteredReminders.length === 0 ? (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center space-y-3">
          <CalendarClock className="w-12 h-12 text-slate-300 mx-auto" />
          <div className="text-base font-bold text-slate-700 dark:text-slate-300">
            در حال حاضر یادآوری فعالی ندارید.
          </div>
          <p className="text-xs text-slate-400">
            تمامی سرویس‌های زمان‌بندی‌شده در وضعیت نرمال قرار دارند یا هنوز برنامه‌ای تعریف نشده است.
          </p>
        </div>
      ) : (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredReminders.map((rem) => {
            const isOverdue = rem.status === 'overdue';
            const isApproaching = rem.status === 'approaching';

            return (
              <div
                key={rem.id}
                className={`p-5 rounded-2xl border transition shadow-xs flex flex-col justify-between ${
                  isOverdue
                    ? 'bg-rose-50/70 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900'
                    : isApproaching
                    ? 'bg-amber-50/70 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900'
                    : 'bg-white dark:bg-slate-900 border-slate-200 dark:border-slate-800'
                }`}
              >
                <div>
                  <div className="flex items-start justify-between gap-3">
                    <div className="space-y-1">
                      <div className="flex items-center gap-2">
                        <span
                          className={`w-3 h-3 rounded-full ${
                            isOverdue
                              ? 'bg-rose-500 animate-pulse'
                              : isApproaching
                              ? 'bg-amber-500'
                              : 'bg-emerald-500'
                          }`}
                        />
                        <h3 className="font-extrabold text-base text-slate-900 dark:text-slate-100">
                          {rem.partName}
                        </h3>
                        <span className="text-[10px] font-semibold text-slate-500 bg-white dark:bg-slate-800 px-2 py-0.5 rounded-md">
                          {rem.category}
                        </span>
                      </div>

                      <div
                        className={`text-xs font-bold ${
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

                    <span
                      className={`text-xs font-black px-2.5 py-1 rounded-lg ${
                        isOverdue
                          ? 'bg-rose-600 text-white'
                          : isApproaching
                          ? 'bg-amber-500 text-white'
                          : 'bg-emerald-100 dark:bg-emerald-950 text-emerald-700 dark:text-emerald-300'
                      }`}
                    >
                      {isOverdue ? 'عقب افتاده' : isApproaching ? 'نزدیک موعد' : 'سالم'}
                    </span>
                  </div>

                  {/* Info table */}
                  <div className="grid grid-cols-2 gap-2 mt-4 pt-3 border-t border-slate-200/60 dark:border-slate-800 text-xs">
                    <div>
                      <span className="text-slate-400 block">کیلومتر موعد:</span>
                      <strong className="text-slate-800 dark:text-slate-200 font-mono">
                        {rem.dueMileage ? formatMileage(rem.dueMileage) : 'نامشخص'}
                      </strong>
                    </div>

                    <div>
                      <span className="text-slate-400 block">تاریخ موعد:</span>
                      <strong className="text-slate-800 dark:text-slate-200 font-mono">
                        {rem.dueDateTimestamp ? formatToJalali(rem.dueDateTimestamp) : 'نامشخص'}
                      </strong>
                    </div>

                    <div>
                      <span className="text-slate-400 block">آخرین تعویض:</span>
                      <strong className="text-slate-700 dark:text-slate-300 font-mono">
                        {rem.lastServiceMileage ? `${formatWithCommas(rem.lastServiceMileage)} ک‌م` : 'اولین دوره'}
                      </strong>
                    </div>

                    <div>
                      <span className="text-slate-400 block">کیلومتر فعلی خودرو:</span>
                      <strong className="text-cyan-600 dark:text-cyan-400 font-mono">
                        {formatMileage(vehicle.currentMileage)}
                      </strong>
                    </div>
                  </div>
                </div>

                {/* Card Action Button: انجام شد */}
                <div className="mt-5 pt-3 border-t border-slate-200/60 dark:border-slate-800 flex items-center justify-between">
                  <div className="text-[11px] text-slate-400 font-medium">
                    {rem.kmRemaining !== undefined && rem.kmRemaining > 0
                      ? `${formatWithCommas(rem.kmRemaining)} ک‌م باقی‌مانده`
                      : rem.kmRemaining !== undefined
                      ? `${formatWithCommas(Math.abs(rem.kmRemaining))} ک‌م تاخیر`
                      : ''}
                  </div>

                  <button
                    onClick={() => handleOpenCompleteModal(rem)}
                    className="flex items-center gap-1.5 bg-cyan-600 hover:bg-cyan-700 text-white font-bold text-xs px-4 py-2 rounded-xl shadow-xs transition active:scale-98"
                  >
                    <Check className="w-4 h-4" />
                    <span>انجام شد (ثبت سرویس)</span>
                  </button>
                </div>
              </div>
            );
          })}
        </div>
      )}

      {/* Completion Modal ("انجام شد") */}
      {completingReminder && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50">
              <div className="flex items-center gap-2 font-bold text-cyan-600 dark:text-cyan-400">
                <Check className="w-5 h-5" />
                <span>ثبت انجام سرویس «{completingReminder.partName}»</span>
              </div>
              <button
                onClick={() => setCompletingReminder(null)}
                className="p-1 text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <form onSubmit={handleSubmitCompletion} className="p-6 space-y-4 overflow-y-auto">
              {modalError && (
                <div className="p-3 bg-rose-50 border border-rose-200 rounded-xl text-xs font-bold text-rose-600">
                  {modalError}
                </div>
              )}

              {/* Prefilled Summary */}
              <div className="p-3 bg-cyan-50 dark:bg-cyan-950/40 rounded-xl text-xs flex justify-between">
                <div>
                  <span className="text-slate-500 block">خودرو:</span>
                  <strong className="text-slate-800 dark:text-slate-200">{vehicle.name}</strong>
                </div>
                <div>
                  <span className="text-slate-500 block">قطعه:</span>
                  <strong className="text-cyan-700 dark:text-cyan-300 font-bold">{completingReminder.partName}</strong>
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    کیلومتر انجام کار:
                  </label>
                  <input
                    type="text"
                    required
                    value={mileageStr}
                    onChange={(e) => setMileageStr(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left font-bold"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    تاریخ انجام (شمسی):
                  </label>
                  <input
                    type="text"
                    required
                    value={jalaliDateStr}
                    onChange={(e) => setJalaliDateStr(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    قیمت قطعه (تومان):
                  </label>
                  <input
                    type="text"
                    placeholder="۰"
                    value={partPriceStr}
                    onChange={(e) => setPartPriceStr(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left"
                  />
                </div>
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    اجرت و دستمزد (تومان):
                  </label>
                  <input
                    type="text"
                    placeholder="۰"
                    value={laborCostStr}
                    onChange={(e) => setLaborCostStr(e.target.value)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left"
                  />
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  تعمیرگاه یا سرویس‌کار (اختیاری):
                </label>
                <input
                  type="text"
                  placeholder="مثال: اتوسرویس پارس..."
                  value={repairShop}
                  onChange={(e) => setRepairShop(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  توضیحات و برند قطعه:
                </label>
                <input
                  type="text"
                  placeholder="مثال: تعویض فیلتر روغن اصلی ایساکو..."
                  value={description}
                  onChange={(e) => setDescription(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  تصویر فاکتور / قطعه (اختیاری):
                </label>
                <input
                  type="file"
                  accept="image/*"
                  onChange={handlePhotoUpload}
                  className="text-xs text-slate-500"
                />
              </div>

              <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex justify-end gap-2">
                <button
                  type="button"
                  onClick={() => setCompletingReminder(null)}
                  className="px-4 py-2 text-xs font-bold rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100"
                >
                  انصراف
                </button>
                <button
                  type="submit"
                  className="px-5 py-2 text-xs font-bold rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white"
                >
                  ثبت سرویس و محاسبه مجدد موعد
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
