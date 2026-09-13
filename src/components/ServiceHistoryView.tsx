import React, { useState, useMemo } from 'react';
import {
  History,
  Search,
  Filter,
  Trash2,
  Edit3,
  Calendar,
  Gauge,
  Eye,
  X,
  FileText,
  AlertTriangle,
  ArrowUpDown,
} from 'lucide-react';
import { ServiceRecord, Vehicle, OperationType, PartCategory } from '../types';
import { formatCurrency, formatMileage, formatWithCommas } from '../utils/formatters';
import { formatToJalali } from '../utils/persianDate';

interface ServiceHistoryViewProps {
  vehicle: Vehicle;
  serviceRecords: ServiceRecord[];
  onEditRecord: (record: ServiceRecord) => void;
  onDeleteRecord: (id: string, name: string) => void;
  onAddNewService: () => void;
}

export const ServiceHistoryView: React.FC<ServiceHistoryViewProps> = ({
  vehicle,
  serviceRecords,
  onEditRecord,
  onDeleteRecord,
  onAddNewService,
}) => {
  const [searchTerm, setSearchTerm] = useState('');
  const [selectedCategory, setSelectedCategory] = useState<string>('all');
  const [selectedOperation, setSelectedOperation] = useState<string>('all');
  const [sortField, setSortField] = useState<'date' | 'mileage' | 'cost'>('date');
  const [sortOrder, setSortOrder] = useState<'desc' | 'asc'>('desc');

  // Modal for full details
  const [viewingRecord, setViewingRecord] = useState<ServiceRecord | null>(null);

  // Filter and sort records for this vehicle
  const filteredRecords = useMemo(() => {
    return serviceRecords
      .filter((r) => r.vehicleId === vehicle.id)
      .filter((r) => {
        if (!searchTerm.trim()) return true;
        const term = searchTerm.toLowerCase();
        return (
          r.partName.toLowerCase().includes(term) ||
          (r.repairShop && r.repairShop.toLowerCase().includes(term)) ||
          (r.description && r.description.toLowerCase().includes(term)) ||
          formatToJalali(r.dateTimestamp).includes(term)
        );
      })
      .filter((r) => {
        if (selectedCategory === 'all') return true;
        return r.category === selectedCategory;
      })
      .filter((r) => {
        if (selectedOperation === 'all') return true;
        return r.operationType === selectedOperation;
      })
      .sort((a, b) => {
        let diff = 0;
        if (sortField === 'date') diff = a.dateTimestamp - b.dateTimestamp;
        else if (sortField === 'mileage') diff = a.mileage - b.mileage;
        else if (sortField === 'cost') diff = a.totalCost - b.totalCost;
        return sortOrder === 'desc' ? -diff : diff;
      });
  }, [serviceRecords, vehicle.id, searchTerm, selectedCategory, selectedOperation, sortField, sortOrder]);

  const totalSpentInView = filteredRecords.reduce((sum, r) => sum + r.totalCost, 0);

  return (
    <div className="space-y-6">
      {/* Title & Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-black text-slate-900 dark:text-slate-100 flex items-center gap-2">
            <History className="w-7 h-7 text-cyan-600 dark:text-cyan-400" />
            <span>تاریخچه سرویس‌ها - {vehicle.name}</span>
          </h1>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            مشاهده، جستجو، فیلتر و ویرایش تمامی سوابق تعمیراتی و سرویس‌های ثبت شده
          </p>
        </div>

        <button
          onClick={onAddNewService}
          className="self-start sm:self-auto bg-cyan-600 hover:bg-cyan-700 text-white font-bold text-xs sm:text-sm px-4 py-2.5 rounded-xl shadow-sm transition"
        >
          + ثبت سرویس جدید
        </button>
      </div>

      {/* Filter and Search Bar */}
      <div className="bg-white dark:bg-slate-900 p-4 rounded-2xl border border-slate-200 dark:border-slate-800 shadow-xs space-y-3">
        <div className="flex flex-col md:flex-row items-center gap-3">
          {/* Search input */}
          <div className="relative flex-1 w-full">
            <Search className="w-4 h-4 text-slate-400 absolute right-3 top-3" />
            <input
              type="text"
              placeholder="جستجو در قطعه، تعمیرگاه یا توضیحات..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              className="w-full pr-9 pl-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
            />
            {searchTerm && (
              <button
                onClick={() => setSearchTerm('')}
                className="absolute left-3 top-2.5 text-slate-400 hover:text-slate-600"
              >
                <X className="w-4 h-4" />
              </button>
            )}
          </div>

          {/* Category Filter */}
          <select
            value={selectedCategory}
            onChange={(e) => setSelectedCategory(e.target.value)}
            className="w-full md:w-44 px-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            <option value="all">همه دسته‌ها</option>
            <option value="موتور">موتور</option>
            <option value="ترمز">ترمز</option>
            <option value="جلوبندی و تعلیق">جلوبندی و تعلیق</option>
            <option value="سرویس‌های دوره‌ای">سرویس‌های دوره‌ای</option>
            <option value="سایر">سایر</option>
          </select>

          {/* Operation Filter */}
          <select
            value={selectedOperation}
            onChange={(e) => setSelectedOperation(e.target.value)}
            className="w-full md:w-36 px-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 focus:outline-none focus:ring-2 focus:ring-cyan-500"
          >
            <option value="all">همه عملیات‌ها</option>
            <option value="تعویض">تعویض</option>
            <option value="سرویس">سرویس</option>
            <option value="تعمیر">تعمیر</option>
          </select>

          {/* Sort Dropdown */}
          <div className="flex items-center gap-1.5 w-full md:w-auto">
            <select
              value={sortField}
              onChange={(e) => setSortField(e.target.value as any)}
              className="px-3 py-2 text-xs sm:text-sm rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800"
            >
              <option value="date">بر اساس تاریخ</option>
              <option value="mileage">بر اساس کیلومتر</option>
              <option value="cost">بر اساس هزینه</option>
            </select>
            <button
              type="button"
              onClick={() => setSortOrder(sortOrder === 'desc' ? 'asc' : 'desc')}
              className="p-2 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-600 dark:text-slate-300 hover:bg-slate-100"
              title="تغییر ترتیب صعودی/نزولی"
            >
              <ArrowUpDown className="w-4 h-4" />
            </button>
          </div>
        </div>

        {/* Summary row */}
        <div className="flex items-center justify-between text-xs text-slate-500 dark:text-slate-400 pt-2 border-t border-slate-100 dark:border-slate-800">
          <span>
            تعداد سوابق پیدا شده: <strong className="text-slate-800 dark:text-slate-200">{filteredRecords.length}</strong>
          </span>
          <span>
            مجموع هزینه این فیلتر: <strong className="text-cyan-600 dark:text-cyan-400 font-bold">{formatCurrency(totalSpentInView)}</strong>
          </span>
        </div>
      </div>

      {/* Records List / Cards */}
      {filteredRecords.length === 0 ? (
        <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-2xl p-12 text-center space-y-3">
          <History className="w-12 h-12 text-slate-300 mx-auto" />
          <div className="text-base font-bold text-slate-700 dark:text-slate-300">
            هنوز سابقه سرویس ثبت نشده است.
          </div>
          <p className="text-xs text-slate-400 max-w-sm mx-auto">
            سابقه سرویسی با فیلترهای انتخابی شما یافت نشد یا هنوز تعویض روغنی یا تعمیری برای این خودرو ثبت نکرده‌اید.
          </p>
          <button
            onClick={onAddNewService}
            className="inline-flex items-center gap-1.5 bg-cyan-600 text-white text-xs font-bold px-4 py-2 rounded-xl mt-2"
          >
            + ثبت اولین سرویس
          </button>
        </div>
      ) : (
        <div className="space-y-3">
          {filteredRecords.map((record) => (
            <div
              key={record.id}
              className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-800 p-4 sm:p-5 hover:border-cyan-300 dark:hover:border-cyan-800 transition shadow-xs flex flex-col sm:flex-row sm:items-center justify-between gap-4"
            >
              {/* Left Details */}
              <div className="space-y-1.5 flex-1">
                <div className="flex flex-wrap items-center gap-2">
                  <span className="font-extrabold text-sm sm:text-base text-slate-900 dark:text-slate-100">
                    {record.partName}
                  </span>
                  <span className="text-xs font-bold text-cyan-700 dark:text-cyan-300 bg-cyan-50 dark:bg-cyan-950/60 px-2 py-0.5 rounded-md">
                    {record.operationType}
                  </span>
                  <span className="text-[11px] text-slate-400 bg-slate-100 dark:bg-slate-800 px-2 py-0.5 rounded-md">
                    {record.category}
                  </span>
                </div>

                <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-xs text-slate-500 dark:text-slate-400 font-medium">
                  <div className="flex items-center gap-1">
                    <Calendar className="w-3.5 h-3.5 text-slate-400" />
                    <span>{formatToJalali(record.dateTimestamp)}</span>
                  </div>
                  <div className="flex items-center gap-1">
                    <Gauge className="w-3.5 h-3.5 text-cyan-500" />
                    <span className="font-mono font-bold text-slate-700 dark:text-slate-300">
                      {formatMileage(record.mileage)}
                    </span>
                  </div>
                  {record.repairShop && (
                    <div className="text-slate-500 truncate max-w-xs">
                      تعمیرگاه: {record.repairShop}
                    </div>
                  )}
                </div>

                {record.description && (
                  <p className="text-xs text-slate-600 dark:text-slate-300 line-clamp-1 pt-1">
                    {record.description}
                  </p>
                )}
              </div>

              {/* Right Price & Actions */}
              <div className="flex sm:flex-col items-center sm:items-end justify-between border-t sm:border-t-0 pt-3 sm:pt-0 border-slate-100 dark:border-slate-800 gap-2 shrink-0">
                <div className="text-left">
                  <div className="text-sm sm:text-base font-black text-slate-900 dark:text-slate-100">
                    {formatCurrency(record.totalCost)}
                  </div>
                  <div className="text-[10px] text-slate-400">
                    قطعه: {formatWithCommas(record.partPrice)} • اجرت: {formatWithCommas(record.laborCost)}
                  </div>
                </div>

                <div className="flex items-center gap-1">
                  <button
                    onClick={() => setViewingRecord(record)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-cyan-600 hover:bg-cyan-50 dark:hover:bg-cyan-950/40 transition"
                    title="مشاهده جزئیات کامل و فاکتور"
                  >
                    <Eye className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => onEditRecord(record)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-slate-700 dark:hover:text-slate-200 hover:bg-slate-100 dark:hover:bg-slate-800 transition"
                    title="ویرایش سابقه"
                  >
                    <Edit3 className="w-4 h-4" />
                  </button>
                  <button
                    onClick={() => onDeleteRecord(record.id, record.partName)}
                    className="p-1.5 rounded-lg text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition"
                    title="حذف سابقه"
                  >
                    <Trash2 className="w-4 h-4" />
                  </button>
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Details View Modal */}
      {viewingRecord && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-xs animate-in fade-in">
          <div className="bg-white dark:bg-slate-900 w-full max-w-lg rounded-2xl shadow-2xl border border-slate-200 dark:border-slate-800 overflow-hidden flex flex-col max-h-[90vh]">
            <div className="flex items-center justify-between px-6 py-4 border-b border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-800/50">
              <div className="flex items-center gap-2 font-bold text-slate-900 dark:text-slate-100">
                <FileText className="w-5 h-5 text-cyan-600" />
                <span>جزئیات سرویس: {viewingRecord.partName}</span>
              </div>
              <button
                onClick={() => setViewingRecord(null)}
                className="p-1 text-slate-400 hover:text-slate-600"
              >
                <X className="w-5 h-5" />
              </button>
            </div>

            <div className="p-6 space-y-4 overflow-y-auto">
              <div className="grid grid-cols-2 gap-3 text-xs">
                <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
                  <span className="text-slate-400 block mb-1">تاریخ انجام:</span>
                  <strong className="text-slate-800 dark:text-slate-200 text-sm">
                    {formatToJalali(viewingRecord.dateTimestamp, true)}
                  </strong>
                </div>
                <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
                  <span className="text-slate-400 block mb-1">کیلومتر ثبت شده:</span>
                  <strong className="text-cyan-600 dark:text-cyan-400 text-sm font-mono">
                    {formatMileage(viewingRecord.mileage)}
                  </strong>
                </div>
                <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
                  <span className="text-slate-400 block mb-1">نوع عملیات:</span>
                  <strong className="text-slate-800 dark:text-slate-200">
                    {viewingRecord.operationType} ({viewingRecord.category})
                  </strong>
                </div>
                <div className="p-3 bg-slate-50 dark:bg-slate-800/60 rounded-xl">
                  <span className="text-slate-400 block mb-1">تعمیرگاه / سرویس‌کار:</span>
                  <strong className="text-slate-800 dark:text-slate-200">
                    {viewingRecord.repairShop || 'ثبت نشده'}
                  </strong>
                </div>
              </div>

              {/* Price Breakdown */}
              <div className="p-4 bg-cyan-50/70 dark:bg-cyan-950/40 rounded-xl border border-cyan-200 dark:border-cyan-900 text-xs space-y-2">
                <div className="flex justify-between">
                  <span className="text-slate-600 dark:text-slate-400">قیمت قطعه:</span>
                  <span className="font-bold">{formatCurrency(viewingRecord.partPrice)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-600 dark:text-slate-400">اجرت و دستمزد:</span>
                  <span className="font-bold">{formatCurrency(viewingRecord.laborCost)}</span>
                </div>
                <div className="flex justify-between pt-2 border-t border-cyan-200 dark:border-cyan-800 text-sm font-black text-cyan-800 dark:text-cyan-200">
                  <span>مجموع هزینه:</span>
                  <span>{formatCurrency(viewingRecord.totalCost)}</span>
                </div>
              </div>

              {/* Next targets */}
              {(viewingRecord.nextMileage || viewingRecord.nextDueDateTimestamp) && (
                <div className="p-3 bg-emerald-50 dark:bg-emerald-950/40 rounded-xl border border-emerald-200 dark:border-emerald-900 text-xs flex justify-between">
                  <div>
                    <span className="text-slate-500 block">کیلومتر بعدی:</span>
                    <strong className="text-emerald-700 dark:text-emerald-400 font-mono">
                      {viewingRecord.nextMileage ? formatMileage(viewingRecord.nextMileage) : '—'}
                    </strong>
                  </div>
                  <div>
                    <span className="text-slate-500 block">تاریخ بعدی:</span>
                    <strong className="text-emerald-700 dark:text-emerald-400 font-mono">
                      {viewingRecord.nextDueDateTimestamp
                        ? formatToJalali(viewingRecord.nextDueDateTimestamp)
                        : '—'}
                    </strong>
                  </div>
                </div>
              )}

              {/* Description */}
              {viewingRecord.description && (
                <div>
                  <span className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    توضیحات:
                  </span>
                  <p className="text-xs text-slate-600 dark:text-slate-300 bg-slate-50 dark:bg-slate-800 p-3 rounded-xl border border-slate-200 dark:border-slate-700">
                    {viewingRecord.description}
                  </p>
                </div>
              )}

              {/* Invoice photo */}
              {viewingRecord.invoicePhotoUrl && (
                <div>
                  <span className="text-xs font-bold text-slate-700 dark:text-slate-300 block mb-1">
                    تصویر فاکتور / قطعه:
                  </span>
                  <img
                    src={viewingRecord.invoicePhotoUrl}
                    alt="فاکتور سرویس"
                    className="w-full max-h-60 object-contain rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-100 dark:bg-slate-800"
                  />
                </div>
              )}
            </div>

            <div className="p-4 border-t border-slate-200 dark:border-slate-800 flex justify-end gap-2">
              <button
                type="button"
                onClick={() => {
                  const r = viewingRecord;
                  setViewingRecord(null);
                  onEditRecord(r);
                }}
                className="px-4 py-2 text-xs font-bold rounded-xl bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-200 hover:bg-slate-200"
              >
                ویرایش
              </button>
              <button
                type="button"
                onClick={() => setViewingRecord(null)}
                className="px-4 py-2 text-xs font-bold rounded-xl bg-cyan-600 text-white hover:bg-cyan-700"
              >
                بستن
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
