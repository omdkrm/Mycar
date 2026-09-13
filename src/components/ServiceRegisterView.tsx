import React, { useState, useEffect } from 'react';
import { Wrench, Calculator, Calendar, Upload, Image as ImageIcon, Check, ArrowRight, DollarSign } from 'lucide-react';
import {
  Vehicle,
  CatalogItem,
  ServiceRecord,
  OperationType,
  PartCategory,
  VehicleMaintenanceSchedule,
} from '../types';
import { normalizeDigits, parseNumericInput, formatWithCommas, formatMileage, formatCurrency } from '../utils/formatters';
import { formatToJalali, parseJalaliStringToDate } from '../utils/persianDate';

interface ServiceRegisterViewProps {
  vehicles: Vehicle[];
  selectedVehicleId: string;
  catalogItems: CatalogItem[];
  schedules: VehicleMaintenanceSchedule[];
  initialRecord?: ServiceRecord | null;
  prefillPartName?: string;
  prefillCatalogItemId?: string;
  onSaveRecord: (record: Partial<ServiceRecord>) => { success: boolean; message?: string };
  onCancel: () => void;
}

export const ServiceRegisterView: React.FC<ServiceRegisterViewProps> = ({
  vehicles,
  selectedVehicleId,
  catalogItems,
  schedules,
  initialRecord,
  prefillPartName,
  prefillCatalogItemId,
  onSaveRecord,
  onCancel,
}) => {
  const currentVehicle = vehicles.find((v) => v.id === selectedVehicleId) || vehicles[0];

  const [vehicleId, setVehicleId] = useState(selectedVehicleId);
  const [catalogItemId, setCatalogItemId] = useState(prefillCatalogItemId || '');
  const [partName, setPartName] = useState(prefillPartName || '');
  const [category, setCategory] = useState<PartCategory>('موتور');
  const [operationType, setOperationType] = useState<OperationType>('تعویض');
  const [jalaliDateStr, setJalaliDateStr] = useState(formatToJalali(Date.now()));
  const [mileageStr, setMileageStr] = useState(currentVehicle ? formatWithCommas(currentVehicle.currentMileage) : '0');
  const [partPriceStr, setPartPriceStr] = useState('');
  const [laborCostStr, setLaborCostStr] = useState('');
  const [repairShop, setRepairShop] = useState('');
  const [description, setDescription] = useState('');
  const [invoicePhotoUrl, setInvoicePhotoUrl] = useState<string | undefined>(undefined);
  const [nextMileageStr, setNextMileageStr] = useState('');
  const [nextJalaliDateStr, setNextJalaliDateStr] = useState('');

  const [errorMessage, setErrorMessage] = useState('');
  const [successMessage, setSuccessMessage] = useState('');

  // When initialRecord is passed (for editing)
  useEffect(() => {
    if (initialRecord) {
      setVehicleId(initialRecord.vehicleId);
      setCatalogItemId(initialRecord.catalogItemId || '');
      setPartName(initialRecord.partName);
      setCategory(initialRecord.category);
      setOperationType(initialRecord.operationType);
      setJalaliDateStr(formatToJalali(initialRecord.dateTimestamp));
      setMileageStr(formatWithCommas(initialRecord.mileage));
      setPartPriceStr(formatWithCommas(initialRecord.partPrice));
      setLaborCostStr(formatWithCommas(initialRecord.laborCost));
      setRepairShop(initialRecord.repairShop || '');
      setDescription(initialRecord.description || '');
      setInvoicePhotoUrl(initialRecord.invoicePhotoUrl);
      if (initialRecord.nextMileage) {
        setNextMileageStr(formatWithCommas(initialRecord.nextMileage));
      }
      if (initialRecord.nextDueDateTimestamp) {
        setNextJalaliDateStr(formatToJalali(initialRecord.nextDueDateTimestamp));
      }
    } else if (prefillPartName || prefillCatalogItemId) {
      const match = catalogItems.find(
        (c) => c.id === prefillCatalogItemId || c.name === prefillPartName
      );
      if (match) {
        setCatalogItemId(match.id);
        setPartName(match.name);
        setCategory(match.category);
      }
    }
  }, [initialRecord, prefillPartName, prefillCatalogItemId, catalogItems]);

  // Recalculate automatic next service mileage & date when part or mileage changes
  useEffect(() => {
    if (initialRecord && nextMileageStr) return; // don't override manual during edit

    const targetVehicle = vehicles.find((v) => v.id === vehicleId);
    if (!targetVehicle) return;

    // Find schedule for this vehicle and item
    const sched = schedules.find(
      (s) => s.vehicleId === vehicleId && (s.catalogItemId === catalogItemId || s.partName === partName)
    );

    const enteredMileage = parseNumericInput(mileageStr);
    if (sched && enteredMileage > 0) {
      setNextMileageStr(formatWithCommas(enteredMileage + sched.kmInterval));
      const parsedDate = parseJalaliStringToDate(jalaliDateStr);
      if (parsedDate) {
        const nextTime = parsedDate.getTime() + sched.timeIntervalMonths * 30.44 * 24 * 60 * 60 * 1000;
        setNextJalaliDateStr(formatToJalali(nextTime));
      }
    } else if (enteredMileage > 0) {
      // Default 8,000 km / 6 months
      setNextMileageStr(formatWithCommas(enteredMileage + 8000));
    }
  }, [catalogItemId, partName, mileageStr, jalaliDateStr, vehicleId, schedules, vehicles, initialRecord]);

  const handleCatalogSelect = (e: React.ChangeEvent<HTMLSelectElement>) => {
    const selectedId = e.target.value;
    setCatalogItemId(selectedId);
    const item = catalogItems.find((c) => c.id === selectedId);
    if (item) {
      setPartName(item.name);
      setCategory(item.category);
    }
  };

  const handlePriceChange = (val: string, setter: (s: string) => void) => {
    const raw = normalizeDigits(val).replace(/,/g, '');
    const num = parseInt(raw, 10);
    setter(isNaN(num) ? '' : formatWithCommas(num));
  };

  const partPrice = parseNumericInput(partPriceStr);
  const laborCost = parseNumericInput(laborCostStr);
  const totalCost = partPrice + laborCost;

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

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage('');
    setSuccessMessage('');

    if (!vehicleId) {
      setErrorMessage('لطفا خودرو را انتخاب کنید.');
      return;
    }
    if (!partName.trim()) {
      setErrorMessage('لطفا نام قطعه یا سرویس را وارد یا انتخاب کنید.');
      return;
    }

    const parsedMileage = parseNumericInput(mileageStr);
    if (parsedMileage <= 0) {
      setErrorMessage('لطفا کیلومتر معتبر وارد کنید.');
      return;
    }

    const dateObj = parseJalaliStringToDate(jalaliDateStr);
    if (!dateObj) {
      setErrorMessage('تاریخ شمسی نامعتبر است (الگوی صحیح: ۱۴۰۴/۰۶/۱۴).');
      return;
    }

    const nextMileage = nextMileageStr ? parseNumericInput(nextMileageStr) : undefined;
    const nextDateObj = nextJalaliDateStr ? parseJalaliStringToDate(nextJalaliDateStr) : undefined;

    const payload: Partial<ServiceRecord> = {
      id: initialRecord ? initialRecord.id : undefined,
      vehicleId,
      catalogItemId: catalogItemId || undefined,
      partName: partName.trim(),
      category,
      operationType,
      dateTimestamp: dateObj.getTime(),
      mileage: parsedMileage,
      partPrice,
      laborCost,
      totalCost,
      repairShop: repairShop.trim() || undefined,
      description: description.trim() || undefined,
      invoicePhotoUrl,
      nextMileage: nextMileage && nextMileage > 0 ? nextMileage : undefined,
      nextDueDateTimestamp: nextDateObj ? nextDateObj.getTime() : undefined,
    };

    const res = onSaveRecord(payload);
    if (res.success) {
      setSuccessMessage('سرویس با موفقیت ثبت شد و محاسبات دوره‌های بعدی به‌روزرسانی گردید.');
      setTimeout(() => {
        onCancel();
      }, 1200);
    } else {
      setErrorMessage(res.message || 'خطا در ثبت سرویس.');
    }
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Title */}
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <Wrench className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
            <span>{initialRecord ? 'ویرایش سابقه سرویس' : 'ثبت سرویس / تعویض قطعه'}</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            ثبت مشخصات، قطعات، اجرت و فاکتور سرویس‌های انجام شده با محاسبه خودکار موعد بعدی
          </p>
        </div>
        <button
          type="button"
          onClick={onCancel}
          className="text-xs sm:text-sm font-bold text-slate-500 hover:text-slate-800 dark:hover:text-slate-200"
        >
          بازگشت
        </button>
      </div>

      {errorMessage && (
        <div className="p-4 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 rounded-2xl text-xs sm:text-sm font-bold text-rose-600 dark:text-rose-400">
          {errorMessage}
        </div>
      )}

      {successMessage && (
        <div className="p-4 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-900 rounded-2xl text-xs sm:text-sm font-bold text-emerald-600 dark:text-emerald-400 flex items-center gap-2">
          <Check className="w-5 h-5" />
          <span>{successMessage}</span>
        </div>
      )}

      {/* Form Card */}
      <form onSubmit={handleSubmit} className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-3xl p-6 sm:p-8 shadow-xs space-y-6">
        {/* Row 1: Vehicle & Catalog selection */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              انتخاب خودرو <span className="text-rose-500">*</span>
            </label>
            <select
              value={vehicleId}
              onChange={(e) => setVehicleId(e.target.value)}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-cyan-500"
            >
              {vehicles.map((v) => (
                <option key={v.id} value={v.id}>
                  {v.name} ({v.brand} {v.model}) - {formatMileage(v.currentMileage)}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              انتخاب از کاتالوگ قطعات و سرویس‌ها
            </label>
            <select
              value={catalogItemId}
              onChange={handleCatalogSelect}
              className="w-full px-3 py-2.5 rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 text-sm font-medium focus:outline-none focus:ring-2 focus:ring-cyan-500"
            >
              <option value="">-- انتخاب از کاتالوگ استاندارد یا قطعه سفارشی --</option>
              {catalogItems.map((c) => (
                <option key={c.id} value={c.id}>
                  [{c.category}] {c.name}
                </option>
              ))}
            </select>
          </div>
        </div>

        {/* Row 2: Part Name, Category & Operation Type */}
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              نام قطعه یا عنوان سرویس <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="مثال: روغن موتور، لنت جلو..."
              value={partName}
              onChange={(e) => setPartName(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              دسته‌بندی <span className="text-rose-500">*</span>
            </label>
            <select
              value={category}
              onChange={(e) => setCategory(e.target.value as PartCategory)}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-medium"
            >
              <option value="موتور">موتور</option>
              <option value="ترمز">ترمز</option>
              <option value="جلوبندی و تعلیق">جلوبندی و تعلیق</option>
              <option value="سرویس‌های دوره‌ای">سرویس‌های دوره‌ای</option>
              <option value="سایر">سایر</option>
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              نوع عملیات <span className="text-rose-500">*</span>
            </label>
            <div className="grid grid-cols-3 gap-1 p-1 bg-slate-100 dark:bg-slate-800 rounded-xl">
              {(['تعویض', 'سرویس', 'تعمیر'] as OperationType[]).map((op) => (
                <button
                  key={op}
                  type="button"
                  onClick={() => setOperationType(op)}
                  className={`py-1.5 text-xs font-bold rounded-lg transition ${
                    operationType === op
                      ? 'bg-cyan-600 text-white shadow-xs'
                      : 'text-slate-600 dark:text-slate-400 hover:text-slate-900'
                  }`}
                >
                  {op}
                </button>
              ))}
            </div>
          </div>
        </div>

        {/* Row 3: Date & Mileage at service time */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <div className="flex items-center justify-between mb-1">
              <label className="text-xs font-bold text-slate-700 dark:text-slate-300">
                تاریخ انجام (شمسی) <span className="text-rose-500">*</span>
              </label>
              <button
                type="button"
                onClick={() => setJalaliDateStr(formatToJalali(Date.now()))}
                className="text-[10px] text-cyan-600 font-bold hover:underline"
              >
                امروز
              </button>
            </div>
            <input
              type="text"
              required
              placeholder="مثال: 1404/06/14"
              value={jalaliDateStr}
              onChange={(e) => setJalaliDateStr(normalizeDigits(e.target.value))}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              کیلومتر خودرو در زمان سرویس <span className="text-rose-500">*</span>
            </label>
            <input
              type="text"
              required
              placeholder="مثال: 52,000"
              value={mileageStr}
              onChange={(e) => handlePriceChange(e.target.value, setMileageStr)}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left font-bold"
            />
          </div>
        </div>

        {/* Row 4: Cost Breakdown (Auto Total Calculation) */}
        <div className="p-4 bg-slate-50 dark:bg-slate-800/60 rounded-2xl border border-slate-200 dark:border-slate-800 space-y-4">
          <div className="flex items-center gap-2 text-xs font-bold text-slate-700 dark:text-slate-300">
            <Calculator className="w-4 h-4 text-cyan-600" />
            <span>محاسبه هزینه‌ها (قیمت قطعه + اجرت):</span>
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <label className="block text-xs text-slate-500 dark:text-slate-400 mb-1">
                قیمت قطعه (تومان):
              </label>
              <input
                type="text"
                placeholder="۰"
                value={partPriceStr}
                onChange={(e) => handlePriceChange(e.target.value, setPartPriceStr)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left font-bold"
              />
            </div>

            <div>
              <label className="block text-xs text-slate-500 dark:text-slate-400 mb-1">
                اجرت و دستمزد (تومان):
              </label>
              <input
                type="text"
                placeholder="۰"
                value={laborCostStr}
                onChange={(e) => handlePriceChange(e.target.value, setLaborCostStr)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 focus:outline-none focus:ring-2 focus:ring-cyan-500 font-mono text-left font-bold"
              />
            </div>

            <div className="bg-cyan-50 dark:bg-cyan-950/60 p-3 rounded-xl border border-cyan-200 dark:border-cyan-900 flex flex-col justify-center">
              <span className="text-[11px] font-bold text-cyan-700 dark:text-cyan-400">
                مجموع هزینه (محاسبه خودکار):
              </span>
              <span className="text-lg font-black font-mono text-cyan-900 dark:text-cyan-200 mt-0.5">
                {formatCurrency(totalCost)}
              </span>
            </div>
          </div>
        </div>

        {/* Row 5: Provider & Description */}
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              تعمیرگاه / سرویس‌کار (اختیاری):
            </label>
            <input
              type="text"
              placeholder="مثال: نمایندگی ایران خودرو، اتوسرویس شایان..."
              value={repairShop}
              onChange={(e) => setRepairShop(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
              توضیحات و مشخصات برند قطعه:
            </label>
            <input
              type="text"
              placeholder="مثال: روغن بهران رانا 5w40 تمام سنتتیک ۴ لیتری"
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              className="w-full px-3 py-2 text-sm rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
          </div>
        </div>

        {/* Row 6: Next due targets (Auto calculated with manual adjustment) */}
        <div className="p-4 bg-emerald-50/60 dark:bg-emerald-950/30 rounded-2xl border border-emerald-200 dark:border-emerald-900 space-y-3">
          <div className="text-xs font-bold text-emerald-800 dark:text-emerald-300">
            محاسبه و تنظیم موعد سرویس بعدی (پیش‌فرض هوشمند بر اساس فواصل زمانی و پیمایش):
          </div>

          <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
            <div>
              <label className="block text-[11px] text-slate-600 dark:text-slate-400 mb-1 font-semibold">
                کیلومتر موعد بعدی:
              </label>
              <input
                type="text"
                placeholder="مثال: 60,000"
                value={nextMileageStr}
                onChange={(e) => handlePriceChange(e.target.value, setNextMileageStr)}
                className="w-full px-3 py-2 text-sm rounded-xl border border-emerald-300 dark:border-emerald-800 bg-white dark:bg-slate-900 font-mono text-left font-bold"
              />
            </div>

            <div>
              <label className="block text-[11px] text-slate-600 dark:text-slate-400 mb-1 font-semibold">
                تاریخ موعد بعدی (شمسی):
              </label>
              <input
                type="text"
                placeholder="مثال: 1404/12/14"
                value={nextJalaliDateStr}
                onChange={(e) => setNextJalaliDateStr(normalizeDigits(e.target.value))}
                className="w-full px-3 py-2 text-sm rounded-xl border border-emerald-300 dark:border-emerald-800 bg-white dark:bg-slate-900 font-mono text-left"
              />
            </div>
          </div>
        </div>

        {/* Row 7: Optional Photo / Invoice attachment */}
        <div>
          <label className="block text-xs font-bold text-slate-700 dark:text-slate-300 mb-1">
            تصویر فاکتور یا قطعه (اختیاری):
          </label>
          <div className="flex items-center gap-4">
            <label className="cursor-pointer flex items-center gap-2 px-4 py-2.5 rounded-xl border border-dashed border-slate-300 dark:border-slate-700 hover:border-cyan-500 text-xs font-bold text-slate-600 dark:text-slate-400 hover:text-cyan-600 transition">
              <Upload className="w-4 h-4" />
              <span>انتخاب عکس فاکتور...</span>
              <input
                type="file"
                accept="image/*"
                onChange={handlePhotoUpload}
                className="hidden"
              />
            </label>

            {invoicePhotoUrl && (
              <div className="relative flex items-center gap-2">
                <img
                  src={invoicePhotoUrl}
                  alt="Invoice"
                  className="w-12 h-12 object-cover rounded-lg border border-slate-300"
                />
                <button
                  type="button"
                  onClick={() => setInvoicePhotoUrl(undefined)}
                  className="text-xs text-rose-500 hover:underline"
                >
                  حذف عکس
                </button>
              </div>
            )}
          </div>
        </div>

        {/* Action Buttons */}
        <div className="flex items-center justify-end gap-3 pt-4 border-t border-slate-200 dark:border-slate-800">
          <button
            type="button"
            onClick={onCancel}
            className="px-5 py-2.5 text-xs sm:text-sm font-bold rounded-xl text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
          >
            انصراف
          </button>
          <button
            type="submit"
            className="px-6 py-2.5 text-xs sm:text-sm font-bold rounded-xl bg-cyan-600 hover:bg-cyan-700 text-white shadow-md transition active:scale-98 flex items-center gap-2"
          >
            <Check className="w-4 h-4" />
            <span>{initialRecord ? 'ذخیره ویرایش' : 'ثبت قطعی سرویس'}</span>
          </button>
        </div>
      </form>
    </div>
  );
};
