package com.mycar.app.util

import java.text.NumberFormat
import java.util.Locale

object NumberFormatter {
    /**
     * Formats an [Int] with thousands separators (e.g. 52,255).
     */
    fun formatNumber(number: Int): String {
        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }

    /**
     * Formats a [Long] with thousands separators (e.g. 52,255).
     */
    fun formatNumber(number: Long): String {
        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }

    /**
     * Formats a [Double] with thousands separators.
     */
    fun formatNumber(number: Double): String {
        return NumberFormat.getNumberInstance(Locale.US).format(number)
    }
}

/**
 * Top-level function to format [Int] with thousands separators.
 */
fun formatNumber(number: Int): String = NumberFormatter.formatNumber(number)

/**
 * Top-level function to format [Long] with thousands separators.
 */
fun formatNumber(number: Long): String = NumberFormatter.formatNumber(number)

/**
 * Top-level function to format [Double] with thousands separators.
 */
fun formatNumber(number: Double): String = NumberFormatter.formatNumber(number)
