import * as XLSX from 'xlsx';
import { Vehicle, ServiceRecord, FuelRecord, MileageRecord } from '../types';
import { formatToJalali } from './persianDate';
import { formatCurrency, formatWithCommas, formatYear } from './formatters';

export function exportToExcel(
  vehicle: Vehicle,
  services: ServiceRecord[],
  fuelRecords: FuelRecord[],
  mileageRecords: MileageRecord[] = []
) {
  const wb = XLSX.utils.book_new();

  // 1. Vehicle Info Sheet
  const vehicleInfo = [
    ['مشخصات خودرو', ''],
    ['نام خودرو', vehicle.name],
    ['برند', vehicle.brand],
    ['مدل', vehicle.model],
    ['سال ساخت', formatYear(vehicle.year)],
    ['تیپ', vehicle.trim || '—'],
    ['حجم موتور', vehicle.engineDisplacement || '—'],
    ['شماره پلاک', vehicle.licensePlate || '—'],
    ['شماره شاسی (VIN)', vehicle.vin || '—'],
    ['کیلومتر فعلی', formatWithCommas(vehicle.currentMileage)],
    ['تاریخ گزارش', formatToJalali(Date.now(), true)],
  ];
  const wsVehicle = XLSX.utils.aoa_to_sheet(vehicleInfo);
  XLSX.utils.book_append_sheet(wb, wsVehicle, 'مشخصات خودرو');

  // 2. Services Sheet
  const servicesData = [
    [
      'ردیف',
      'تاریخ (شمسی)',
      'کیلومتر',
      'قطعه / سرویس',
      'دسته‌بندی',
      'نوع عملیات',
      'قیمت قطعه (تومان)',
      'اجرت (تومان)',
      'مجموع هزینه (تومان)',
      'تعمیرگاه / سرویس‌کار',
      'توضیحات',
    ],
    ...services
      .sort((a, b) => b.mileage - a.mileage)
      .map((s, idx) => [
        idx + 1,
        formatToJalali(s.dateTimestamp),
        s.mileage,
        s.partName,
        s.category,
        s.operationType,
        s.partPrice,
        s.laborCost,
        s.totalCost,
        s.repairShop || '—',
        s.description || '—',
      ]),
  ];
  const wsServices = XLSX.utils.aoa_to_sheet(servicesData);
  XLSX.utils.book_append_sheet(wb, wsServices, 'سوابق سرویس');

  // 3. Fuel Records Sheet
  const fuelData = [
    [
      'ردیف',
      'تاریخ (شمسی)',
      'کیلومتر',
      'مقدار سوخت (لیتر)',
      'نوع بنزین',
      'هزینه کل (تومان)',
      'قیمت هر لیتر (تومان)',
      'جایگاه / یادداشت',
    ],
    ...fuelRecords
      .sort((a, b) => b.mileage - a.mileage)
      .map((f, idx) => [
        idx + 1,
        formatToJalali(f.dateTimestamp),
        f.mileage,
        f.liters,
        f.fuelType || 'معمولی',
        f.totalCost,
        f.pricePerLiter || '—',
        f.gasStation || f.notes || '—',
      ]),
  ];
  const wsFuel = XLSX.utils.aoa_to_sheet(fuelData);
  XLSX.utils.book_append_sheet(wb, wsFuel, 'سوابق سوخت‌گیری');

  // 4. Mileage History Sheet
  if (mileageRecords && mileageRecords.length > 0) {
    const mileageData = [
      ['ردیف', 'تاریخ ثبت (شمسی)', 'کیلومتر ثبت شده', 'توضیحات'],
      ...mileageRecords
        .sort((a, b) => b.timestamp - a.timestamp)
        .map((m, idx) => [
          idx + 1,
          formatToJalali(m.timestamp),
          m.mileage,
          m.notes || '—',
        ]),
    ];
    const wsMileage = XLSX.utils.aoa_to_sheet(mileageData);
    XLSX.utils.book_append_sheet(wb, wsMileage, 'تاریخچه کیلومتر');
  }

  // Trigger download
  const fileName = `گزارش_${vehicle.name.replace(/\s+/g, '_')}_${formatToJalali(Date.now())}.xlsx`;
  XLSX.writeFile(wb, fileName);
}

// Alias for convenience
export const exportToExcelCSV = exportToExcel;

/**
 * Generates an accessible, beautifully styled printable report for PDF saving
 */
export function printPdfReport(
  vehicle: Vehicle,
  services: ServiceRecord[],
  fuelRecords: FuelRecord[],
  expenseStats?: any
) {
  const printWindow = window.open('', '_blank');
  if (!printWindow) {
    window.print();
    return;
  }

  const totalServiceCost = services.reduce((sum, s) => sum + s.totalCost, 0);
  const totalFuelCost = fuelRecords.reduce((sum, f) => sum + f.totalCost, 0);

  const html = `
<!DOCTYPE html>
<html lang="fa" dir="rtl">
<head>
  <meta charset="UTF-8">
  <title>گزارش کامل خودرو ${vehicle.name}</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
  <link href="https://fonts.googleapis.com/css2?family=Vazirmatn:wght@400;600;700;800&display=swap" rel="stylesheet">
  <style>
    body {
      font-family: 'Vazirmatn', sans-serif;
      direction: rtl;
      padding: 30px;
      color: #1e293b;
      background: #ffffff;
      margin: 0;
    }
    .header {
      border-bottom: 2px solid #0891b2;
      padding-bottom: 16px;
      margin-bottom: 24px;
      display: flex;
      justify-content: space-between;
      align-items: center;
    }
    h1 { margin: 0; font-size: 24px; color: #0e7490; }
    h2 { font-size: 18px; margin-top: 24px; margin-bottom: 12px; color: #334155; border-bottom: 1px solid #e2e8f0; padding-bottom: 6px; }
    .badge { background: #e0f2fe; color: #0369a1; padding: 4px 10px; border-radius: 6px; font-size: 13px; font-weight: bold; }
    .grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 12px; margin-bottom: 20px; }
    .info-card { background: #f8fafc; border: 1px solid #e2e8f0; padding: 10px 14px; border-radius: 8px; }
    .info-label { font-size: 12px; color: #64748b; margin-bottom: 4px; }
    .info-val { font-size: 14px; font-weight: bold; color: #0f172a; }
    table { width: 100%; border-collapse: collapse; margin-top: 8px; margin-bottom: 20px; font-size: 13px; }
    th { background: #f1f5f9; color: #475569; padding: 8px 10px; text-align: right; border: 1px solid #cbd5e1; }
    td { padding: 8px 10px; border: 1px solid #e2e8f0; text-align: right; }
    tr:nth-child(even) { background: #f8fafc; }
    .total-box { background: #ecfeff; border: 1px solid #a5f3fc; padding: 14px; border-radius: 8px; margin-top: 10px; display: flex; justify-content: space-between; font-weight: bold; font-size: 15px; color: #0e7490; }
    @media print {
      body { padding: 10px; }
      .no-print { display: none; }
    }
  </style>
</head>
<body>
  <div class="header">
    <div>
      <h1>خودروهای من (My Car) - پرونده جامع نگهداری</h1>
      <p style="margin: 4px 0 0 0; font-size: 13px; color: #64748b;">تاریخ گزارش: ${formatToJalali(Date.now(), true)}</p>
    </div>
    <span class="badge">${vehicle.brand} ${vehicle.model}</span>
  </div>

  <h2>مشخصات خودرو</h2>
  <div class="grid">
    <div class="info-card"><div class="info-label">نام خودرو</div><div class="info-val">${vehicle.name}</div></div>
    <div class="info-card"><div class="info-label">برند و مدل</div><div class="info-val">${vehicle.brand} - ${vehicle.model}</div></div>
    <div class="info-card"><div class="info-label">سال ساخت</div><div class="info-val">${formatYear(vehicle.year)}</div></div>
    <div class="info-card"><div class="info-label">کیلومتر فعلی</div><div class="info-val">${formatWithCommas(vehicle.currentMileage)} کیلومتر</div></div>
    <div class="info-card"><div class="info-label">پلاک انتظامی</div><div class="info-val">${vehicle.licensePlate || '—'}</div></div>
    <div class="info-card"><div class="info-label">شماره شاسی (VIN)</div><div class="info-val">${vehicle.vin || '—'}</div></div>
  </div>

  <h2>سوابق سرویس و تعویض قطعات</h2>
  ${
    services.length === 0
      ? '<p style="color:#64748b; font-size:13px;">هیچ سابقه سرویسی برای این خودرو ثبت نشده است.</p>'
      : `
    <table>
      <thead>
        <tr>
          <th>ردیف</th>
          <th>تاریخ</th>
          <th>کیلومتر</th>
          <th>قطعه / سرویس</th>
          <th>نوع</th>
          <th>قطعه (تومان)</th>
          <th>اجرت (تومان)</th>
          <th>مجموع (تومان)</th>
          <th>تعمیرگاه</th>
        </tr>
      </thead>
      <tbody>
        ${services
          .map(
            (s, i) => `
          <tr>
            <td>${i + 1}</td>
            <td>${formatToJalali(s.dateTimestamp)}</td>
            <td>${formatWithCommas(s.mileage)}</td>
            <td><strong>${s.partName}</strong></td>
            <td>${s.operationType}</td>
            <td>${formatWithCommas(s.partPrice)}</td>
            <td>${formatWithCommas(s.laborCost)}</td>
            <td><strong>${formatWithCommas(s.totalCost)}</strong></td>
            <td>${s.repairShop || '—'}</td>
          </tr>
        `
          )
          .join('')}
      </tbody>
    </table>
  `
  }

  <h2>سوابق سوخت‌گیری</h2>
  ${
    fuelRecords.length === 0
      ? '<p style="color:#64748b; font-size:13px;">هیچ سوخت‌گیری ثبت نشده است.</p>'
      : `
    <table>
      <thead>
        <tr>
          <th>ردیف</th>
          <th>تاریخ</th>
          <th>کیلومتر</th>
          <th>مقدار (لیتر)</th>
          <th>مبلغ کل (تومان)</th>
          <th>جایگاه / یادداشت</th>
        </tr>
      </thead>
      <tbody>
        ${fuelRecords
          .map(
            (f, i) => `
          <tr>
            <td>${i + 1}</td>
            <td>${formatToJalali(f.dateTimestamp)}</td>
            <td>${formatWithCommas(f.mileage)}</td>
            <td>${f.liters}</td>
            <td>${formatWithCommas(f.totalCost)}</td>
            <td>${f.gasStation || f.notes || '—'}</td>
          </tr>
        `
          )
          .join('')}
      </tbody>
    </table>
  `
  }

  <div class="total-box">
    <span>مجموع هزینه‌های ثبت شده (سرویس‌ها + سوخت):</span>
    <span>${formatCurrency(totalServiceCost + totalFuelCost)}</span>
  </div>

  <div style="margin-top: 40px; text-align: center;" class="no-print">
    <button onclick="window.print()" style="background:#0891b2; color:white; border:none; padding:10px 24px; border-radius:6px; font-size:14px; cursor:pointer; font-family:'Vazirmatn';">چاپ / ذخیره به صورت PDF</button>
  </div>
</body>
</html>
`;

  printWindow.document.write(html);
  printWindow.document.close();
}

// Alias
export const exportToPrintableHTML = printPdfReport;
