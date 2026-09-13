/**
 * Number and currency formatting utilities tailored for Persian RTL automotive application
 */

// Persian digits
const PERSIAN_DIGITS = ['۰', '۱', '۲', '۳', '۴', '۵', '۶', '۷', '۸', '۹'];
const ARABIC_DIGITS = ['٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩'];

/**
 * Normalizes input string containing Persian or Arabic digits to English digits
 */
export function normalizeDigits(input: string | number | undefined | null): string {
  if (input === undefined || input === null) return '';
  let str = String(input);
  // Replace Persian digits
  for (let i = 0; i < 10; i++) {
    str = str.replace(new RegExp(PERSIAN_DIGITS[i], 'g'), String(i));
  }
  // Replace Arabic digits
  for (let i = 0; i < 10; i++) {
    str = str.replace(new RegExp(ARABIC_DIGITS[i], 'g'), String(i));
  }
  return str;
}

/**
 * Parses numeric input string (handles Persian/Arabic digits and commas)
 */
export function parseNumericInput(input: string | number | undefined | null): number {
  if (input === undefined || input === null) return 0;
  const normalized = normalizeDigits(input).replace(/,/g, '').trim();
  const parsed = parseFloat(normalized);
  return isNaN(parsed) ? 0 : parsed;
}

/**
 * Formats a number with thousands separators (e.g. 2,500,000)
 */
export function formatWithCommas(num: number | string | undefined | null): string {
  if (num === undefined || num === null) return '۰';
  const val = typeof num === 'string' ? parseNumericInput(num) : num;
  if (isNaN(val)) return '۰';
  return Math.round(val).toLocaleString('en-US');
}

/**
 * Converts English digits to Persian digits
 */
export function toPersianDigits(input: string | number | undefined | null): string {
  if (input === undefined || input === null) return '';
  const str = String(input);
  return str.replace(/\d/g, (d) => PERSIAN_DIGITS[parseInt(d, 10)]);
}

/**
 * Formats vehicle mileage with thousands separator and unit
 * Example: "52,255 کیلومتر"
 */
export function formatMileage(km: number | undefined | null, usePersianDigits: boolean = false): string {
  if (km === undefined || km === null) return `0 کیلومتر`;
  const formatted = formatWithCommas(km);
  const result = usePersianDigits ? toPersianDigits(formatted) : formatted;
  return `${result} کیلومتر`;
}

/**
 * Formats price/cost with thousands separator and currency (تومان)
 * Example: "2,500,000 تومان"
 */
export function formatCurrency(amount: number | undefined | null, usePersianDigits: boolean = false): string {
  if (amount === undefined || amount === null) return `۰ تومان`;
  const formatted = formatWithCommas(amount);
  const result = usePersianDigits ? toPersianDigits(formatted) : formatted;
  return `${result} تومان`;
}

/**
 * Formats vehicle manufacturing year - STRICT RULE: NO THOUSANDS SEPARATORS
 * Example: "1401" or "2023"
 */
export function formatYear(year: number | undefined | null, usePersianDigits: boolean = false): string {
  if (!year) return '—';
  const str = String(year);
  return usePersianDigits ? toPersianDigits(str) : str;
}

/**
 * Formats fuel volume in Liters
 * Example: "45.5 لیتر"
 */
export function formatLiters(liters: number | undefined | null): string {
  if (liters === undefined || liters === null) return `۰ لیتر`;
  return `${Number(liters.toFixed(1))} لیتر`;
}

/**
 * Formats fuel consumption rate
 * Example: "7.4 لیتر در ۱۰۰ کیلومتر"
 */
export function formatConsumption(rate: number | null | undefined): string {
  if (rate === null || rate === undefined || isNaN(rate) || rate <= 0) {
    return 'داده ناکافی';
  }
  return `${rate.toFixed(1)} لیتر / ۱۰۰ ک‌م`;
}
