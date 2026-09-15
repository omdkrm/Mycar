import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Vehicle,
  ServiceRecord,
  FuelRecord,
  VehicleMaintenanceSchedule,
  CatalogItem,
  MileageRecord,
  AppSettings,
  NavTab,
  ReminderItem,
} from './types';
import { StorageService } from './services/storage';
import { calculateMaintenanceReminders } from './utils/calculations';
import { Header } from './components/Header';
import { Navigation } from './components/Navigation';
import { VehiclesListView } from './components/VehiclesListView';
import { VehicleFormModal } from './components/VehicleFormModal';
import { MileageUpdateModal } from './components/MileageUpdateModal';
import { DashboardView } from './components/DashboardView';
import { ServiceRegisterView } from './components/ServiceRegisterView';
import { ServiceHistoryView } from './components/ServiceHistoryView';
import { PartHistoryView } from './components/PartHistoryView';
import { RemindersView } from './components/RemindersView';
import { FuelView } from './components/FuelView';
import { SchedulesView } from './components/SchedulesView';
import { ReportsView } from './components/ReportsView';
import { SettingsView } from './components/SettingsView';

export default function App() {
  // Load state from local-first storage
  const [vehicles, setVehicles] = useState<Vehicle[]>([]);
  const [serviceRecords, setServiceRecords] = useState<ServiceRecord[]>([]);
  const [fuelRecords, setFuelRecords] = useState<FuelRecord[]>([]);
  const [schedules, setSchedules] = useState<VehicleMaintenanceSchedule[]>([]);
  const [catalogItems, setCatalogItems] = useState<CatalogItem[]>([]);
  const [mileageHistory, setMileageHistory] = useState<MileageRecord[]>([]);
  const [settings, setSettings] = useState<AppSettings>(StorageService.getSettings());

  // Active view & selection state
  const [currentTab, setCurrentTab] = useState<NavTab>('vehicles');
  const [selectedVehicleId, setSelectedVehicleId] = useState<string | null>(null);

  // Modals & temporary states
  const [isVehicleModalOpen, setIsVehicleModalOpen] = useState(false);
  const [editingVehicle, setEditingVehicle] = useState<Vehicle | null>(null);

  const [isMileageModalOpen, setIsMileageModalOpen] = useState(false);

  const [editingServiceRecord, setEditingServiceRecord] = useState<ServiceRecord | null>(null);
  const [servicePrefillPart, setServicePrefillPart] = useState<{ name: string; catalogItemId?: string } | null>(null);

  // Confirmation modal for deletion
  const [deleteConfirm, setDeleteConfirm] = useState<{
    type: 'vehicle' | 'serviceRecord' | 'fuelRecord';
    id: string;
    title: string;
  } | null>(null);

  // Load all data
  const refreshData = useCallback(() => {
    const data = StorageService.loadAll();
    setVehicles(data.vehicles);
    setServiceRecords(data.serviceRecords);
    setFuelRecords(data.fuelRecords);
    setSchedules(data.schedules);
    setCatalogItems(data.catalogItems);
    setMileageHistory(data.mileageHistory);
    setSettings(data.settings);

    // Ensure selected vehicle is valid
    if (data.vehicles.length > 0) {
      setSelectedVehicleId((prev) => {
        if (prev && data.vehicles.some((v) => v.id === prev)) {
          return prev;
        }
        return data.vehicles[0].id;
      });
    } else {
      setSelectedVehicleId(null);
    }
  }, []);

  useEffect(() => {
    refreshData();
  }, [refreshData]);

  // Handle Theme
  useEffect(() => {
    if (settings.theme === 'dark') {
      document.documentElement.classList.add('dark');
    } else {
      document.documentElement.classList.remove('dark');
    }
  }, [settings.theme]);

  // Selected vehicle object
  const selectedVehicle = useMemo(() => {
    return vehicles.find((v) => v.id === selectedVehicleId) || vehicles[0] || null;
  }, [vehicles, selectedVehicleId]);

  // Calculate reminders for all vehicles
  const remindersByVehicle = useMemo(() => {
    const map: Record<string, ReminderItem[]> = {};
    vehicles.forEach((v) => {
      map[v.id] = calculateMaintenanceReminders(v, schedules, serviceRecords);
    });
    return map;
  }, [vehicles, schedules, serviceRecords]);

  const activeReminders = useMemo(() => {
    if (!selectedVehicle) return [];
    return remindersByVehicle[selectedVehicle.id] || [];
  }, [selectedVehicle, remindersByVehicle]);

  const totalOverdueCount = useMemo(() => {
    let count = 0;
    Object.values(remindersByVehicle).forEach((list) => {
      const items = list as ReminderItem[];
      count += items.filter((r) => r.status === 'overdue').length;
    });
    return count;
  }, [remindersByVehicle]);

  // Handlers for Vehicle
  const handleSaveVehicle = (data: Partial<Vehicle>, isCorrectionConfirmed?: boolean) => {
    try {
      if (data.id) {
        // Edit existing
        const existing = vehicles.find((v) => v.id === data.id);
        if (!existing) return { success: false, message: 'خودرو یافت نشد.' };

        const updated: Vehicle = {
          ...existing,
          ...data,
          updatedAt: Date.now(),
        } as Vehicle;

        // If mileage changed, record in history
        if (data.currentMileage !== undefined && data.currentMileage !== existing.currentMileage) {
          StorageService.addMileageRecord({
            vehicleId: updated.id,
            timestamp: Date.now(),
            mileage: data.currentMileage,
            notes: isCorrectionConfirmed ? 'اصلاح کیلومتر در ویرایش خودرو' : 'به‌روزرسانی در ویرایش خودرو',
          });
        }

        StorageService.saveVehicle(updated);
      } else {
        // Add new vehicle
        const newV: Vehicle = {
          id: 'v_' + Date.now(),
          name: data.name || 'خودروی من',
          brand: data.brand || '',
          model: data.model || '',
          year: data.year || 1402,
          trim: data.trim,
          engineDisplacement: data.engineDisplacement,
          licensePlate: data.licensePlate,
          vin: data.vin,
          currentMileage: data.currentMileage || 0,
          createdAt: Date.now(),
          updatedAt: Date.now(),
        };
        StorageService.saveVehicle(newV);

        // Record initial mileage
        StorageService.addMileageRecord({
          vehicleId: newV.id,
          timestamp: Date.now(),
          mileage: newV.currentMileage,
          notes: 'کیلومتر اولیه هنگام ثبت خودرو',
        });

        // Initialize default maintenance schedules for this new vehicle
        StorageService.initializeSchedulesForVehicle(newV.id);

        setSelectedVehicleId(newV.id);
      }

      refreshData();
      return { success: true };
    } catch (e: any) {
      return { success: false, message: e.message };
    }
  };

  const handleDeleteVehicle = (id: string, name: string) => {
    setDeleteConfirm({
      type: 'vehicle',
      id,
      title: `خودرو «${name}» و تمامی سوابق، سوخت‌ها و یادآوری‌های مربوط به آن`,
    });
  };

  // Handlers for Mileage Update
  const handleSaveMileage = (newMileage: number, notes?: string, isCorrection?: boolean) => {
    if (!selectedVehicle) return { success: false, message: 'هیچ خودرویی انتخاب نشده است.' };

    try {
      // Update vehicle current mileage
      const updated: Vehicle = {
        ...selectedVehicle,
        currentMileage: newMileage,
        updatedAt: Date.now(),
      };
      StorageService.saveVehicle(updated);

      // Add to history
      StorageService.addMileageRecord({
        vehicleId: selectedVehicle.id,
        timestamp: Date.now(),
        mileage: newMileage,
        notes: notes || (isCorrection ? 'اصلاح کیلومتر' : 'به‌روزرسانی کیلومتر'),
      });

      refreshData();
      return { success: true };
    } catch (e: any) {
      return { success: false, message: e.message };
    }
  };

  // Handlers for Service Records
  const handleSaveServiceRecord = (recordData: Partial<ServiceRecord>) => {
    try {
      if (recordData.id) {
        // Edit
        const existing = serviceRecords.find((r) => r.id === recordData.id);
        if (!existing) return { success: false, message: 'سابقه یافت نشد.' };
        const updated = { ...existing, ...recordData } as ServiceRecord;
        StorageService.saveServiceRecord(updated);
      } else {
        // New
        const newRecord: ServiceRecord = {
          id: 'sr_' + Date.now(),
          vehicleId: recordData.vehicleId || selectedVehicle?.id || '',
          catalogItemId: recordData.catalogItemId,
          partName: recordData.partName || '',
          category: recordData.category || 'موتور',
          operationType: recordData.operationType || 'تعویض',
          dateTimestamp: recordData.dateTimestamp || Date.now(),
          mileage: recordData.mileage || 0,
          partPrice: recordData.partPrice || 0,
          laborCost: recordData.laborCost || 0,
          totalCost: (recordData.partPrice || 0) + (recordData.laborCost || 0),
          repairShop: recordData.repairShop,
          description: recordData.description,
          invoicePhotoUrl: recordData.invoicePhotoUrl,
          nextMileage: recordData.nextMileage,
          nextDueDateTimestamp: recordData.nextDueDateTimestamp,
          createdAt: Date.now(),
        };

        // If service mileage is greater than current vehicle mileage, update vehicle mileage
        const targetVehicle = vehicles.find((v) => v.id === newRecord.vehicleId);
        if (targetVehicle && newRecord.mileage > targetVehicle.currentMileage) {
          StorageService.saveVehicle({
            ...targetVehicle,
            currentMileage: newRecord.mileage,
            updatedAt: Date.now(),
          });
          StorageService.addMileageRecord({
            vehicleId: targetVehicle.id,
            timestamp: newRecord.dateTimestamp,
            mileage: newRecord.mileage,
            notes: `ثبت کیلومتر از سرویس «${newRecord.partName}»`,
          });
        }

        StorageService.saveServiceRecord(newRecord);
      }

      refreshData();
      return { success: true };
    } catch (e: any) {
      return { success: false, message: e.message };
    }
  };

  const handleDeleteServiceRecord = (id: string, name: string) => {
    setDeleteConfirm({
      type: 'serviceRecord',
      id,
      title: `سابقه سرویس «${name}»`,
    });
  };

  // Handlers for Fuel Records
  const handleSaveFuelRecord = (recordData: Partial<FuelRecord>) => {
    try {
      if (recordData.id) {
        const existing = fuelRecords.find((r) => r.id === recordData.id);
        if (!existing) return { success: false, message: 'رکورد سوخت یافت نشد.' };
        const updated = { ...existing, ...recordData } as FuelRecord;
        StorageService.saveFuelRecord(updated);
      } else {
        const newRec: FuelRecord = {
          id: 'fuel_' + Date.now(),
          vehicleId: recordData.vehicleId || selectedVehicle?.id || '',
          dateTimestamp: recordData.dateTimestamp || Date.now(),
          mileage: recordData.mileage || 0,
          liters: recordData.liters || 0,
          pricePerLiter: recordData.pricePerLiter || 3000,
          totalCost: recordData.totalCost || 0,
          fuelType: recordData.fuelType || 'معمولی',
          isFullTank: recordData.isFullTank ?? true,
          gasStation: recordData.gasStation,
          notes: recordData.notes,
          createdAt: Date.now(),
        };

        // If fuel mileage is greater than current vehicle mileage, update vehicle mileage
        const targetVehicle = vehicles.find((v) => v.id === newRec.vehicleId);
        if (targetVehicle && newRec.mileage > targetVehicle.currentMileage) {
          StorageService.saveVehicle({
            ...targetVehicle,
            currentMileage: newRec.mileage,
            updatedAt: Date.now(),
          });
          StorageService.addMileageRecord({
            vehicleId: targetVehicle.id,
            timestamp: newRec.dateTimestamp,
            mileage: newRec.mileage,
            notes: `ثبت کیلومتر هنگام سوخت‌گیری`,
          });
        }

        StorageService.saveFuelRecord(newRec);
      }

      refreshData();
      return { success: true };
    } catch (e: any) {
      return { success: false, message: e.message };
    }
  };

  const handleDeleteFuelRecord = (id: string) => {
    setDeleteConfirm({
      type: 'fuelRecord',
      id,
      title: `سابقه سوخت‌گیری انتخاب‌شده`,
    });
  };

  // Handlers for Schedules
  const handleSaveSchedule = (scheduleData: Partial<VehicleMaintenanceSchedule>) => {
    try {
      if (scheduleData.id) {
        const existing = schedules.find((s) => s.id === scheduleData.id);
        if (!existing) return { success: false, message: 'دوره سرویس یافت نشد.' };
        const updated = { ...existing, ...scheduleData } as VehicleMaintenanceSchedule;
        StorageService.saveSchedule(updated);
      } else {
        const newSched: VehicleMaintenanceSchedule = {
          id: 'sched_' + Date.now(),
          vehicleId: scheduleData.vehicleId || selectedVehicle?.id || '',
          partName: scheduleData.partName || '',
          category: scheduleData.category || 'موتور',
          kmInterval: scheduleData.kmInterval || 8000,
          timeIntervalMonths: scheduleData.timeIntervalMonths || 6,
          description: scheduleData.description,
          isEnabled: true,
        };
        StorageService.saveSchedule(newSched);
      }

      refreshData();
      return { success: true };
    } catch (e: any) {
      return { success: false, message: e.message };
    }
  };

  const handleToggleSchedule = (id: string, isEnabled: boolean) => {
    const existing = schedules.find((s) => s.id === id);
    if (existing) {
      StorageService.saveSchedule({ ...existing, isEnabled });
      refreshData();
    }
  };

  const handleDeleteSchedule = (id: string) => {
    StorageService.deleteSchedule(id);
    refreshData();
  };

  // Execute confirmed deletion
  const executeDelete = () => {
    if (!deleteConfirm) return;
    if (deleteConfirm.type === 'vehicle') {
      StorageService.deleteVehicle(deleteConfirm.id);
      if (selectedVehicleId === deleteConfirm.id) {
        const remaining = vehicles.filter((v) => v.id !== deleteConfirm.id);
        setSelectedVehicleId(remaining.length > 0 ? remaining[0].id : null);
      }
    } else if (deleteConfirm.type === 'serviceRecord') {
      StorageService.deleteServiceRecord(deleteConfirm.id);
    } else if (deleteConfirm.type === 'fuelRecord') {
      StorageService.deleteFuelRecord(deleteConfirm.id);
    }
    setDeleteConfirm(null);
    refreshData();
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 text-slate-900 dark:text-slate-100 flex flex-col font-sans antialiased selection:bg-cyan-500 selection:text-white">
      {/* App Header */}
      <Header
        vehicles={vehicles}
        selectedVehicleId={selectedVehicleId}
        onSelectVehicle={(id) => {
          setSelectedVehicleId(id);
          // If on vehicles list, go to dashboard
          if (currentTab === 'vehicles') {
            setCurrentTab('dashboard');
          }
        }}
        onAddVehicle={() => {
          setEditingVehicle(null);
          setIsVehicleModalOpen(true);
        }}
        currentTab={currentTab}
        onNavigate={setCurrentTab}
        totalOverdueReminders={totalOverdueCount}
      />

      {/* Main Layout Body */}
      <div className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-6 lg:px-8 py-6 flex flex-col md:flex-row gap-6">
        {/* Navigation Sidebar for desktop */}
        <Navigation
          currentTab={currentTab}
          activeTab={currentTab}
          onSelectTab={(tab) => {
            setCurrentTab(tab);
            setEditingServiceRecord(null);
            setServicePrefillPart(null);
          }}
          onTabChange={(tab) => {
            setCurrentTab(tab);
            setEditingServiceRecord(null);
            setServicePrefillPart(null);
          }}
          overdueCount={totalOverdueCount}
          hasSelectedVehicle={!!selectedVehicle}
        />

        {/* Dynamic View Content */}
        <main className="flex-1 min-w-0 pb-20 md:pb-6">
          {/* Tab 1: Vehicles List */}
          {currentTab === 'vehicles' && (
            <VehiclesListView
              vehicles={vehicles}
              selectedVehicleId={selectedVehicleId}
              onSelectVehicle={setSelectedVehicleId}
              onOpenDashboard={(id) => {
                setSelectedVehicleId(id);
                setCurrentTab('dashboard');
              }}
              onAddVehicle={() => {
                setEditingVehicle(null);
                setIsVehicleModalOpen(true);
              }}
              onEditVehicle={(veh) => {
                setEditingVehicle(veh);
                setIsVehicleModalOpen(true);
              }}
              onDeleteVehicle={handleDeleteVehicle}
              remindersByVehicle={remindersByVehicle}
            />
          )}

          {/* Tab 2: Dashboard (Requires vehicle selected) */}
          {currentTab === 'dashboard' && selectedVehicle && (
            <DashboardView
              vehicle={selectedVehicle}
              reminders={activeReminders}
              serviceRecords={serviceRecords}
              fuelRecords={fuelRecords}
              onOpenMileageModal={() => setIsMileageModalOpen(true)}
              onOpenRegisterService={() => {
                setEditingServiceRecord(null);
                setServicePrefillPart(null);
                setCurrentTab('register-service');
              }}
              onOpenRegisterFuel={() => setCurrentTab('fuel')}
              onNavigateTab={setCurrentTab}
              onCompleteReminder={(rem) => {
                setServicePrefillPart({ name: rem.partName, catalogItemId: rem.catalogItemId });
                setEditingServiceRecord(null);
                setCurrentTab('register-service');
              }}
            />
          )}

          {/* Tab 3: Register Service / Part Replacement */}
          {currentTab === 'register-service' && selectedVehicle && (
            <ServiceRegisterView
              vehicles={vehicles}
              selectedVehicleId={selectedVehicle.id}
              catalogItems={catalogItems}
              schedules={schedules}
              initialRecord={editingServiceRecord}
              prefillPartName={servicePrefillPart?.name}
              prefillCatalogItemId={servicePrefillPart?.catalogItemId}
              onSaveRecord={handleSaveServiceRecord}
              onCancel={() => {
                setEditingServiceRecord(null);
                setServicePrefillPart(null);
                setCurrentTab('service-history');
              }}
            />
          )}

          {/* Tab 4: Full Service History */}
          {currentTab === 'service-history' && selectedVehicle && (
            <ServiceHistoryView
              vehicle={selectedVehicle}
              serviceRecords={serviceRecords}
              onEditRecord={(rec) => {
                setEditingServiceRecord(rec);
                setCurrentTab('register-service');
              }}
              onDeleteRecord={handleDeleteServiceRecord}
              onAddNewService={() => {
                setEditingServiceRecord(null);
                setServicePrefillPart(null);
                setCurrentTab('register-service');
              }}
            />
          )}

          {/* Tab 5: Part History Timeline */}
          {currentTab === 'part-history' && selectedVehicle && (
            <PartHistoryView
              vehicle={selectedVehicle}
              catalogItems={catalogItems}
              schedules={schedules}
              serviceRecords={serviceRecords}
              onAddNewServiceForPart={(name, catId) => {
                setServicePrefillPart({ name, catalogItemId: catId });
                setEditingServiceRecord(null);
                setCurrentTab('register-service');
              }}
            />
          )}

          {/* Tab 6: Reminders View */}
          {currentTab === 'reminders' && selectedVehicle && (
            <RemindersView
              vehicle={selectedVehicle}
              reminders={activeReminders}
              onCompleteService={(rec) => {
                const res = handleSaveServiceRecord(rec);
                return res;
              }}
            />
          )}

          {/* Tab 7: Fuel View */}
          {currentTab === 'fuel' && selectedVehicle && (
            <FuelView
              vehicle={selectedVehicle}
              fuelRecords={fuelRecords}
              onSaveFuelRecord={handleSaveFuelRecord}
              onDeleteFuelRecord={handleDeleteFuelRecord}
            />
          )}

          {/* Tab 8: Schedules & Catalog View */}
          {currentTab === 'schedules' && selectedVehicle && (
            <SchedulesView
              vehicle={selectedVehicle}
              schedules={schedules}
              catalogItems={catalogItems}
              onSaveSchedule={handleSaveSchedule}
              onToggleSchedule={handleToggleSchedule}
              onDeleteSchedule={handleDeleteSchedule}
            />
          )}

          {/* Tab 9: Reports & Export View */}
          {currentTab === 'reports' && selectedVehicle && (
            <ReportsView
              vehicle={selectedVehicle}
              serviceRecords={serviceRecords}
              fuelRecords={fuelRecords}
            />
          )}

          {/* Tab 10: Settings, Backup & Restore View */}
          {currentTab === 'settings' && (
            <SettingsView
              settings={settings}
              onUpdateSettings={(newSettings) => {
                StorageService.saveSettings(newSettings);
                refreshData();
              }}
              onDataRestored={refreshData}
            />
          )}

          {/* Fallback if a vehicle-specific tab is clicked without a vehicle */}
          {!selectedVehicle && currentTab !== 'vehicles' && currentTab !== 'settings' && (
            <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center space-y-4 shadow-xs">
              <h3 className="text-lg font-bold text-slate-800 dark:text-slate-200">
                لطفاً ابتدا یک خودرو اضافه یا انتخاب نمایید.
              </h3>
              <p className="text-xs text-slate-500 max-w-sm mx-auto">
                برای دسترسی به داشبورد، سرویس‌ها و هزینه‌ها نیاز به وجود حداقل یک خودرو در سامانه است.
              </p>
              <button
                onClick={() => {
                  setEditingVehicle(null);
                  setIsVehicleModalOpen(true);
                }}
                className="inline-flex items-center gap-2 bg-cyan-600 hover:bg-cyan-700 text-white font-bold px-5 py-2.5 rounded-xl text-xs sm:text-sm shadow-md transition"
              >
                + افزودن خودرو جدید
              </button>
            </div>
          )}
        </main>
      </div>

      {/* Modal: Add/Edit Vehicle */}
      <VehicleFormModal
        isOpen={isVehicleModalOpen}
        onClose={() => setIsVehicleModalOpen(false)}
        initialVehicle={editingVehicle}
        latestKnownMileage={editingVehicle ? editingVehicle.currentMileage : 0}
        onSave={handleSaveVehicle}
      />

      {/* Modal: Update Mileage */}
      {selectedVehicle && (
        <MileageUpdateModal
          isOpen={isMileageModalOpen}
          onClose={() => setIsMileageModalOpen(false)}
          vehicle={selectedVehicle}
          mileageHistory={mileageHistory}
          onSaveMileage={handleSaveMileage}
        />
      )}

      {/* Confirmation Modal */}
      {deleteConfirm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white dark:bg-slate-900 w-full max-w-md rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 p-6 space-y-4">
            <h3 className="text-base font-extrabold text-slate-900 dark:text-slate-100">
              تأیید حذف
            </h3>
            <p className="text-xs sm:text-sm text-slate-600 dark:text-slate-300 leading-relaxed">
              آیا از حذف <strong>{deleteConfirm.title}</strong> اطمینان دارید؟ این عملیات غیرقابل بازگشت است.
            </p>
            <div className="flex justify-end gap-2 pt-2">
              <button
                type="button"
                onClick={() => setDeleteConfirm(null)}
                className="px-4 py-2 rounded-xl text-xs font-bold text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
              >
                انصراف
              </button>
              <button
                type="button"
                onClick={executeDelete}
                className="px-4 py-2 rounded-xl text-xs font-bold bg-rose-600 hover:bg-rose-700 text-white shadow-sm"
              >
                حذف قطعی
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
