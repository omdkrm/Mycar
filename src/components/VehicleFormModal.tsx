import React, { useState, useEffect } from 'react';
import { X, Car, AlertTriangle } from 'lucide-react';
import { Vehicle } from '../types';
import { normalizeDigits, parseNumericInput, formatWithCommas, formatYear } from '../utils/formatters';

interface VehicleFormModalProps {
  isOpen: boolean;
  onClose: () => void;
  onSave: (vehicleData: Partial<Vehicle>, isCorrectionConfirmed?: boolean) => { success: boolean; message?: string };
  initialVehicle?: Vehicle | null;
  latestKnownMileage?: number;
}

export const VehicleFormModal: React.FC<VehicleFormModalProps> = ({
  isOpen,
  onClose,
  onSave,
  initialVehicle,
  latestKnownMileage = 0,
}) => {
  const [name, setName] = useState('');
  const [brand, setBrand] = useState('');
  const [model, setModel] = useState('');
  const [year, setYear] = useState('');
  const [trim, setTrim] = useState('');
  const [engineDisplacement, setEngineDisplacement] = useState('');
  const [licensePlate, setLicensePlate] = useState('');
  const [vin, setVin] = useState('');
  const [currentMileageStr, setCurrentMileageStr] = useState('');

  const [errorMessage, setErrorMessage] = useState('');
  const [showCorrectionWarning, setShowCorrectionWarning] = useState(false);

  useEffect(() => {
    if (initialVehicle) {
      setName(initialVehicle.name || '');
      setBrand(initialVehicle.brand || '');
      setModel(initialVehicle.model || '');
      setYear(String(initialVehicle.year || ''));
      setTrim(initialVehicle.trim || '');
      setEngineDisplacement(initialVehicle.engineDisplacement || '');
      setLicensePlate(initialVehicle.licensePlate || '');
      setVin(initialVehicle.vin || '');
      setCurrentMileageStr(formatWithCommas(initialVehicle.currentMileage));
    } else {
      setName('');
      setBrand('');
      setModel('');
      setYear('1402');
      setTrim('');
      setEngineDisplacement('');
      setLicensePlate('');
      setVin('');
      setCurrentMileageStr('0');
    }
    setErrorMessage('');
    setShowCorrectionWarning(false);
  }, [initialVehicle, isOpen]);

  if (!isOpen) return null;

  const handleMileageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const raw = normalizeDigits(e.target.value).replace(/,/g, '');
    const num = parseInt(raw, 10);
    if (isNaN(num)) {
      setCurrentMileageStr('');
    } else {
      setCurrentMileageStr(formatWithCommas(num));
    }
    setShowCorrectionWarning(false);
  };

  const handleSubmit = (e: React.FormEvent, forceConfirm: boolean = false) => {
    e.preventDefault();
    setErrorMessage('');

    if (!name.trim()) {
      setErrorMessage('لطفا نام یا عنوان خودرو را وارد کنید.');
      return;
    }
    if (!brand.trim()) {
      setErrorMessage('لطفا برند خودرو را مشخص کنید.');
      return;
    }
    if (!model.trim()) {
      setErrorMessage('لطفا مدل خودرو را مشخص کنید.');
      return;
    }

    const parsedYear = parseInt(normalizeDigits(year).trim(), 10);
    if (isNaN(parsedYear) || parsedYear < 1300 || (parsedYear > 1500 && parsedYear < 1950) || parsedYear > 2050) {
      setErrorMessage('لطفا سال ساخت معتبر وارد کنید (بدون جداکننده، مثلاً ۱۴۰۲ یا ۲۰۲۳).');
      return;
    }

    const parsedMileage = parseNumericInput(currentMileageStr);
    if (parsedMileage < 0) {
      setErrorMessage('کیلومتر خودرو نمی‌تواند منفی باشد.');
      return;
    }

    // Validation: Check if mileage is reduced below latest valid mileage
    if (initialVehicle && parsedMileage < latestKnownMileage && !forceConfirm && !showCorrectionWarning) {
      setShowCorrectionWarning(true);
      return;
    }

    const result = onSave(
      {
        id: initialVehicle ? initialVehicle.id : undefined,
        name: name.trim(),
        brand: brand.trim(),
        model: model.trim(),
        year: parsedYear,
        trim: trim.trim() || undefined,
        engineDisplacement: engineDisplacement.trim() || undefined,
        licensePlate: licensePlate.trim() || undefined,
        vin: vin.trim() || undefined,
        currentMileage: parsedMileage,
      },
      forceConfirm || showCorrectionWarning
    );

    if (result.success) {
      onClose();
    } else {
      setErrorMessage(result.message || 'خطا در ذخیره اطلاعات خودرو.');
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
      <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
        {/* Modal Header */}
        <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-50/50 dark:bg-slate-800/50">
          <div className="flex items-center gap-2 text-cyan-600 dark:text-cyan-400 font-bold text-lg">
            <Car className="w-5 h-5" />
            <span>{initialVehicle ? 'ویرایش اطلاعات خودرو' : 'افزودن خودرو جدید'}</span>
          </div>
          <button
            onClick={onClose}
            className="p-1 rounded-lg text-slate-400 hover:text-slate-600 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Modal Body */}
        <form onSubmit={(e) => handleSubmit(e, false)} className="p-6 overflow-y-auto space-y-4">
          {errorMessage && (
            <div className="p-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 rounded-xl text-xs font-semibold text-rose-600 dark:text-rose-400">
              {errorMessage}
            </div>
          )}

          {showCorrectionWarning && (
            <div className="p-4 bg-amber-50 dark:bg-amber-950/40 border border-amber-300 dark:border-amber-700 rounded-xl text-xs space-y-2">
              <div className="flex items-center gap-2 text-amber-700 dark:text-amber-300 font-bold">
                <AlertTriangle className="w-4 h-4" />
                <span>هشدار کاهش کیلومتر خودرو</span>
              </div>
              <p className="text-slate-600 dark:text-slate-300 leading-relaxed">
                کیلومتر وارد شده ({currentMileageStr} ک‌م) از آخرین کیلومتر ثبت شده ({formatWithCommas(latestKnownMileage)} ک‌م) کمتر است. آیا این یک اصلاح و تصحیح دستی است؟
              </p>
              <div className="flex justify-end gap-2 pt-1">
                <button
                  type="button"
                  onClick={() => setShowCorrectionWarning(false)}
                  className="px-3 py-1.5 rounded-lg bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-200 text-xs font-bold"
                >
                  انصراف
                </button>
                <button
                  type="button"
                  onClick={(e) => handleSubmit(e, true)}
                  className="px-3 py-1.5 rounded-lg bg-amber-600 hover:bg-amber-700 text-white text-xs font-bold"
                >
                  تأیید و اصلاح کیلومتر
                </button>
              </div>
            </div>
          )}

          {/* Form Fields */}
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              نام یا عنوان خودرو <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="مثال: دنا پلاس سفید من"
              value={name}
              onChange={(e) => setName(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                برند <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="مثال: ایران خودرو"
                value={brand}
                onChange={(e) => setBrand(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                مدل <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="مثال: دنا پلاس"
                value={model}
                onChange={(e) => setModel(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                سال ساخت (بدون کاما) <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="مثال: 1402 یا 2023"
                value={year}
                onChange={(e) => setYear(normalizeDigits(e.target.value).replace(/,/g, ''))}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 text-left font-mono"
              />
              <span className="text-[10px] text-slate-400 mt-0.5 block">سال ساخت نباید جداکننده هزارگان داشته باشد</span>
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                تیپ خودرو (اختیاری)
              </label>
              <input
                type="text"
                placeholder="مثال: توربو اتوماتیک"
                value={trim}
                onChange={(e) => setTrim(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                حجم موتور (اختیاری)
              </label>
              <input
                type="text"
                placeholder="مثال: 1645cc یا 1.7L"
                value={engineDisplacement}
                onChange={(e) => setEngineDisplacement(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 text-left"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                کیلومتر فعلی <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                required
                placeholder="مثال: 52,255"
                value={currentMileageStr}
                onChange={handleMileageChange}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left font-bold"
              />
              <span className="text-[10px] text-cyan-600 dark:text-cyan-400 mt-0.5 block">
                {currentMileageStr ? `${currentMileageStr} کیلومتر` : '۰ کیلومتر'}
              </span>
            </div>
          </div>

          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                شماره پلاک (اختیاری)
              </label>
              <input
                type="text"
                placeholder="مثال: ۱۲ ب ۳۶۵ ایران ۳۳"
                value={licensePlate}
                onChange={(e) => setLicensePlate(e.target.value)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 text-center font-bold"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
                شماره شاسی / VIN (اختیاری)
              </label>
              <input
                type="text"
                placeholder="مثال: IRNC1402..."
                value={vin}
                onChange={(e) => setVin(e.target.value.toUpperCase())}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left uppercase"
              />
            </div>
          </div>

          <div className="pt-4 border-t border-slate-200 dark:border-slate-800 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2 text-xs sm:text-sm font-bold rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
            >
              انصراف
            </button>
            <button
              type="submit"
              className="px-5 py-2 text-xs sm:text-sm font-bold rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white shadow-sm transition active:scale-98"
            >
              {initialVehicle ? 'ذخیره تغییرات' : 'افزودن خودرو'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
