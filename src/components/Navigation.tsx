import React from 'react';
import {
  LayoutDashboard,
  Wrench,
  History,
  CalendarClock,
  Fuel,
  BarChart3,
  Search,
  FileSpreadsheet,
  Settings,
  Database,
  Layers,
  CarFront,
} from 'lucide-react';

export type NavTab =
  | 'vehicles'
  | 'dashboard'
  | 'register-service'
  | 'service-history'
  | 'part-history'
  | 'reminders'
  | 'fuel'
  | 'reports'
  | 'search'
  | 'export'
  | 'settings'
  | 'backup';

interface NavigationProps {
  currentTab?: NavTab;
  activeTab?: NavTab;
  onSelectTab?: (tab: NavTab) => void;
  onTabChange?: (tab: NavTab) => void;
  hasSelectedVehicle: boolean;
  overdueCount: number;
}

export const Navigation: React.FC<NavigationProps> = ({
  currentTab,
  activeTab,
  onSelectTab,
  onTabChange,
  hasSelectedVehicle,
  overdueCount,
}) => {
  const currentActiveTab = currentTab || activeTab || 'dashboard';
  const handleTabChange = (tab: NavTab) => {
    if (onSelectTab) onSelectTab(tab);
    if (onTabChange) onTabChange(tab);
  };
  const mainNavItems = [
    { id: 'dashboard', label: 'داشبورد خودرو', icon: LayoutDashboard, requiresVehicle: true },
    { id: 'register-service', label: 'ثبت سرویس', icon: Wrench, requiresVehicle: true },
    { id: 'reminders', label: 'یادآوری‌های من', icon: CalendarClock, badge: overdueCount, requiresVehicle: true },
    { id: 'service-history', label: 'تاریخچه سرویس‌ها', icon: History, requiresVehicle: true },
    { id: 'fuel', label: 'ثبت و مصرف سوخت', icon: Fuel, requiresVehicle: true },
    { id: 'part-history', label: 'تاریخچه هر قطعه', icon: Layers, requiresVehicle: true },
    { id: 'reports', label: 'گزارش هزینه‌ها', icon: BarChart3, requiresVehicle: true },
    { id: 'search', label: 'جستجو و فیلتر', icon: Search, requiresVehicle: false },
    { id: 'export', label: 'خروجی PDF / Excel', icon: FileSpreadsheet, requiresVehicle: true },
    { id: 'vehicles', label: 'خودروهای من', icon: CarFront, requiresVehicle: false },
    { id: 'backup', label: 'Backup / Restore', icon: Database, requiresVehicle: false },
    { id: 'settings', label: 'تنظیمات', icon: Settings, requiresVehicle: false },
  ];

  return (
    <>
      {/* Desktop & Tablet Sidebar */}
      <aside className="hidden lg:flex flex-col w-64 shrink-0 bg-white dark:bg-slate-900 border-l border-slate-200 dark:border-slate-800 p-4 h-[calc(100vh-65px)] sticky top-[65px] overflow-y-auto">
        <div className="text-xs font-bold text-slate-400 px-3 py-2 uppercase tracking-wider">
          بخش‌های اصلی
        </div>
        <nav className="space-y-1">
          {mainNavItems.map((item) => {
            const Icon = item.icon;
            const isActive = currentActiveTab === item.id;
            const isDisabled = item.requiresVehicle && !hasSelectedVehicle;

            return (
              <button
                key={item.id}
                disabled={isDisabled}
                onClick={() => handleTabChange(item.id as NavTab)}
                className={`w-full flex items-center justify-between px-3 py-2.5 rounded-xl text-sm font-medium transition-all ${
                  isDisabled
                    ? 'opacity-40 cursor-not-allowed text-slate-400'
                    : isActive
                    ? 'bg-cyan-50 dark:bg-cyan-950/50 text-cyan-600 dark:text-cyan-400 font-bold shadow-xs'
                    : 'text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800/60 hover:text-slate-900 dark:hover:text-slate-200'
                }`}
              >
                <div className="flex items-center gap-3">
                  <Icon className={`w-5 h-5 ${isActive ? 'text-cyan-600 dark:text-cyan-400' : 'text-slate-400'}`} />
                  <span>{item.label}</span>
                </div>
                {item.badge !== undefined && item.badge > 0 && (
                  <span className="bg-rose-500 text-white text-[11px] font-bold px-2 py-0.5 rounded-full">
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        <div className="mt-auto pt-4 border-t border-slate-200 dark:border-slate-800 text-xs text-slate-400 text-center">
          <div>سامانه هوشمند مدیریت خودرو</div>
          <div className="text-[11px] text-slate-500 mt-0.5">com.mycar.app • نسخه ۱.۰</div>
        </div>
      </aside>

      {/* Mobile Horizontal Quick Navigation Bar */}
      <div className="lg:hidden sticky top-[65px] z-30 bg-white/95 dark:bg-slate-900/95 backdrop-blur border-b border-slate-200 dark:border-slate-800 px-2 py-1.5 overflow-x-auto scrollbar-none flex items-center gap-1.5">
        {mainNavItems.map((item) => {
          const Icon = item.icon;
          const isActive = currentActiveTab === item.id;
          const isDisabled = item.requiresVehicle && !hasSelectedVehicle;

          return (
            <button
              key={item.id}
              disabled={isDisabled}
              onClick={() => handleTabChange(item.id as NavTab)}
              className={`shrink-0 flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold whitespace-nowrap transition ${
                isDisabled
                  ? 'opacity-40 cursor-not-allowed'
                  : isActive
                  ? 'bg-cyan-600 text-white shadow-xs'
                  : 'bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700'
              }`}
            >
              <Icon className="w-4 h-4" />
              <span>{item.label}</span>
              {item.badge !== undefined && item.badge > 0 && (
                <span className="w-4 h-4 rounded-full bg-rose-500 text-white text-[9px] flex items-center justify-center font-bold">
                  {item.badge}
                </span>
              )}
            </button>
          );
        })}
      </div>
    </>
  );
};
