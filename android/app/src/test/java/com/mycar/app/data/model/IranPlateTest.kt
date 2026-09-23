package com.mycar.app.data.model

import com.mycar.app.ui.components.IranPlate
import com.mycar.app.ui.components.IranPlateParser
import com.mycar.app.ui.components.PlateType
import org.junit.Assert.*
import org.junit.Test

class IranPlateTest {

    @Test
    fun testParsePersianDigitsPlate() {
        val raw = "۱۲۳ ب ۴۵ ایران ۱۲"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should successfully parse Persian plate", parsed)
        assertEquals("123", parsed?.threeDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("45", parsed?.twoDigits)
        assertEquals("12", parsed?.provinceCode)
        assertTrue(parsed?.isValid == true)
        assertEquals("۱۲۳ ب ۴۵ ایران ۱۲", parsed?.toNormalizedString())
    }

    @Test
    fun testParseEnglishDigitsPlate() {
        val raw = "123 ب 45 ایران 12"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should successfully parse English digits in plate", parsed)
        assertEquals("123", parsed?.threeDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("45", parsed?.twoDigits)
        assertEquals("12", parsed?.provinceCode)
        assertTrue(parsed?.isValid == true)
        assertEquals("۱۲۳ ب ۴۵ ایران ۱۲", parsed?.toNormalizedString())
    }

    @Test
    fun testParseCompactHyphenPlate() {
        val raw = "123ب45-12"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should parse compact hyphen format", parsed)
        assertEquals("123", parsed?.threeDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("45", parsed?.twoDigits)
        assertEquals("12", parsed?.provinceCode)
    }

    @Test
    fun testParseReverseTwoDigitsFirstFormat() {
        val raw = "45 ب 123 ایران 12"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should parse 2-digits first format", parsed)
        assertEquals("123", parsed?.threeDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("45", parsed?.twoDigits)
        assertEquals("12", parsed?.provinceCode)
    }

    @Test
    fun testUnparseableStringReturnsNull() {
        assertNull(IranPlateParser.parse(""))
        assertNull(IranPlateParser.parse("   "))
        assertNull(IranPlateParser.parse("12345"))
        assertNull(IranPlateParser.parse("پلاک قدیمی"))
        assertNull(IranPlateParser.parse("تهران 11"))
    }

    @Test
    fun testValidPlateLetters() {
        val validLetters = listOf("ب", "ج", "د", "س", "ص", "ط", "ق", "ل", "م", "ن", "و", "ه", "ی")
        validLetters.forEach { letter ->
            val plate = IranPlate(
                threeDigits = "123",
                letter = letter,
                twoDigits = "45",
                provinceCode = "67"
            )
            assertTrue("Letter $letter should be valid", plate.isValid)
        }

        // Invalid letters for personal vehicles
        val invalidLetters = listOf("X", "1", "الف", "ث", "Z", "#")
        invalidLetters.forEach { letter ->
            val plate = IranPlate(
                threeDigits = "123",
                letter = letter,
                twoDigits = "45",
                provinceCode = "67"
            )
            assertFalse("Letter $letter should be invalid for personal vehicle", plate.isValid)
        }
    }

    @Test
    fun testDigitLengthsValidation() {
        // Incomplete threeDigits
        assertFalse(IranPlate(threeDigits = "12", letter = "ب", twoDigits = "45", provinceCode = "12").isValid)
        // Incomplete twoDigits
        assertFalse(IranPlate(threeDigits = "123", letter = "ب", twoDigits = "4", provinceCode = "12").isValid)
        // Incomplete province
        assertFalse(IranPlate(threeDigits = "123", letter = "ب", twoDigits = "45", provinceCode = "1").isValid)
        // Complete
        assertTrue(IranPlate(threeDigits = "123", letter = "ب", twoDigits = "45", provinceCode = "12").isValid)
    }

    @Test
    fun testPlateTypesEnum() {
        assertEquals(PlateType.PERSONAL, PlateType.valueOf("PERSONAL"))
        assertEquals(PlateType.TAXI, PlateType.valueOf("TAXI"))
        assertEquals(PlateType.GOVERNMENT, PlateType.valueOf("GOVERNMENT"))
        assertEquals(PlateType.POLICE, PlateType.valueOf("POLICE"))
    }
}
