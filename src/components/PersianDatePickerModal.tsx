import React, { useState, useMemo } from 'react';
import { ChevronRight, ChevronLeft, Calendar as CalendarIcon, Check, X } from 'lucide-react';
import {
  PERSIAN_MONTH_NAMES,
  PERSIAN_WEEK_DAYS_SHORT,
  getDaysInJalaliMonth,
  getFirstDayOfJalaliMonth,
  gregorianToJalali
} from '../utils/persianDate';
import { toPersianDigits } from '../utils/formatters';

interface PersianDatePickerModalProps {
  isOpen: boolean;
  initialDateStr?: string; // e.g. "1404/06/14"
  onClose: () => void;
  onSelectDate: (dateStr: string) => void;
}

export const PersianDatePickerModal: React.FC<PersianDatePickerModalProps> = ({
  isOpen,
  initialDateStr,
  onClose,
  onSelectDate
}) => {
  const todayJalali = useMemo(() => {
    const now = new Date();
    return gregorianToJalali(now.getFullYear(), now.getMonth() + 1, now.getDate());
  }, []);

  // Parse initial date or default to today
  const [selectedYear, setSelectedYear] = useState<number>(() => {
    if (initialDateStr && initialDateStr.includes('/')) {
      const parts = initialDateStr.split('/');
      const y = parseInt(parts[0], 10);
      if (!isNaN(y) && y >= 1300 && y <= 1500) return y;
    }
    return todayJalali.jy;
  });

  const [selectedMonth, setSelectedMonth] = useState<number>(() => {
    if (initialDateStr && initialDateStr.includes('/')) {
      const parts = initialDateStr.split('/');
      const m = parseInt(parts[1], 10);
      if (!isNaN(m) && m >= 1 && m <= 12) return m;
    }
    return todayJalali.jm;
  });

  const [selectedDay, setSelectedDay] = useState<number>(() => {
    if (initialDateStr && initialDateStr.includes('/')) {
      const parts = initialDateStr.split('/');
      const d = parseInt(parts[2], 10);
      if (!isNaN(d) && d >= 1 && d <= 31) return d;
    }
    return todayJalali.jd;
  });

  if (!isOpen) return null;

  const daysInMonth = getDaysInJalaliMonth(selectedYear, selectedMonth);
  const firstDayOffset = getFirstDayOfJalaliMonth(selectedYear, selectedMonth);

  const handlePrevMonth = () => {
    if (selectedMonth === 1) {
      setSelectedYear((y) => y - 1);
      setSelectedMonth(12);
    } else {
      setSelectedMonth((m) => m - 1);
    }
  };

  const handleNextMonth = () => {
    if (selectedMonth === 12) {
      setSelectedYear((y) => y + 1);
      setSelectedMonth(1);
    } else {
      setSelectedMonth((m) => m + 1);
    }
  };

  const handleConfirm = () => {
    const formattedMonth = selectedMonth.toString().padStart(2, '0');
    const formattedDay = Math.min(selectedDay, daysInMonth).toString().padStart(2, '0');
    onSelectDate(`${selectedYear}/${formattedMonth}/${formattedDay}`);
    onClose();
  };

  const setToday = () => {
    setSelectedYear(todayJalali.jy);
    setSelectedMonth(todayJalali.jm);
    setSelectedDay(todayJalali.jd);
  };

  return (
    <div
      id="persian-date-picker-backdrop"
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/60 backdrop-blur-sm"
      onClick={onClose}
    >
      <div
        id="persian-date-picker-dialog"
        className="w-full max-w-sm bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col"
        onClick={(e) => e.stopPropagation()}
        dir="rtl"
      >
        {/* Header */}
        <div className="p-4 bg-gradient-to-l from-cyan-600 to-blue-600 text-white flex items-center justify-between">
          <div className="flex items-center gap-2">
            <CalendarIcon className="w-5 h-5" />
            <span className="font-bold text-base">انتخاب تاریخ</span>
          </div>
          <button
            type="button"
            onClick={setToday}
            className="px-2.5 py-1 text-xs font-bold rounded-lg bg-white/20 hover:bg-white/30 text-white transition-colors"
          >
            امروز
          </button>
        </div>

        {/* Selected date display */}
        <div className="px-5 py-3 bg-slate-50 dark:bg-slate-800/60 border-b border-slate-200 dark:border-slate-800 flex items-center justify-between">
          <span className="text-xs text-slate-500 dark:text-slate-400 font-medium">تاریخ انتخاب شده:</span>
          <span className="text-sm font-bold text-cyan-600 dark:text-cyan-400 font-mono">
            {toPersianDigits(
              `${selectedYear}/${selectedMonth.toString().padStart(2, '0')}/${Math.min(selectedDay, daysInMonth).toString().padStart(2, '0')}`
            )}
          </span>
        </div>

        {/* Month & Year Navigation */}
        <div className="p-4 flex items-center justify-between border-b border-slate-100 dark:border-slate-800">
          <button
            type="button"
            onClick={handleNextMonth}
            className="p-1.5 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
            aria-label="ماه بعد"
          >
            <ChevronRight className="w-5 h-5" />
          </button>

          <div className="flex items-center gap-2">
            <span className="font-bold text-slate-800 dark:text-slate-200 text-sm">
              {PERSIAN_MONTH_NAMES[selectedMonth - 1]}
            </span>
            <select
              value={selectedYear}
              onChange={(e) => setSelectedYear(Number(e.target.value))}
              className="bg-transparent font-bold text-cyan-600 dark:text-cyan-400 text-sm cursor-pointer outline-none font-mono"
            >
              {Array.from({ length: 30 }, (_, i) => 1390 + i).map((year) => (
                <option key={year} value={year} className="bg-white dark:bg-slate-900 text-slate-800 dark:text-slate-200">
                  {toPersianDigits(year.toString())}
                </option>
              ))}
            </select>
          </div>

          <button
            type="button"
            onClick={handlePrevMonth}
            className="p-1.5 rounded-full hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-600 dark:text-slate-300"
            aria-label="ماه قبل"
          >
            <ChevronLeft className="w-5 h-5" />
          </button>
        </div>

        {/* Week Days Header */}
        <div className="grid grid-cols-7 gap-1 px-4 pt-3 text-center">
          {PERSIAN_WEEK_DAYS_SHORT.map((dayName, idx) => (
            <div
              key={idx}
              className={`text-xs font-bold py-1 ${idx === 6 ? 'text-rose-500' : 'text-slate-400 dark:text-slate-500'}`}
            >
              {dayName}
            </div>
          ))}
        </div>

        {/* Days Grid */}
        <div className="grid grid-cols-7 gap-1 p-4">
          {/* Empty prefix slots */}
          {Array.from({ length: firstDayOffset }).map((_, idx) => (
            <div key={`empty-${idx}`} className="h-9 w-9" />
          ))}

          {/* Month days */}
          {Array.from({ length: daysInMonth }).map((_, idx) => {
            const dayNum = idx + 1;
            const isSelected = selectedDay === dayNum;
            const isToday =
              todayJalali.jy === selectedYear &&
              todayJalali.jm === selectedMonth &&
              todayJalali.jd === dayNum;

            return (
              <button
                key={dayNum}
                type="button"
                onClick={() => setSelectedDay(dayNum)}
                className={`h-9 w-9 mx-auto rounded-xl flex items-center justify-center text-sm font-bold transition-all ${
                  isSelected
                    ? 'bg-cyan-600 text-white shadow-md shadow-cyan-600/30 font-extrabold'
                    : isToday
                    ? 'border-2 border-cyan-500 text-cyan-600 dark:text-cyan-400 hover:bg-cyan-50 dark:hover:bg-cyan-950/30'
                    : 'text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800'
                }`}
              >
                {toPersianDigits(dayNum.toString())}
              </button>
            );
          })}
        </div>

        {/* Actions Footer */}
        <div className="p-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-end gap-2 bg-slate-50/50 dark:bg-slate-800/30">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 text-xs font-bold text-slate-600 dark:text-slate-400 hover:bg-slate-200 dark:hover:bg-slate-700 rounded-xl transition-colors flex items-center gap-1.5"
          >
            <X className="w-3.5 h-3.5" />
            انصراف
          </button>
          <button
            type="button"
            onClick={handleConfirm}
            className="px-4 py-2 text-xs font-bold text-white bg-cyan-600 hover:bg-cyan-700 rounded-xl shadow-md shadow-cyan-600/20 transition-all flex items-center gap-1.5"
          >
            <Check className="w-3.5 h-3.5" />
            تأیید و انتخاب
          </button>
        </div>
      </div>
    </div>
  );
};
