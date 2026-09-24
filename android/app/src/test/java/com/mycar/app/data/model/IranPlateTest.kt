package com.mycar.app.data.model

import com.mycar.app.ui.components.IranPlate
import com.mycar.app.ui.components.IranPlateParser
import com.mycar.app.ui.components.PlateType
import org.junit.Assert.*
import org.junit.Test

class IranPlateTest {

    @Test
    fun testParseStandardNewPersianPlate() {
        // New structure: 2 digits + letter + 3 digits + ایران + 2 digits
        val raw = "۱۲ ب ۳۶۵ ایران ۳۳"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should successfully parse Persian plate", parsed)
        assertEquals("12", parsed?.twoDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("365", parsed?.threeDigits)
        assertEquals("33", parsed?.provinceCode)
        assertTrue(parsed?.isValid == true)
        assertEquals("۱۲ ب ۳۶۵ ایران ۳۳", parsed?.toNormalizedString())
    }

    @Test
    fun testParseStandardNewEnglishDigitsPlate() {
        val raw = "12 ب 365 ایران 33"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should successfully parse English digits in plate", parsed)
        assertEquals("12", parsed?.twoDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("365", parsed?.threeDigits)
        assertEquals("33", parsed?.provinceCode)
        assertTrue(parsed?.isValid == true)
        assertEquals("۱۲ ب ۳۶۵ ایران ۳۳", parsed?.toNormalizedString())
    }

    @Test
    fun testBackwardCompatibilityOldFormat() {
        // Old stored format: 3 digits + letter + 2 digits + ایران + 2 digits
        val raw = "365 ب 12 ایران 33"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should parse old stored format without crashing", parsed)
        assertEquals("12", parsed?.twoDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("365", parsed?.threeDigits)
        assertEquals("33", parsed?.provinceCode)
        assertTrue(parsed?.isValid == true)
        assertEquals("۱۲ ب ۳۶۵ ایران ۳۳", parsed?.toNormalizedString())
    }

    @Test
    fun testBackwardCompatibilityOldPersianDigits() {
        val raw = "۳۶۵ ب ۱۲ ایران ۳۳"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should parse old Persian digits format", parsed)
        assertEquals("12", parsed?.twoDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("365", parsed?.threeDigits)
        assertEquals("33", parsed?.provinceCode)
        assertTrue(parsed?.isValid == true)
    }

    @Test
    fun testParseCompactHyphenPlates() {
        val rawNew = "12ب365-33"
        val parsedNew = IranPlateParser.parse(rawNew)
        assertNotNull("Should parse compact hyphen format (2 digits first)", parsedNew)
        assertEquals("12", parsedNew?.twoDigits)
        assertEquals("ب", parsedNew?.letter)
        assertEquals("365", parsedNew?.threeDigits)
        assertEquals("33", parsedNew?.provinceCode)

        val rawOld = "365ب12-33"
        val parsedOld = IranPlateParser.parse(rawOld)
        assertNotNull("Should parse compact hyphen format (3 digits first)", parsedOld)
        assertEquals("12", parsedOld?.twoDigits)
        assertEquals("ب", parsedOld?.letter)
        assertEquals("365", parsedOld?.threeDigits)
        assertEquals("33", parsedOld?.provinceCode)
    }

    @Test
    fun testParseIranPrefixFormat() {
        val raw = "ایران 33 12 ب 365"
        val parsed = IranPlateParser.parse(raw)
        assertNotNull("Should parse Iran-prefix format", parsed)
        assertEquals("12", parsed?.twoDigits)
        assertEquals("ب", parsed?.letter)
        assertEquals("365", parsed?.threeDigits)
        assertEquals("33", parsed?.provinceCode)
    }

    @Test
    fun testUnparseableStringReturnsNullWithoutCrashing() {
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
                twoDigits = "12",
                letter = letter,
                threeDigits = "365",
                provinceCode = "33"
            )
            assertTrue("Letter $letter should be valid", plate.isValid)
        }

        // Invalid letters for personal vehicles
        val invalidLetters = listOf("X", "1", "الف", "ث", "Z", "#")
        invalidLetters.forEach { letter ->
            val plate = IranPlate(
                twoDigits = "12",
                letter = letter,
                threeDigits = "365",
                provinceCode = "33"
            )
            assertFalse("Letter $letter should be invalid for personal vehicle", plate.isValid)
        }
    }

    @Test
    fun testDigitLengthsValidation() {
        // Incomplete twoDigits
        assertFalse(IranPlate(twoDigits = "1", letter = "ب", threeDigits = "365", provinceCode = "33").isValid)
        // Incomplete threeDigits
        assertFalse(IranPlate(twoDigits = "12", letter = "ب", threeDigits = "36", provinceCode = "33").isValid)
        // Incomplete province
        assertFalse(IranPlate(twoDigits = "12", letter = "ب", threeDigits = "365", provinceCode = "3").isValid)
        // Complete & Valid
        assertTrue(IranPlate(twoDigits = "12", letter = "ب", threeDigits = "365", provinceCode = "33").isValid)
    }

    @Test
    fun testPlateTypesEnum() {
        assertEquals(PlateType.PERSONAL, PlateType.valueOf("PERSONAL"))
        assertEquals(PlateType.TAXI, PlateType.valueOf("TAXI"))
        assertEquals(PlateType.GOVERNMENT, PlateType.valueOf("GOVERNMENT"))
        assertEquals(PlateType.POLICE, PlateType.valueOf("POLICE"))
    }
}
