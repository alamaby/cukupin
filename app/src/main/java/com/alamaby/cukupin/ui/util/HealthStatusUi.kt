package com.alamaby.cukupin.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.alamaby.cukupin.domain.model.BudgetHealthStatus
import com.alamaby.cukupin.ui.theme.AmberContainer
import com.alamaby.cukupin.ui.theme.AmberWarning
import com.alamaby.cukupin.ui.theme.BlueContainer
import com.alamaby.cukupin.ui.theme.BlueInfo
import com.alamaby.cukupin.ui.theme.GreenContainer
import com.alamaby.cukupin.ui.theme.GreenPrimary
import com.alamaby.cukupin.ui.theme.GreyContainer
import com.alamaby.cukupin.ui.theme.GreyMuted
import com.alamaby.cukupin.ui.theme.RedContainer
import com.alamaby.cukupin.ui.theme.RedDanger

/**
 * Presentasi status kesehatan dana: label, penjelasan, rekomendasi tindakan,
 * ikon, dan warna. Warna tidak pernah menjadi satu-satunya pembeda (FR-09).
 */
data class HealthStatusUi(
    val label: String,
    val description: String,
    val action: String,
    val icon: ImageVector,
    val containerColor: Color,
    val contentColor: Color
)

fun BudgetHealthStatus.toUi(): HealthStatusUi = when (this) {
    BudgetHealthStatus.NOT_STARTED -> HealthStatusUi(
        label = "Belum dimulai",
        description = "Periode target belum berjalan. Dana masih utuh.",
        action = "Siapkan rencana pengeluaran dari sekarang.",
        icon = Icons.Filled.Schedule,
        containerColor = BlueContainer,
        contentColor = BlueInfo
    )
    BudgetHealthStatus.SAFE -> HealthStatusUi(
        label = "Aman",
        description = "Pengeluaranmu jauh di bawah jalur rencana.",
        action = "Pertahankan pola belanjamu saat ini.",
        icon = Icons.Filled.CheckCircle,
        containerColor = GreenContainer,
        contentColor = GreenPrimary
    )
    BudgetHealthStatus.ON_TRACK -> HealthStatusUi(
        label = "Sesuai rencana",
        description = "Pengeluaranmu berada di sekitar jalur ideal.",
        action = "Jaga ritme belanja agar tetap di jalur.",
        icon = Icons.Filled.TrendingUp,
        containerColor = GreenContainer,
        contentColor = GreenPrimary
    )
    BudgetHealthStatus.WATCH -> HealthStatusUi(
        label = "Perlu perhatian",
        description = "Pengeluaranmu sedikit lebih cepat dari rencana.",
        action = "Kurangi pengeluaran yang tidak penting beberapa hari ini.",
        icon = Icons.Filled.Warning,
        containerColor = AmberContainer,
        contentColor = AmberWarning
    )
    BudgetHealthStatus.AT_RISK -> HealthStatusUi(
        label = "Berisiko",
        description = "Dengan pola ini, dana bisa habis sebelum waktunya.",
        action = "Batasi pengeluaran harian seminimal mungkin.",
        icon = Icons.Filled.Error,
        containerColor = RedContainer,
        contentColor = RedDanger
    )
    BudgetHealthStatus.DEPLETED -> HealthStatusUi(
        label = "Dana habis",
        description = "Saldo sudah habis padahal periode masih berjalan.",
        action = "Hentikan pengeluaran dari dana ini atau tambah dana.",
        icon = Icons.Filled.TrendingDown,
        containerColor = RedContainer,
        contentColor = RedDanger
    )
    BudgetHealthStatus.FINISHED_WITH_BALANCE -> HealthStatusUi(
        label = "Target tercapai",
        description = "Dana bertahan sampai akhir periode.",
        action = "Selamat! Buat target baru untuk periode berikutnya.",
        icon = Icons.Filled.EmojiEvents,
        containerColor = GreenContainer,
        contentColor = GreenPrimary
    )
    BudgetHealthStatus.FINISHED_EXCEEDED -> HealthStatusUi(
        label = "Melebihi target",
        description = "Periode selesai, tetapi pengeluaran melebihi dana.",
        action = "Tinjau pengeluaran terbesarmu, lalu buat target baru.",
        icon = Icons.Filled.TrendingDown,
        containerColor = GreyContainer,
        contentColor = GreyMuted
    )
}
