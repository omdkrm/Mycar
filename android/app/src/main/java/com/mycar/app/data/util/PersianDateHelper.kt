package com.mycar.app.data.util

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

object PersianDateHelper {

    data class JalaliDate(val year: Int, val month: Int, val day: Int) {
        override fun toString(): String = String.format(Locale.US, "%04d/%02d/%02d", year, month, day)
    }

    /**
     * Converts a Gregorian date (year, month 1-12, day 1-31) to a Jalali (Solar Hijri) date.
     */
    fun gregorianToJalali(gy: Int, gm: Int, gd: Int): JalaliDate {
        val gDaysInMonth = intArrayOf(0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334)
        val gy2 = if (gm > 2) gy + 1 else gy
        var days = 355666 + (365 * gy) + ((gy2 + 3) / 4) - ((gy2 + 99) / 100) + ((gy2 + 399) / 400) + gd + gDaysInMonth[gm - 1]
        var jy = -1595 + (33 * (days / 12053))
        days %= 12053
        jy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            jy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val jm: Int
        val jd: Int
        if (days < 186) {
            jm = 1 + (days / 31)
            jd = 1 + (days % 31)
        } else {
            jm = 7 + ((days - 186) / 30)
            jd = 1 + ((days - 186) % 30)
        }
        return JalaliDate(jy, jm, jd)
    }

    /**
     * Converts a Jalali date to Gregorian (gy, gm 1-12, gd 1-31).
     */
    fun jalaliToGregorian(jy: Int, jm: Int, jd: Int): Triple<Int, Int, Int> {
        val jy2 = jy + 1595
        var days = -355668 + (365 * jy2) + ((jy2 / 33) * 8) + (((jy2 % 33) + 3) / 4) + jd + (if (jm < 7) (jm - 1) * 31 else ((jm - 7) * 30) + 186)
        var gy = 400 * (days / 146097)
        days %= 146097
        if (days > 36524) {
            days--
            gy += 100 * (days / 36524)
            days %= 36524
            if (days >= 365) days++
        }
        gy += 4 * (days / 1461)
        days %= 1461
        if (days > 365) {
            gy += (days - 1) / 365
            days = (days - 1) % 365
        }
        val salA = intArrayOf(0, 31, if ((gy % 4 == 0 && gy % 100 != 0) || (gy % 400 == 0)) 29 else 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
        var gm = 0
        while (gm < 13 && days >= salA[gm]) {
            days -= salA[gm]
            gm++
        }
        val gd = days + 1
        return Triple(gy, gm, gd)
    }

    /**
     * Converts a millisecond timestamp to JalaliDate using Asia/Tehran timezone.
     */
    fun timestampToJalali(timestampMillis: Long): JalaliDate {
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.timeInMillis = timestampMillis
        val gy = cal.get(Calendar.YEAR)
        val gm = cal.get(Calendar.MONTH) + 1
        val gd = cal.get(Calendar.DAY_OF_MONTH)
        return gregorianToJalali(gy, gm, gd)
    }

    /**
     * Formats timestamp into Persian/Jalali string like "1403/06/25".
     */
    fun formatJalali(timestampMillis: Long): String {
        return timestampToJalali(timestampMillis).toString()
    }

    /**
     * Converts Jalali (year, month, day) to UTC millisecond timestamp at 12:00 PM Tehran time.
     */
    fun jalaliToTimestamp(jy: Int, jm: Int, jd: Int): Long {
        val (gy, gm, gd) = jalaliToGregorian(jy, jm, jd)
        val cal = Calendar.getInstance(TimeZone.getTimeZone("Asia/Tehran"))
        cal.set(Calendar.YEAR, gy)
        cal.set(Calendar.MONTH, gm - 1)
        cal.set(Calendar.DAY_OF_MONTH, gd)
        cal.set(Calendar.HOUR_OF_DAY, 12)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /**
     * Parses a Jalali date string (e.g. "1403/06/25" or "1403-06-25", Persian or English digits)
     * and returns the epoch millisecond timestamp, or null if invalid.
     */
    fun parseJalaliDate(input: String): Long? {
        val clean = PriceFormatter.toEnglishDigits(input.trim())
        val parts = clean.split(Regex("[/\\-.,\\s]+"))
        if (parts.size == 3) {
            val y = parts[0].toIntOrNull() ?: return null
            val m = parts[1].toIntOrNull() ?: return null
            val d = parts[2].toIntOrNull() ?: return null
            if (y in 1300..1500 && m in 1..12 && d in 1..31) {
                // Check valid days for month: 1-6 have 31 days, 7-11 have 30 days, 12 has 29 (or 30 in leap year)
                if (m in 7..11 && d > 30) return null
                if (m == 12 && d > 30) return null
                return jalaliToTimestamp(y, m, d)
            }
        }
        return null
    }

    /**
     * Checks whether a string is a valid Jalali date format.
     */
    fun isValidJalaliDate(input: String): Boolean {
        return parseJalaliDate(input) != null
    }
}
