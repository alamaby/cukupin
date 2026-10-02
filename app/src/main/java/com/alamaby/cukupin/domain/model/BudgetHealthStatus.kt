package com.alamaby.cukupin.domain.model

/**
 * Status kesehatan dana: apakah pengeluaran masih sesuai target.
 * Nilai ini murni domain — teks UI dipetakan di lapisan presentation.
 */
enum class BudgetHealthStatus {
    /** Periode belum dimulai. */
    NOT_STARTED,

    /** Pengeluaran jauh di bawah jalur ideal. */
    SAFE,

    /** Pengeluaran sekitar jalur ideal. */
    ON_TRACK,

    /** Sedikit di atas jalur ideal, perlu perhatian. */
    WATCH,

    /** Jauh di atas jalur ideal, berisiko habis sebelum waktunya. */
    AT_RISK,

    /** Saldo habis atau negatif saat periode masih berjalan. */
    DEPLETED,

    /** Periode selesai dan dana bertahan (saldo akhir >= 0). */
    FINISHED_WITH_BALANCE,

    /** Periode selesai tetapi pengeluaran melebihi dana (saldo akhir < 0). */
    FINISHED_EXCEEDED
}
