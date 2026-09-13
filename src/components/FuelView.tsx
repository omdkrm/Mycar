import React, { useState } from 'react';
import { Fuel, Plus, Calendar, Gauge, DollarSign, Trash2, Edit3, Check, X, AlertCircle } from 'lucide-react';
import { Vehicle, FuelRecord, FuelType } from '../types';
import { formatCurrency, formatMileage, formatConsumption, formatWithCommas, parseNumericInput, normalizeDigits } from '../utils/formatters';
import { formatToJalali, parseJalaliStringToDate } from '../utils/persianDate';
import { calculateFuelConsumption } from '../utils/calculations';

interface FuelViewProps {
  vehicle: Vehicle;
  fuelRecords: FuelRecord[];
  onSaveFuelRecord: (record: Partial<FuelRecord>) => { success: boolean; message?: string };
  onDeleteFuelRecord: (id: string) => void;
}

export const FuelView: React.FC<FuelViewProps> = ({
  vehicle,
  fuelRecords,
  onSaveFuelRecord,
  onDeleteFuelRecord,
}) => {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingRecord, setEditingRecord] = useState<FuelRecord | null>(null);

  // Form states
  const [jalaliDate, setJalaliDate] = useState(formatToJalali(Date.now()));
  const [mileageStr, setMileageStr] = useState(formatWithCommas(vehicle.currentMileage));
  const [fuelVolumeStr, setFuelVolumeStr] = useState('30');
  const [pricePerLiterStr, setPricePerLiterStr] = useState('3,000');
  const [fuelType, setFuelType] = useState<FuelType>('معمولی');
  const [isFullTank, setIsFullTank] = useState(true);
  const [gasStation, setGasStation] = useState('');
  const [notes, setNotes] = useState('');
  const [modalError, setModalError] = useState('');

  const stats = calculateFuelConsumption(fuelRecords, vehicle.id);

  const vehicleFuelList = fuelRecords
    .filter((r) => r.vehicleId === vehicle.id)
    .sort((a, b) => b.mileage - a.mileage || b.dateTimestamp - a.dateTimestamp);

  const handleOpenModal = (rec?: FuelRecord) => {
    if (rec) {
      setEditingRecord(rec);
      setJalaliDate(formatToJalali(rec.dateTimestamp));
      setMileageStr(formatWithCommas(rec.mileage));
      setFuelVolumeStr(String(rec.liters));
      setPricePerLiterStr(formatWithCommas(rec.pricePerLiter));
      setFuelType(rec.fuelType);
      setIsFullTank(rec.isFullTank);
      setGasStation(rec.gasStation || '');
      setNotes(rec.notes || '');
    } else {
      setEditingRecord(null);
      setJalaliDate(formatToJalali(Date.now()));
      setMileageStr(formatWithCommas(vehicle.currentMileage));
      setFuelVolumeStr('30');
      setPricePerLiterStr('3,000');
      setFuelType('معمولی');
      setIsFullTank(true);
      setGasStation('');
      setNotes('');
    }
    setModalError('');
    setIsModalOpen(true);
  };

  const volume = parseFloat(normalizeDigits(fuelVolumeStr)) || 0;
  const unitPrice = parseNumericInput(pricePerLiterStr);
  const totalFuelCost = Math.round(volume * unitPrice);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setModalError('');

    const parsedMileage = parseNumericInput(mileageStr);
    if (parsedMileage <= 0) {
      setModalError('لطفا کیلومتر معتبر وارد کنید.');
      return;
    }
    if (volume <= 0) {
      setModalError('لطفا حجم بنزین (لیتر) را مشخص کنید.');
      return;
    }

    const dateObj = parseJalaliStringToDate(jalaliDate);
    if (!dateObj) {
      setModalError('تاریخ شمسی نامعتبر است.');
      return;
    }

    const payload: Partial<FuelRecord> = {
      id: editingRecord ? editingRecord.id : undefined,
      vehicleId: vehicle.id,
      dateTimestamp: dateObj.getTime(),
      mileage: parsedMileage,
      liters: volume,
      pricePerLiter: unitPrice,
      totalCost: totalFuelCost,
      fuelType,
      isFullTank,
      gasStation: gasStation.trim() || undefined,
      notes: notes.trim() || undefined,
    };

    const res = onSaveFuelRecord(payload);
    if (res.success) {
      setIsModalOpen(false);
    } else {
      setModalError(res.message || 'خطا در ثبت سوخت‌گیری.');
    }
  };

  return (
    <div className="space-y-6">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Fuel className="w-7 h-7 text-emerald-600 dark:text-emerald-400" />
            <span>سوخت‌گیری و مصرف سوخت - {vehicle.name}</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            ثبت اطلاعات پمپ بنزین، محاسبه مصرف صد کیلومتر و هزینه سوخت به ازای پیمایش
          </p>
        </div>

        <button
          onClick={() => handleOpenModal()}
          className="self-start sm:self-auto bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-xs transition flex items-center gap-2"
        >
          <Plus className="w-4 h-4" />
          <span>+ ثبت سوخت‌گیری جدید</span>
        </button>
      </div>

      {/* Metrics Row */}
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            میانگین مصرف (لیتر / ۱۰۰ ک‌م):
          </div>
          <div className="text-2xl font-black text-slate-900 dark:text-slate-100 font-mono">
            {formatConsumption(stats.averageLitersPer100Km)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            بر اساس باک‌های پر متوالی
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            کل هزینه بنزین پرداختی:
          </div>
          <div className="text-2xl font-black text-emerald-600 dark:text-emerald-400">
            {formatCurrency(stats.totalFuelCost)}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            تعداد دفعات: {stats.recordsCount} بار
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            کل حجم بنزین مصرف‌شده:
          </div>
          <div className="text-2xl font-black text-slate-900 dark:text-slate-100 font-mono">
            {stats.totalFuelLiters} لیتر
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            حجم ثبت شده در سوابق
          </div>
        </div>

        <div className="bg-white dark:bg-slate-900 p-5 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs">
          <div className="text-xs font-semibold text-slate-500 dark:text-slate-400 mb-1">
            هزینه سوخت به ازای هر ک‌م:
          </div>
          <div className="text-2xl font-black text-cyan-600 dark:text-cyan-400 font-mono">
            {stats.costPerKm ? `${stats.costPerKm} تومان` : '—'}
          </div>
          <div className="text-[11px] text-slate-400 mt-1">
            هزینه پیمایش هر کیلومتر
          </div>
        </div>
      </div>

      {/* Fuel Log List */}
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-6 shadow-xs">
        <h3 className="font-extrabold text-slate-900 dark:text-slate-100 text-base mb-4">
          سوابق سوخت‌گیری ({vehicleFuelList.length} مورد)
        </h3>

        {vehicleFuelList.length === 0 ? (
          <div className="text-center py-10 text-slate-400 text-xs">
            هنوز سابقه سوخت‌گیری برای این خودرو ثبت نشده است. با ثبت اولین باک بنزین، مصرف ۱۰۰ کیلومتر محاسبه خواهد شد.
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-xs text-right">
              <thead>
                <tr className="border-b border-slate-100 dark:border-slate-800 text-slate-400">
                  <th className="pb-3 font-semibold">تاریخ</th>
                  <th className="pb-3 font-semibold">کیلومتر</th>
                  <th className="pb-3 font-semibold">حجم (لیتر)</th>
                  <th className="pb-3 font-semibold">نوع بنزین</th>
                  <th className="pb-3 font-semibold">وضعیت باک</th>
                  <th className="pb-3 font-semibold">مبلغ کل</th>
                  <th className="pb-3 font-semibold">جایگاه / یادداشت</th>
                  <th className="pb-3 font-semibold text-left">عملیات</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 dark:divide-slate-800/60">
                {vehicleFuelList.map((rec) => (
                  <tr key={rec.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/40">
                    <td className="py-3.5 font-mono text-slate-600 dark:text-slate-300">
                      {formatToJalali(rec.dateTimestamp)}
                    </td>
                    <td className="py-3.5 font-mono font-bold text-slate-800 dark:text-slate-200">
                      {formatMileage(rec.mileage)}
                    </td>
                    <td className="py-3.5 font-mono font-extrabold text-emerald-600 dark:text-emerald-400">
                      {rec.liters} ل
                    </td>
                    <td className="py-3.5">
                      <span
                        className={`px-2 py-0.5 rounded-md font-bold text-[10px] ${
                          rec.fuelType === 'سوپر'
                            ? 'bg-purple-100 text-purple-700 dark:bg-purple-950 dark:text-purple-300'
                            : 'bg-emerald-50 text-emerald-700 dark:bg-emerald-950 dark:text-emerald-300'
                        }`}
                      >
                        {rec.fuelType}
                      </span>
                    </td>
                    <td className="py-3.5">
                      {rec.isFullTank ? (
                        <span className="text-emerald-600 dark:text-emerald-400 font-bold">باک پر</span>
                      ) : (
                        <span className="text-slate-400">ناقص</span>
                      )}
                    </td>
                    <td className="py-3.5 font-bold text-slate-900 dark:text-slate-100">
                      {formatCurrency(rec.totalCost)}
                    </td>
                    <td className="py-3.5 text-slate-500 max-w-xs truncate">
                      {rec.gasStation || rec.notes || '—'}
                    </td>
                    <td className="py-3.5 text-left">
                      <div className="flex items-center justify-end gap-1">
                        <button
                          onClick={() => handleOpenModal(rec)}
                          className="p-1 rounded text-slate-400 hover:text-slate-700"
                        >
                          <Edit3 className="w-4 h-4" />
                        </button>
                        <button
                          onClick={() => onDeleteFuelRecord(rec.id)}
                          className="p-1 rounded text-slate-400 hover:text-rose-600"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Fuel Register Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50">
              <div className="flex items-center gap-2 font-bold text-emerald-600 dark:text-emerald-400">
                <Fuel className="w-5 h-5" />
                <span>{editingRecord ? 'ویرایش سوخت‌گیری' : 'ثبت سوخت‌گیری جدید'}</span>
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

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    کیلومتر خودرو:
                  </label>
                  <input
                    type="text"
                    required
                    value={mileageStr}
                    onChange={(e) => {
                      const num = parseInt(normalizeDigits(e.target.value).replace(/,/g, ''), 10);
                      setMileageStr(isNaN(num) ? '' : formatWithCommas(num));
                    }}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left font-bold"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    تاریخ (شمسی):
                  </label>
                  <input
                    type="text"
                    required
                    value={jalaliDate}
                    onChange={(e) => setJalaliDate(normalizeDigits(e.target.value))}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left"
                  />
                </div>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    حجم بنزین (لیتر):
                  </label>
                  <input
                    type="text"
                    required
                    value={fuelVolumeStr}
                    onChange={(e) => setFuelVolumeStr(normalizeDigits(e.target.value))}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left font-bold"
                  />
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    قیمت هر لیتر (تومان):
                  </label>
                  <input
                    type="text"
                    value={pricePerLiterStr}
                    onChange={(e) => {
                      const num = parseInt(normalizeDigits(e.target.value).replace(/,/g, ''), 10);
                      setPricePerLiterStr(isNaN(num) ? '' : formatWithCommas(num));
                    }}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-mono text-left"
                  />
                </div>
              </div>

              {/* Calculated Total Cost */}
              <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 rounded-xl border border-emerald-200 dark:border-emerald-900 flex items-center justify-between text-xs">
                <span className="font-bold text-emerald-800 dark:text-emerald-300">
                  مبلغ کل سوخت‌گیری (محاسبه خودکار):
                </span>
                <span className="text-base font-black font-mono text-emerald-900 dark:text-emerald-200">
                  {formatCurrency(totalFuelCost)}
                </span>
              </div>

              <div className="grid grid-cols-2 gap-3">
                <div>
                  <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                    نوع سوخت:
                  </label>
                  <select
                    value={fuelType}
                    onChange={(e) => setFuelType(e.target.value as FuelType)}
                    className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                  >
                    <option value="معمولی">معمولی (۱,۵۰۰ / ۳,۰۰۰ تومان)</option>
                    <option value="سوپر">سوپر</option>
                  </select>
                </div>

                <div className="flex items-center pt-5">
                  <label className="flex items-center gap-2 cursor-pointer text-xs font-bold text-slate-700 dark:text-slate-300">
                    <input
                      type="checkbox"
                      checked={isFullTank}
                      onChange={(e) => setIsFullTank(e.target.checked)}
                      className="w-4 h-4 rounded text-emerald-600 focus:ring-emerald-500"
                    />
                    <span>باک خودرو کاملاً پر شد (برای دقت ۱۰۰ ک‌م)</span>
                  </label>
                </div>
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  جایگاه سوخت / موقعیت (اختیاری):
                </label>
                <input
                  type="text"
                  placeholder="مثال: جایگاه ۲۵ ولیعصر"
                  value={gasStation}
                  onChange={(e) => setGasStation(e.target.value)}
                  className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800"
                />
              </div>

              <div>
                <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                  توضیحات (اختیاری):
                </label>
                <input
                  type="text"
                  placeholder="مثال: استفاده از مکمل سوخت"
                  value={notes}
                  onChange={(e) => setNotes(e.target.value)}
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
                  className="px-5 py-2 text-xs font-bold rounded-xl bg-emerald-600 hover:bg-emerald-700 text-white shadow-xs"
                >
                  {editingRecord ? 'ذخیره تغییرات' : 'ثبت سوخت‌گیری'}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
