package com.alamaby.cukupin.domain.model

/**
 * Tingkat keyakinan prediksi daya tahan dana.
 * Didasarkan pada jumlah hari dengan data pengeluaran tercatat,
 * bukan sekadar umur target.
 */
enum class PredictionConfidence {
    /** Belum ada pengeluaran tercatat. */
    UNAVAILABLE,

    /** 1–2 hari data tercatat. */
    EARLY,

    /** 3–6 hari data tercatat. */
    DEVELOPING,

    /** 7+ hari data tercatat. */
    ESTABLISHED
}
