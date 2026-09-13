import React, { useState, useEffect } from 'react';
import { X, Gauge, AlertTriangle, History, Check } from 'lucide-react';
import { Vehicle, MileageRecord } from '../types';
import { normalizeDigits, parseNumericInput, formatWithCommas, formatMileage } from '../utils/formatters';
import { formatToJalali } from '../utils/persianDate';

interface MileageUpdateModalProps {
  isOpen: boolean;
  onClose: () => void;
  vehicle: Vehicle;
  mileageHistory: MileageRecord[];
  onSaveMileage: (newMileage: number, notes?: string, isCorrection?: boolean) => { success: boolean; message?: string };
}

export const MileageUpdateModal: React.FC<MileageUpdateModalProps> = ({
  isOpen,
  onClose,
  vehicle,
  mileageHistory,
  onSaveMileage,
}) => {
  const [mileageInput, setMileageInput] = useState('');
  const [notes, setNotes] = useState('');
  const [errorMessage, setErrorMessage] = useState('');
  const [showWarning, setShowWarning] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setMileageInput(formatWithCommas(vehicle.currentMileage));
      setNotes('');
      setErrorMessage('');
      setShowWarning(false);
    }
  }, [isOpen, vehicle]);

  if (!isOpen) return null;

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = normalizeDigits(e.target.value).replace(/,/g, '');
    const num = parseInt(raw, 10);
    if (isNaN(num)) {
      setMileageInput('');
    } else {
      setMileageInput(formatWithCommas(num));
    }
    setShowWarning(false);
  };

  const handleQuickAdd = (increment: number) => {
    const current = parseNumericInput(mileageInput) || vehicle.currentMileage;
    const nextVal = current + increment;
    setMileageInput(formatWithCommas(nextVal));
    setShowWarning(false);
  };

  const handleSubmit = (e: React.FormEvent, forceConfirm: boolean = false) => {
    e.preventDefault();
    setErrorMessage('');

    const parsed = parseNumericInput(mileageInput);
    if (parsed <= 0) {
      setErrorMessage('لطفا کیلومتر معتبر وارد نمایید.');
      return;
    }

    if (parsed < vehicle.currentMileage && !forceConfirm && !showWarning) {
      setShowWarning(true);
      return;
    }

    const res = onSaveMileage(parsed, notes.trim() || undefined, forceConfirm || showWarning);
    if (res.success) {
      onClose();
    } else {
      setErrorMessage(res.message || 'خطا در ثبت کیلومتر.');
    }
  };

  const sortedHistory = [...mileageHistory]
    .filter((m) => m.vehicleId === vehicle.id)
    .sort((a, b) => b.timestamp - a.timestamp);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/50">
          <div className="flex items-center gap-2 text-cyan-600 dark:text-cyan-400 font-bold text-lg">
            <Gauge className="w-5 h-5" />
            <span>به‌روزرسانی کیلومتر خودرو</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Content */}
        <div className="p-6 overflow-y-auto space-y-5">
          {/* Current Vehicle info */}
          <div className="p-3 bg-cyan-50 dark:bg-cyan-950/40 rounded-xl border border-cyan-200 dark:border-cyan-900 flex items-center justify-between">
            <div>
              <div className="text-xs text-slate-500 dark:text-slate-400">خودرو:</div>
              <div className="text-sm font-bold text-slate-800 dark:text-slate-200">{vehicle.name}</div>
            </div>
            <div className="text-left">
              <div className="text-xs text-slate-500 dark:text-slate-400">کیلومتر فعلی ثبت‌شده:</div>
              <div className="text-sm font-extrabold text-cyan-600 dark:text-cyan-400 font-mono">
                {formatMileage(vehicle.currentMileage)}
              </div>
            </div>
          </div>

          {errorMessage && (
            <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 rounded-xl text-xs font-semibold text-rose-600 dark:text-rose-400">
              {errorMessage}
            </div>
          )}

          {showWarning && (
            <div className="p-4 bg-amber-50 dark:bg-amber-950/40 border border-amber-300 dark:border-amber-700 rounded-xl text-xs space-y-2">
              <div className="flex items-center gap-2 text-amber-700 dark:text-amber-300 font-bold">
                <AlertTriangle className="w-4 h-4" />
                <span>هشدار: کیلومتر کمتر از مقدار قبلی است</span>
              </div>
              <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
                کیلومتر جدید ({mileageInput} ک‌م) از کیلومتر فعلی ({formatWithCommas(vehicle.currentMileage)} ک‌م) کمتر است. آیا مطمئن هستید که این یک اصلاح کیلومتر اشتباه است؟
              </p>
              <div className="flex justify-end gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setShowWarning(false)}
                  className="px-3 py-1.5 rounded-lg bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-200 font-bold"
                >
                  انصراف
                </button>
                <button
                  type="button"
                  onClick={(e) => handleSubmit(e, true)}
                  className="px-3 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-700 text-white font-bold"
                >
                  تأیید و ثبت اصلاح
                </button>
              </div>
            </div>
          )}

          {/* Form */}
          <form onSubmit={(e) => handleSubmit(e, false)} className="space-y-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                کیلومتر جدید خودرو (با جداکننده هزارگان):
              </label>
              <div className="relative">
                <input
                  type="text"
                  required
                  placeholder="مثال: 55,000"
                  value={mileageInput}
                  onChange={handleInputChange}
                  className="w-full px-4 py-3 text-base rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left font-extrabold text-slate-900 dark:text-slate-100"
                />
                <span className="absolute left-3 top-3.5 text-xs text-slate-400 font-normal">
                  کیلومتر
                </span>
              </div>
            </div>

            {/* Quick Add buttons */}
            <div className="flex items-center gap-2">
              <span className="text-[11px] text-slate-400 font-medium">افزودن سریع:</span>
              <button
                type="button"
                onClick={() => handleQuickAdd(100)}
                className="px-2.5 py-1 text-xs rounded-lg bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 text-slate-700 dark:text-slate-300 font-semibold"
              >
                +۱۰۰ ک‌م
              </button>
              <button
                type="button"
                onClick={() => handleQuickAdd(500)}
                className="px-2.5 py-1 text-xs rounded-lg bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 text-slate-700 dark:text-slate-300 font-semibold"
              >
                +۵۰۰ ک‌م
              </button>
              <button
                type="button"
                onClick={() => handleQuickAdd(1000)}
                className="px-2.5 py-1 text-xs rounded-lg bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 text-slate-700 dark:text-slate-300 font-semibold"
              >
                +۱,۰۰۰ ک‌م
              </button>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                توضیحات یا یادداشت (اختیاری):
              </label>
              <input
                type="text"
                placeholder="مثال: بعد از سفر اصفهان"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                className="w-full px-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>

            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={onClose}
                className="px-4 py-2 text-xs sm:text-sm font-bold rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                انصراف
              </button>
              <button
                type="submit"
                className="px-5 py-2 text-xs sm:text-sm font-bold rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white shadow-sm flex items-center gap-1.5"
              >
                <Check className="w-4 h-4" />
                <span>ثبت در تاریخچه و ذخیره</span>
              </button>
            </div>
          </form>

          {/* History Section */}
          <div className="border-t border-slate-200 dark:border-slate-800 pt-4">
            <h4 className="text-xs font-bold text-slate-600 dark:text-slate-400 mb-2 flex items-center gap-1.5">
              <History className="w-3.5 h-3.5 text-cyan-600" />
              <span>تاریخچه ثبت کیلومتر این خودرو ({sortedHistory.length} مورد):</span>
            </h4>
            <div className="max-h-40 overflow-y-auto space-y-1.5 pr-1">
              {sortedHistory.length === 0 ? (
                <div className="text-xs text-slate-400 text-center py-2">تاریخچه‌ای ثبت نشده است.</div>
              ) : (
                sortedHistory.map((rec) => (
                  <div
                    key={rec.id}
                    className="flex items-center justify-between text-xs p-2 rounded-lg bg-slate-50 dark:bg-slate-800/60 border border-slate-100 dark:border-slate-800"
                  >
                    <div className="flex items-center gap-2">
                      <span className="font-mono text-slate-500 dark:text-slate-400">
                        {formatToJalali(rec.timestamp)}
                      </span>
                      {rec.notes && (
                        <span className="text-slate-600 dark:text-slate-300 font-medium truncate max-w-[150px]">
                          • {rec.notes}
                        </span>
                      )}
                    </div>
                    <span className="font-bold font-mono text-slate-900 dark:text-slate-100">
                      {formatMileage(rec.mileage)}
                    </span>
                  </div>
                ))
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
