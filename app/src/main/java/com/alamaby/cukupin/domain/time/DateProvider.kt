package com.alamaby.cukupin.domain.time

import java.time.LocalDate

/**
 * Abstraksi waktu agar ViewModel/use case tidak membaca jam sistem langsung.
 * Memudahkan pengujian pergantian hari tanpa mengubah jam perangkat (PRD §53).
 */
interface DateProvider {
    fun today(): LocalDate
}
