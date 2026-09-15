// Accurate Jalali (Solar Hijri) calendar conversion algorithm

export interface JalaliDate {
  jy: number; // Jalali year (e.g. 1404)
  jm: number; // Jalali month (1 - 12)
  jd: number; // Jalali day (1 - 31)
}

export const PERSIAN_MONTH_NAMES = [
  'فروردین',
  'اردیبهشت',
  'خرداد',
  'تیر',
  'مرداد',
  'شهریور',
  'مهر',
  'آبان',
  'آذر',
  'دی',
  'بهمن',
  'اسفند',
];

/**
 * Converts Gregorian Date to Jalali Date
 */
export function gregorianToJalali(gy: number, gm: number, gd: number): JalaliDate {
  const g_d_m = [0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334];
  let jy: number;
  if (gy > 1600) {
    jy = 979;
    gy -= 1600;
  } else {
    jy = 0;
    gy -= 621;
  }
  const gy2 = gm > 2 ? gy + 1 : gy;
  let days =
    365 * gy +
    Math.floor((gy2 + 3) / 4) -
    Math.floor((gy2 + 99) / 100) +
    Math.floor((gy2 + 399) / 400) -
    80 +
    gd +
    g_d_m[gm - 1];
  jy += 33 * Math.floor(days / 12053);
  days %= 12053;
  jy += 4 * Math.floor(days / 1461);
  days %= 1461;
  if (days > 365) {
    jy += Math.floor((days - 1) / 365);
    days = (days - 1) % 365;
  }
  const jm = days < 186 ? 1 + Math.floor(days / 31) : 7 + Math.floor((days - 186) / 30);
  const jd = 1 + (days < 186 ? days % 31 : (days - 186) % 30);
  return { jy, jm, jd };
}

/**
 * Converts Jalali Date to Gregorian Date
 */
export function jalaliToGregorian(jy: number, jm: number, jd: number): { gy: number; gm: number; gd: number } {
  let gy: number;
  if (jy > 979) {
    gy = 1600;
    jy -= 979;
  } else {
    gy = 621;
  }
  let days =
    365 * jy +
    Math.floor(jy / 33) * 8 +
    Math.floor(((jy % 33) + 3) / 4) +
    78 +
    jd +
    (jm < 7 ? (jm - 1) * 31 : (jm - 7) * 30 + 186);
  gy += 400 * Math.floor(days / 146097);
  days %= 146097;
  if (days > 36524) {
    gy += 100 * Math.floor(--days / 36524);
    days %= 36524;
    if (days >= 365) days++;
  }
  gy += 4 * Math.floor(days / 1461);
  days %= 1461;
  if (days > 365) {
    gy += Math.floor((days - 1) / 365);
    days = (days - 1) % 365;
  }
  let gd = days + 1;
  const sal_a = [
    0,
    31,
    (gy % 4 === 0 && gy % 100 !== 0) || gy % 400 === 0 ? 29 : 28,
    31,
    30,
    31,
    30,
    31,
    31,
    30,
    31,
    30,
    31,
  ];
  let gm: number;
  for (gm = 0; gm < 13 && gd > sal_a[gm]; gm++) {
    gd -= sal_a[gm];
  }
  return { gy, gm, gd };
}

/**
 * Formats a Date or timestamp to Persian Jalali format e.g. "1404/06/14"
 */
export function formatToJalali(
  timestampOrDate: number | Date | undefined,
  includeMonthName: boolean = false
): string {
  if (!timestampOrDate) return '—';
  const d = typeof timestampOrDate === 'number' ? new Date(timestampOrDate) : timestampOrDate;
  if (isNaN(d.getTime())) return '—';

  const j = gregorianToJalali(d.getFullYear(), d.getMonth() + 1, d.getDate());
  const monthStr = j.jm < 10 ? `0${j.jm}` : `${j.jm}`;
  const dayStr = j.jd < 10 ? `0${j.jd}` : `${j.jd}`;

  if (includeMonthName) {
    return `${j.jd} ${PERSIAN_MONTH_NAMES[j.jm - 1]} ${j.jy}`;
  }
  return `${j.jy}/${monthStr}/${dayStr}`;
}

/**
 * Formats timestamp to relative Persian description e.g. "امروز", "۵ روز پیش", "۱۰ روز دیگر"
 */
export function getRelativeJalaliDays(targetTimestamp: number, baseTimestamp: number = Date.now()): string {
  const diffMs = targetTimestamp - baseTimestamp;
  const diffDays = Math.round(diffMs / (1000 * 60 * 60 * 24));

  if (diffDays === 0) return 'امروز';
  if (diffDays === 1) return 'فردا';
  if (diffDays === -1) return 'دیروز';
  if (diffDays > 0) return `${diffDays} روز دیگر`;
  return `${Math.abs(diffDays)} روز پیش`;
}

/**
 * Parse a Jalali string like "1404/06/14" or "1404-06-14" into a JavaScript Date object (UTC midnight)
 */
export function parseJalaliStringToDate(str: string): Date | null {
  if (!str) return null;
  const cleaned = str.replace(/[^\d\/-]/g, '').trim();
  const parts = cleaned.split(/[\/-]/).map(Number);
  if (parts.length !== 3) return null;
  const [jy, jm, jd] = parts;
  if (jy < 1300 || jy > 1500 || jm < 1 || jm > 12 || jd < 1 || jd > 31) return null;

  const { gy, gm, gd } = jalaliToGregorian(jy, jm, jd);
  return new Date(gy, gm - 1, gd, 12, 0, 0); // midday to prevent timezone edge shifts
}

/**
 * Gets the current Jalali Year (e.g. 1404)
 */
export function getCurrentJalaliYear(): number {
  const now = new Date();
  const j = gregorianToJalali(now.getFullYear(), now.getMonth() + 1, now.getDate());
  return j.jy;
}

export const PERSIAN_WEEK_DAYS_SHORT = ['ش', 'ی', 'د', 'س', 'چ', 'پ', 'ج'];

export function isLeapJalaliYear(jy: number): boolean {
  const rem = jy % 33;
  return [1, 5, 9, 13, 17, 22, 26, 30].includes(rem);
}

export function getDaysInJalaliMonth(jy: number, jm: number): number {
  if (jm >= 1 && jm <= 6) return 31;
  if (jm >= 7 && jm <= 11) return 30;
  if (jm === 12) return isLeapJalaliYear(jy) ? 30 : 29;
  return 30;
}

/**
 * Returns 0 for Saturday, 1 for Sunday, ..., 6 for Friday
 */
export function getFirstDayOfJalaliMonth(jy: number, jm: number): number {
  const { gy, gm, gd } = jalaliToGregorian(jy, jm, 1);
  const d = new Date(gy, gm - 1, gd);
  const dow = d.getDay(); // 0 is Sunday, 6 is Saturday
  return (dow + 1) % 7; // Convert to Saturday = 0, Sunday = 1, ...
}

