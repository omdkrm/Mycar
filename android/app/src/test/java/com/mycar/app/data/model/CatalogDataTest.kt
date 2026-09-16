package com.mycar.app.data.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogDataTest {

    @Test
    fun testNormalizeText() {
        // Arabic Yeh and Kaf conversion
        val inputWithArabic = "روغني كاسترول"
        val normalized = CatalogData.normalizeText(inputWithArabic)
        assertEquals("روغنی کاسترول", normalized)

        // ZWNJ to space
        val inputWithZwnj = "لنت\u200Cترمز"
        assertEquals("لنت ترمز", CatalogData.normalizeText(inputWithZwnj))

        // Multiple spaces
        val inputWithSpaces = "  فیلتر    هوا   "
        assertEquals("فیلتر هوا", CatalogData.normalizeText(inputWithSpaces))

        // English lowercase
        assertEquals("engine oil dot4", CatalogData.normalizeText("Engine Oil DOT4"))
    }

    @Test
    fun testFilterItemsWithPersianQuery() {
        // Query "روغن" matches engine oil, oil filter, brake fluid, gearbox oil, hydraulic oil
        val results = CatalogData.filterItems("روغن")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.id == "cat-engine-oil" })
        assertTrue(results.any { it.id == "cat-oil-filter" })
        assertTrue(results.any { it.id == "cat-brake-fluid" })
        assertTrue(results.any { it.id == "cat-gearbox-oil" })
        assertTrue(results.any { it.id == "cat-hydraulic-oil" })

        // Most relevant (starts with "روغن موتور") should be first
        assertEquals("cat-engine-oil", results.first().id)
    }

    @Test
    fun testFilterItemsWithCategoryQuery() {
        // Query "سیستم ترمز" or "ترمز" should match brake items
        val results = CatalogData.filterItems("ترمز")
        assertTrue(results.isNotEmpty())
        assertTrue(results.any { it.id == "cat-brake-pads-front" })
        assertTrue(results.any { it.id == "cat-brake-pads-rear" })
        assertTrue(results.any { it.id == "cat-brake-fluid" })
    }

    @Test
    fun testFilterItemsMultiWord() {
        // "لنت جلو" should match "لنت ترمز جلو"
        val results = CatalogData.filterItems("لنت جلو")
        assertTrue(results.isNotEmpty())
        assertEquals("cat-brake-pads-front", results.first().id)
    }

    @Test
    fun testFilterItemsNonMatching() {
        // Non-matching query should return empty list
        val results = CatalogData.filterItems("قطعه کاملا نامعلوم و عجیب")
        assertTrue(results.isEmpty())
    }

    @Test
    fun testFilterItemsEmptyQuery() {
        // Blank query should return all items
        val results = CatalogData.filterItems("")
        assertEquals(CatalogData.items.size, results.size)
    }

    @Test
    fun testFindExactMatch() {
        val exact = CatalogData.findExactMatch("روغن موتور")
        assertNotNull(exact)
        assertEquals("cat-engine-oil", exact?.id)

        // Exact match with Arabic Yeh should also work
        val exactArabic = CatalogData.findExactMatch("روغني موتور")
        assertNotNull(exactArabic)
        assertEquals("cat-engine-oil", exactArabic?.id)

        // Partial or modified text is NOT an exact match
        val partial = CatalogData.findExactMatch("روغن موتور بهران")
        assertNull(partial)
    }

    @Test
    fun testCategoriesPresent() {
        val categories = CatalogData.items.map { it.category }.toSet()
        // Requirements mention engine, brakes, suspension, general services, refueling
        assertTrue("Must include brake system", categories.contains("سیستم ترمز"))
        assertTrue("Must include suspension", categories.contains("چرخ و تعلیق"))
        assertTrue("Must include general services", categories.contains("سرویس‌های عمومی"))
        assertTrue("Must include refueling", categories.contains("سوخت و باک"))
    }
}
