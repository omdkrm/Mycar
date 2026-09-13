import React, { useState, useRef } from 'react';
import {
  Settings as SettingsIcon,
  Download,
  Upload,
  RotateCcw,
  Moon,
  Sun,
  ShieldCheck,
  Check,
  AlertTriangle,
  Info,
  Smartphone,
  HardDrive,
} from 'lucide-react';
import { AppSettings } from '../types';
import { StorageService } from '../services/storage';

interface SettingsViewProps {
  settings: AppSettings;
  onUpdateSettings: (newSettings: Partial<AppSettings>) => void;
  onDataRestored: () => void;
}

export const SettingsView: React.FC<SettingsViewProps> = ({
  settings,
  onUpdateSettings,
  onDataRestored,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const [backupMessage, setBackupMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);
  const [showResetConfirm, setShowResetConfirm] = useState(false);

  // Handle export JSON backup
  const handleExportBackup = () => {
    try {
      const jsonStr = StorageService.exportBackupJson();
      const blob = new Blob([jsonStr], { type: 'application/json;charset=utf-8;' });
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `MyCar_Backup_${new Date().toISOString().slice(0, 10)}.json`);
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
      setBackupMessage({ type: 'success', text: 'فایل پشتیبان کامل با موفقیت تولید و دانلود گردید.' });
    } catch (e: any) {
      setBackupMessage({ type: 'error', text: 'خطا در ایجاد پشتیبان: ' + e.message });
    }
  };

  // Handle restore JSON backup
  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;

    const reader = new FileReader();
    reader.onload = (evt) => {
      try {
        const text = evt.target?.result as string;
        const res = StorageService.restoreBackupJson(text);
        if (res.success) {
          setBackupMessage({ type: 'success', text: 'اطلاعات با موفقیت از فایل پشتیبان بازیابی شد.' });
          onDataRestored();
        } else {
          setBackupMessage({ type: 'error', text: res.message || 'فایل نامعتبر است.' });
        }
      } catch (err: any) {
        setBackupMessage({ type: 'error', text: 'خطا در خواندن فایل: ' + err.message });
      }
    };
    reader.readAsText(file);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  // Handle Clear all user data
  const handleClearAllData = () => {
    StorageService.clearAllUserData();
    setShowResetConfirm(false);
    setBackupMessage({ type: 'success', text: 'تمامی اطلاعات خودروها و سوابق با موفقیت پاکسازی شد و برنامه با صفر رکورد بازنشانی گردید.' });
    onDataRestored();
  };

  return (
    <div className="max-w-4xl mx-auto space-y-6">
      {/* Header */}
      <div>
        <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
          <SettingsIcon className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
          <span>تنظیمات، پشتیبان‌گیری و اطلاعات سامانه</span>
        </h1>
        <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
          مدیریت نسخه پشتیبان محلی (Offline-First)، تم تاریک/روشن و تنظیمات شخصی
        </p>
      </div>

      {backupMessage && (
        <div
          className={`p-4 rounded-2xl text-xs sm:text-sm font-bold flex items-center gap-2 ${
            backupMessage.type === 'success'
              ? 'bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-900 text-emerald-700 dark:text-emerald-300'
              : 'bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300'
          }`}
        >
          {backupMessage.type === 'success' ? <Check className="w-5 h-5" /> : <AlertTriangle className="w-5 h-5" />}
          <span>{backupMessage.text}</span>
        </div>
      )}

      {/* Backup and Restore Box */}
      <div className="bg-white dark:bg-slate-900 p-6 sm:p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-5">
        <div className="flex items-center gap-3">
          <HardDrive className="w-6 h-6 text-cyan-600 dark:text-cyan-400" />
          <div>
            <h3 className="font-extrabold text-base text-slate-900 dark:text-slate-100">
              پشتیبان‌گیری و بازیابی اطلاعات (Local-first Backup & Restore)
            </h3>
            <p className="text-xs text-slate-500 dark:text-slate-400 mt-0.5">
              تمام اطلاعات خودروها، کیلومترها، سوابق سرویس و سوخت به صورت فایل امن JSON ذخیره یا بازیابی می‌شوند.
            </p>
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4 pt-2">
          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60 flex flex-col justify-between space-y-3">
            <div>
              <span className="font-bold text-sm text-slate-800 dark:text-slate-200 block mb-1">
                تهیه فایل پشتیبان (خروجی JSON)
              </span>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                یک فایل کامل شامل تمام ناوگان و سوابق شما دانلود می‌شود تا در صورت تعویض گوشی یا مرورگر قابل استفاده باشد.
              </p>
            </div>
            <button
              onClick={handleExportBackup}
              className="flex items-center justify-center gap-2 bg-cyan-600 hover:bg-cyan-700 text-white font-bold text-xs sm:text-sm py-2.5 px-4 rounded-xl transition shadow-xs"
            >
              <Download className="w-4 h-4" />
              <span>دانلود فایل پشتیبان (JSON)</span>
            </button>
          </div>

          <div className="p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/60 border border-slate-200 dark:border-slate-700/60 flex flex-col justify-between space-y-3">
            <div>
              <span className="font-bold text-sm text-slate-800 dark:text-slate-200 block mb-1">
                بازیابی از فایل پشتیبان
              </span>
              <p className="text-xs text-slate-500 dark:text-slate-400">
                انتخاب فایل پشتیبان JSON قبلی و بازگردانی فوری تمام ناوگان و گزارش‌ها بدون نیاز به اینترنت.
              </p>
            </div>
            <div>
              <input
                type="file"
                ref={fileInputRef}
                accept=".json,application/json"
                onChange={handleFileChange}
                className="hidden"
              />
              <button
                onClick={() => fileInputRef.current?.click()}
                className="w-full flex items-center justify-center gap-2 bg-slate-800 hover:bg-slate-900 dark:bg-slate-700 dark:hover:bg-slate-600 text-white font-bold text-xs sm:text-sm py-2.5 px-4 rounded-xl transition shadow-xs"
              >
                <Upload className="w-4 h-4" />
                <span>انتخاب و بازگردانی فایل JSON</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Preferences Box */}
      <div className="bg-white dark:bg-slate-900 p-6 sm:p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-5">
        <h3 className="font-extrabold text-base text-slate-900 dark:text-slate-100">
          تنظیمات ظاهری و نمایش
        </h3>

        <div className="divide-y divide-slate-100 dark:divide-slate-800 text-xs sm:text-sm">
          {/* Theme */}
          <div className="py-4 flex items-center justify-between">
            <div>
              <span className="font-bold text-slate-800 dark:text-slate-200 block">حالت نمایش (تم)</span>
              <span className="text-xs text-slate-400">انتخاب بین حالت روشن استاندارد و حالت تیره مخصوص شب</span>
            </div>
            <div className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 p-1 rounded-xl">
              <button
                onClick={() => onUpdateSettings({ theme: 'light' })}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-bold text-xs transition ${
                  settings.theme === 'light' ? 'bg-white text-cyan-600 shadow-xs' : 'text-slate-500'
                }`}
              >
                <Sun className="w-4 h-4" />
                <span>روشن</span>
              </button>
              <button
                onClick={() => onUpdateSettings({ theme: 'dark' })}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg font-bold text-xs transition ${
                  settings.theme === 'dark' ? 'bg-slate-900 text-cyan-400 shadow-xs' : 'text-slate-500'
                }`}
              >
                <Moon className="w-4 h-4" />
                <span>تاریک</span>
              </button>
            </div>
          </div>

          {/* Unit of Currency */}
          <div className="py-4 flex items-center justify-between">
            <div>
              <span className="font-bold text-slate-800 dark:text-slate-200 block">واحد پولی نمایش مبالغ</span>
              <span className="text-xs text-slate-400">نمایش هزینه‌ها بر حسب تومان (پیش‌فرض استاندارد ایران)</span>
            </div>
            <select
              value={settings.currencyUnit}
              onChange={(e) => onUpdateSettings({ currencyUnit: e.target.value as any })}
              className="px-3 py-1.5 text-xs rounded-xl border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-800 font-bold"
            >
              <option value="تومان">تومان</option>
              <option value="ریال">ریال</option>
            </select>
          </div>

          {/* Distance Unit */}
          <div className="py-4 flex items-center justify-between">
            <div>
              <span className="font-bold text-slate-800 dark:text-slate-200 block">واحد مسافت و پیمایش</span>
              <span className="text-xs text-slate-400">کیلومتر (km)</span>
            </div>
            <span className="font-bold text-cyan-600 font-mono">کیلومتر</span>
          </div>
        </div>
      </div>

      {/* Application Specifications Box */}
      <div className="bg-white dark:bg-slate-900 p-6 sm:p-8 rounded-3xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-4">
        <div className="flex items-center gap-2 text-cyan-600 dark:text-cyan-400 font-bold text-sm">
          <Smartphone className="w-5 h-5" />
          <span>مشخصات فنی و پکیج اپلیکیشن (com.mycar.app)</span>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-3 text-xs">
          <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
            <span className="text-slate-400 block mb-0.5">شناسه پکیج (Package Name):</span>
            <strong className="text-slate-900 dark:text-slate-100 font-mono text-xs">
              com.mycar.app
            </strong>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
            <span className="text-slate-400 block mb-0.5">نسخه اپلیکیشن:</span>
            <strong className="text-slate-900 dark:text-slate-100 font-mono text-xs">
              Version 1.0.0 (Build 1)
            </strong>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
            <span className="text-slate-400 block mb-0.5">نوع معماری و ذخیره‌سازی:</span>
            <strong className="text-slate-900 dark:text-slate-100 font-mono text-xs">
              Local-First / Offline Native Engine
            </strong>
          </div>

          <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
            <span className="text-slate-400 block mb-0.5">قلم و راست‌چین:</span>
            <strong className="text-slate-900 dark:text-slate-100 font-mono text-xs">
              Vazirmatn (RTL Standard)
            </strong>
          </div>
        </div>

        {/* Clear Database Button */}
        <div className="pt-4 border-t border-slate-100 dark:border-slate-800 flex flex-col sm:flex-row sm:items-center justify-between gap-3">
          <div className="text-xs text-slate-400">
            پاکسازی کلیه داده‌های کاربری و بازنشانی دیتابیس به حالت خام (صفر رکورد):
          </div>

          {showResetConfirm ? (
            <div className="flex items-center gap-2">
              <span className="text-xs text-rose-600 font-bold">تمام سوابق حذف شود؟</span>
              <button
                onClick={handleClearAllData}
                className="px-3 py-1.5 bg-rose-600 text-white rounded-lg text-xs font-bold shadow-xs hover:bg-rose-700 transition"
              >
                بله، پاکسازی کامل
              </button>
              <button
                onClick={() => setShowResetConfirm(false)}
                className="px-2.5 py-1.5 bg-slate-200 dark:bg-slate-700 text-slate-700 dark:text-slate-300 rounded-lg text-xs font-bold transition"
              >
                انصراف
              </button>
            </div>
          ) : (
            <button
              onClick={() => setShowResetConfirm(true)}
              className="flex items-center gap-1.5 text-xs font-bold text-rose-600 hover:text-rose-700 py-2 px-3 rounded-lg hover:bg-rose-50 dark:hover:bg-rose-950/40 transition border border-rose-200 dark:border-rose-900/60"
            >
              <RotateCcw className="w-3.5 h-3.5" />
              <span>پاکسازی کامل اطلاعات کاربر</span>
            </button>
          )}
        </div>
      </div>
    </div>
  );
};
