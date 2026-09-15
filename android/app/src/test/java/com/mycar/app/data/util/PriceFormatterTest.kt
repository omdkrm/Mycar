package com.mycar.app.data.util

import org.junit.Assert.assertEquals
import org.junit.Test

class PriceFormatterTest {

    @Test
    fun testPersianAndArabicDigitConversion() {
        assertEquals("150000", PriceFormatter.toEnglishDigits("۱۵۰۰۰۰"))
        assertEquals("1250000", PriceFormatter.toEnglishDigits("١٢٥٠٠٠٠"))
        assertEquals("1234567890", PriceFormatter.toEnglishDigits("۱۲۳۴۵۶۷۸۹۰"))
        assertEquals("1234567890", PriceFormatter.toEnglishDigits("١٢٣٤٥٦٧٨٩٠"))
        assertEquals("150,000", PriceFormatter.toEnglishDigits("۱۵۰,۰۰۰"))
    }

    @Test
    fun testFormatWithSeparators() {
        assertEquals("0", PriceFormatter.formatWithSeparators(0L))
        assertEquals("150,000", PriceFormatter.formatWithSeparators(150000L))
        assertEquals("1,250,000", PriceFormatter.formatWithSeparators(1250000L))
        assertEquals("12,500,000", PriceFormatter.formatWithSeparators(12500000L))
    }

    @Test
    fun testFormatInputAsYouType() {
        // Typing standard numbers
        assertEquals("1", PriceFormatter.formatInputAsYouType("1"))
        assertEquals("15", PriceFormatter.formatInputAsYouType("15"))
        assertEquals("150", PriceFormatter.formatInputAsYouType("150"))
        assertEquals("1,500", PriceFormatter.formatInputAsYouType("1500"))
        assertEquals("150,000", PriceFormatter.formatInputAsYouType("150000"))
        assertEquals("1,250,000", PriceFormatter.formatInputAsYouType("1250000"))

        // Typing with existing separators
        assertEquals("150,000", PriceFormatter.formatInputAsYouType("150,000"))
        assertEquals("1,500,000", PriceFormatter.formatInputAsYouType("150,0000"))

        // Typing Persian digits
        assertEquals("150,000", PriceFormatter.formatInputAsYouType("۱۵۰۰۰۰"))
        assertEquals("1,250,000", PriceFormatter.formatInputAsYouType("۱۲۵۰۰۰۰"))

        // Empty or non-digit input
        assertEquals("", PriceFormatter.formatInputAsYouType(""))
        assertEquals("", PriceFormatter.formatInputAsYouType("   "))
        assertEquals("", PriceFormatter.formatInputAsYouType("abc"))
    }

    @Test
    fun testParseCost() {
        assertEquals(0L, PriceFormatter.parseCost(""))
        assertEquals(150000L, PriceFormatter.parseCost("150,000"))
        assertEquals(1250000L, PriceFormatter.parseCost("1,250,000"))
        assertEquals(150000L, PriceFormatter.parseCost("۱۵۰,۰۰۰"))
        assertEquals(1250000L, PriceFormatter.parseCost("۱,۲۵۰,۰۰۰"))
        assertEquals(500000L, PriceFormatter.parseCost("500000 تومان"))
    }

    @Test
    fun testFormatCostWithUnit() {
        assertEquals("150,000 تومان", PriceFormatter.formatCostWithUnit(150000L))
        assertEquals("1,250,000 تومان", PriceFormatter.formatCostWithUnit(1250000L))
    }
}
