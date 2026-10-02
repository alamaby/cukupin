package com.alamaby.cukupin.ui.util

import java.text.NumberFormat
import java.util.Locale

private val indonesianLocale = Locale("id", "ID")

/**
 * Format nominal rupiah. Nilai penyimpanan tetap Long;
 * format hanya untuk tampilan (NFR-03).
 */
fun formatRupiah(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(indonesianLocale)
    return "Rp" + nf.format(amount)
}

/** Format selisih dengan tanda +/− yang jelas. */
fun formatRupiahSigned(amount: Long): String {
    val nf = NumberFormat.getNumberInstance(indonesianLocale)
    return when {
        amount > 0 -> "+Rp" + nf.format(amount)
        amount < 0 -> "−Rp" + nf.format(-amount)
        else -> "Rp0"
    }
}

/** Parse input pengguna ("1.500.000" atau "1500000") menjadi Long. */
fun parseRupiahInput(input: String): Long? {
    val digits = input.filter { it.isDigit() }
    if (digits.isEmpty()) return null
    return digits.toLongOrNull()
}
