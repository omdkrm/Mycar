import React from 'react';
import { Car, Moon, Sun, Bell, Plus, ChevronDown } from 'lucide-react';
import { Vehicle } from '../types';
import { formatWithCommas } from '../utils/formatters';

interface HeaderProps {
  vehicles: Vehicle[];
  selectedVehicle: Vehicle | null;
  onSelectVehicle: (id: string) => void;
  onAddVehicleClick: () => void;
  isDarkMode: boolean;
  onToggleTheme: () => void;
  activeRemindersCount: number;
  onOpenReminders: () => void;
  onOpenVehiclesList: () => void;
}

export const Header: React.FC<HeaderProps> = ({
  vehicles,
  selectedVehicle,
  onSelectVehicle,
  onAddVehicleClick,
  isDarkMode,
  onToggleTheme,
  activeRemindersCount,
  onOpenReminders,
  onOpenVehiclesList,
}) => {
  const [dropdownOpen, setDropdownOpen] = React.useState(false);

  return (
    <header className="sticky top-0 z-40 bg-white/95 dark:bg-slate-900/95 backdrop-blur border-b border-slate-200 dark:border-slate-800 px-4 py-3 shadow-xs">
      <div className="max-w-7xl mx-auto flex items-center justify-between gap-3">
        {/* Brand & Vehicle Switcher */}
        <div className="flex items-center gap-3">
          <button
            onClick={onOpenVehiclesList}
            className="flex items-center gap-2 text-cyan-600 dark:text-cyan-400 font-black text-xl hover:opacity-90 transition-opacity"
            title="فهرست خودروها"
          >
            <div className="w-10 h-10 rounded-xl bg-cyan-600 text-white flex items-center justify-center shadow-md shadow-cyan-600/20">
              <Car className="w-6 h-6" />
            </div>
            <div className="text-right hidden sm:block">
              <div className="text-base font-extrabold text-slate-900 dark:text-slate-100 leading-tight">
                خودروهای من
              </div>
              <div className="text-xs font-medium text-cyan-600 dark:text-cyan-400">My Car App</div>
            </div>
          </button>

          {/* Active Vehicle Dropdown */}
          {selectedVehicle && (
            <div className="relative">
              <button
                type="button"
                onClick={() => setDropdownOpen(!dropdownOpen)}
                className="flex items-center gap-2 bg-slate-100 dark:bg-slate-800 hover:bg-slate-200 dark:hover:bg-slate-700 text-slate-800 dark:text-slate-200 text-xs sm:text-sm font-semibold py-1.5 px-3 rounded-lg border border-slate-200 dark:border-slate-700 transition"
              >
                <span className="w-2 h-2 rounded-full bg-cyan-500 animate-pulse"></span>
                <span className="max-w-[120px] sm:max-w-[180px] truncate">{selectedVehicle.name}</span>
                <span className="text-[11px] text-slate-500 dark:text-slate-400 hidden md:inline">
                  ({formatWithCommas(selectedVehicle.currentMileage)} ک‌م)
                </span>
                <ChevronDown className="w-4 h-4 text-slate-400" />
              </button>

              {dropdownOpen && (
                <>
                  <div
                    className="fixed inset-0 z-40"
                    onClick={() => setDropdownOpen(false)}
                  />
                  <div className="absolute right-0 mt-2 w-64 bg-white dark:bg-slate-800 rounded-xl shadow-xl border border-slate-200 dark:border-slate-700 py-2 z-50 animate-in fade-in">
                    <div className="px-3 py-1.5 text-xs font-bold text-slate-400">
                      انتخاب خودرو فعال:
                    </div>
                    {vehicles.map((v) => (
                      <button
                        key={v.id}
                        type="button"
                        onClick={() => {
                          onSelectVehicle(v.id);
                          setDropdownOpen(false);
                        }}
                        className={`w-full text-right px-3 py-2 text-xs sm:text-sm flex items-center justify-between hover:bg-slate-100 dark:hover:bg-slate-700/60 ${
                          v.id === selectedVehicle.id
                            ? 'bg-cyan-50 dark:bg-cyan-950/40 text-cyan-600 dark:text-cyan-400 font-bold'
                            : 'text-slate-700 dark:text-slate-300'
                        }`}
                      >
                        <span className="truncate">{v.name}</span>
                        <span className="text-[11px] text-slate-400 font-normal">
                          {formatWithCommas(v.currentMileage)} ک‌م
                        </span>
                      </button>
                    ))}
                    <div className="border-t border-slate-200 dark:border-slate-700 mt-1 pt-1">
                      <button
                        type="button"
                        onClick={() => {
                          setDropdownOpen(false);
                          onAddVehicleClick();
                        }}
                        className="w-full text-right px-3 py-2 text-xs text-cyan-600 dark:text-cyan-400 font-bold hover:bg-cyan-50 dark:hover:bg-cyan-950/40 flex items-center gap-1.5"
                      >
                        <Plus className="w-3.5 h-3.5" />
                        افزودن خودرو جدید...
                      </button>
                    </div>
                  </div>
                </>
              )}
            </div>
          )}
        </div>

        {/* Action icons */}
        <div className="flex items-center gap-1.5 sm:gap-2">
          {/* Reminders Button */}
          <button
            onClick={onOpenReminders}
            className="relative p-2 rounded-lg text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
            title="یادآوری‌های من"
          >
            <Bell className="w-5 h-5" />
            {activeRemindersCount > 0 && (
              <span className="absolute top-1 right-1 w-5 h-5 bg-rose-500 text-white rounded-full text-[10px] font-bold flex items-center justify-center animate-bounce shadow-sm">
                {activeRemindersCount}
              </span>
            )}
          </button>

          {/* Theme Toggle */}
          <button
            onClick={onToggleTheme}
            className="p-2 rounded-lg text-slate-600 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
            title={isDarkMode ? 'حالت روز' : 'حالت شب'}
          >
            {isDarkMode ? <Sun className="w-5 h-5 text-amber-400" /> : <Moon className="w-5 h-5 text-slate-600" />}
          </button>

          {/* New Vehicle Button */}
          <button
            onClick={onAddVehicleClick}
            className="flex items-center gap-1.5 bg-cyan-600 hover:bg-cyan-700 text-white text-xs sm:text-sm font-bold py-2 px-3 sm:px-4 rounded-xl shadow-sm transition active:scale-98"
          >
            <Plus className="w-4 h-4" />
            <span className="hidden sm:inline">افزودن خودرو</span>
            <span className="sm:hidden">خودرو</span>
          </button>
        </div>
      </div>
    </header>
  );
};
