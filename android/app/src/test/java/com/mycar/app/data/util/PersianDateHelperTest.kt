package com.mycar.app.data.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class PersianDateHelperTest {

    @Test
    fun testGregorianToJalaliKnownDates() {
        // Nowruz 1403: 2024-03-20
        val j1 = PersianDateHelper.gregorianToJalali(2024, 3, 20)
        assertEquals(1403, j1.year)
        assertEquals(1, j1.month)
        assertEquals(1, j1.day)
        assertEquals("1403/01/01", j1.toString())

        // 2024-03-19: 1402/12/29
        val j2 = PersianDateHelper.gregorianToJalali(2024, 3, 19)
        assertEquals(1402, j2.year)
        assertEquals(12, j2.month)
        assertEquals(29, j2.day)

        // 2026-09-15: 1405/06/24
        val j3 = PersianDateHelper.gregorianToJalali(2026, 9, 15)
        assertEquals(1405, j3.year)
        assertEquals(6, j3.month)
        assertEquals(24, j3.day)
    }

    @Test
    fun testJalaliToGregorianRoundtrip() {
        val (gy, gm, gd) = PersianDateHelper.jalaliToGregorian(1403, 1, 1)
        assertEquals(2024, gy)
        assertEquals(3, gm)
        assertEquals(20, gd)

        val (gy2, gm2, gd2) = PersianDateHelper.jalaliToGregorian(1405, 6, 24)
        assertEquals(2026, gy2)
        assertEquals(9, gm2)
        assertEquals(15, gd2)
    }

    @Test
    fun testParseJalaliDate() {
        val ts1 = PersianDateHelper.parseJalaliDate("1403/06/25")
        assertNotNull(ts1)
        val formatted1 = PersianDateHelper.formatJalali(ts1!!)
        assertEquals("1403/06/25", formatted1)

        // Test with Persian digits
        val ts2 = PersianDateHelper.parseJalaliDate("۱۴۰۳/۰۶/۲۵")
        assertNotNull(ts2)
        assertEquals("1403/06/25", PersianDateHelper.formatJalali(ts2!!))

        // Test with dash delimiter
        val ts3 = PersianDateHelper.parseJalaliDate("1403-01-15")
        assertNotNull(ts3)
        assertEquals("1403/01/15", PersianDateHelper.formatJalali(ts3!!))
    }

    @Test
    fun testInvalidJalaliDates() {
        assertNull(PersianDateHelper.parseJalaliDate("invalid"))
        assertNull(PersianDateHelper.parseJalaliDate("1403/13/01")) // Month 13 invalid
        assertNull(PersianDateHelper.parseJalaliDate("1403/07/31")) // Month 7 only has 30 days
        assertNull(PersianDateHelper.parseJalaliDate("1200/01/01")) // Out of range
        assertTrue(!PersianDateHelper.isValidJalaliDate("99/99/99"))
    }

    @Test
    fun testCalendarCalculations() {
        // Month names
        assertEquals(12, PersianDateHelper.PERSIAN_MONTH_NAMES.size)
        assertEquals("فروردین", PersianDateHelper.PERSIAN_MONTH_NAMES[0])
        assertEquals("اسفند", PersianDateHelper.PERSIAN_MONTH_NAMES[11])

        // Weekdays short
        assertEquals(7, PersianDateHelper.WEEK_DAYS_SHORT.size)
        assertEquals("ش", PersianDateHelper.WEEK_DAYS_SHORT[0])
        assertEquals("ج", PersianDateHelper.WEEK_DAYS_SHORT[6])

        // Leap year checks
        assertTrue(PersianDateHelper.isLeapJalaliYear(1403))
        assertTrue(!PersianDateHelper.isLeapJalaliYear(1402))
        assertTrue(!PersianDateHelper.isLeapJalaliYear(1404))
        assertTrue(PersianDateHelper.isLeapJalaliYear(1399))

        // Days in month
        assertEquals(31, PersianDateHelper.getDaysInMonth(1403, 1))
        assertEquals(31, PersianDateHelper.getDaysInMonth(1403, 6))
        assertEquals(30, PersianDateHelper.getDaysInMonth(1403, 7))
        assertEquals(30, PersianDateHelper.getDaysInMonth(1403, 11))
        assertEquals(30, PersianDateHelper.getDaysInMonth(1403, 12)) // Leap year Esfand
        assertEquals(29, PersianDateHelper.getDaysInMonth(1402, 12)) // Normal year Esfand

        // First day of week for 1403/01/01 (2024-03-20 was Wednesday -> 4 in Persian week starting Saturday)
        assertEquals(4, PersianDateHelper.getFirstDayOfWeek(1403, 1))

        // formatJalaliFull
        val ts = PersianDateHelper.jalaliToTimestamp(1405, 6, 24)
        val fullStr = PersianDateHelper.formatJalaliFull(ts)
        assertTrue(fullStr.contains("24"))
        assertTrue(fullStr.contains("شهریور"))
        assertTrue(fullStr.contains("1405"))
    }
}
