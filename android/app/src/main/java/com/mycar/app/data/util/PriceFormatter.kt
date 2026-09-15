package com.mycar.app.data.util

import java.text.NumberFormat
import java.util.Locale

object PriceFormatter {

    /**
     * Converts Persian and Arabic digits to ASCII English digits (0-9).
     */
    fun toEnglishDigits(input: String): String {
        val builder = StringBuilder(input.length)
        for (char in input) {
            when (char) {
                '۰', '٠' -> builder.append('0')
                '۱', '١' -> builder.append('1')
                '۲', '٢' -> builder.append('2')
                '۳', '٣' -> builder.append('3')
                '۴', '٤' -> builder.append('4')
                '۵', '٥' -> builder.append('5')
                '۶', '٦' -> builder.append('6')
                '۷', '٧' -> builder.append('7')
                '۸', '٨' -> builder.append('8')
                '۹', '٩' -> builder.append('9')
                else -> builder.append(char)
            }
        }
        return builder.toString()
    }

    /**
     * Cleans raw input by extracting only digits after normalizing Persian/Arabic digits.
     */
    fun cleanNumericString(input: String): String {
        val english = toEnglishDigits(input)
        return english.filter { it.isDigit() }
    }

    /**
     * Safely parses cost string to non-negative Long.
     * Commas, spaces, and Persian digits are handled cleanly.
     * Returns 0L if empty or invalid.
     */
    fun parseCost(input: String): Long {
        val clean = cleanNumericString(input)
        if (clean.isBlank()) return 0L
        return clean.toLongOrNull()?.coerceAtLeast(0L) ?: 0L
    }

    /**
     * Formats a Long number with standard thousands separators (e.g. 150000 -> "150,000").
     */
    fun formatWithSeparators(value: Long): String {
        if (value < 0L) return "-${formatWithSeparators(-value)}"
        if (value == 0L) return "0"
        return NumberFormat.getNumberInstance(Locale.US).format(value)
    }

    /**
     * Formats an Int number with standard thousands separators.
     */
    fun formatWithSeparators(value: Int): String {
        return formatWithSeparators(value.toLong())
    }

    /**
     * Formats text dynamically while the user types:
     * strips existing separators, parses digits, and formats with commas.
     * Returns empty string if the input has no digits.
     */
    fun formatInputAsYouType(rawInput: String): String {
        val digits = cleanNumericString(rawInput)
        if (digits.isBlank()) return ""
        val parsed = digits.toLongOrNull() ?: return digits
        return formatWithSeparators(parsed)
    }

    /**
     * Formats cost with currency unit (e.g. "150,000 تومان").
     */
    fun formatCostWithUnit(value: Long, unit: String = "تومان"): String {
        return "${formatWithSeparators(value)} $unit"
    }
}
