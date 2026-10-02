package com.alamaby.cukupin.ui.screens.statistics

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.alamaby.cukupin.domain.model.PredictionConfidence
import com.alamaby.cukupin.domain.usecase.BudgetStatistics
import com.alamaby.cukupin.ui.components.HealthStatusCard
import com.alamaby.cukupin.ui.components.SectionTitle
import com.alamaby.cukupin.ui.components.StatRow
import com.alamaby.cukupin.ui.util.categoryIcon
import com.alamaby.cukupin.ui.util.formatLong
import com.alamaby.cukupin.ui.util.formatRupiah
import com.alamaby.cukupin.ui.util.formatRupiahSigned

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistik") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Kembali")
                    }
                }
            )
        }
    ) { padding ->
        val stats = state.stats
        when {
            state.isLoading -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { CircularProgressIndicator() }
            }
            stats == null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { Text("Belum ada target aktif.") }
            }
            else -> {
                StatisticsContent(stats = stats, state = state, modifier = Modifier.padding(padding))
            }
        }
    }
}

@Composable
private fun StatisticsContent(
    stats: BudgetStatistics,
    state: StatisticsUiState,
    modifier: Modifier = Modifier
) {
    val summary = stats.summary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        HealthStatusCard(status = summary.healthStatus)
        Spacer(Modifier.height(12.dp))

        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Ringkasan", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                StatRow("Total pengeluaran", formatRupiah(summary.totalExpense))
                StatRow(
                    "Rata-rata harian",
                    summary.averageDailyExpense?.let { formatRupiah(it) } ?: "–"
                )
                StatRow("Batas awal", "${formatRupiah(summary.initialDailyBudget)}/hari")
                StatRow(
                    "Batas aman terbaru",
                    summary.currentDailyBudget?.let { "${formatRupiah(it)}/hari" } ?: "–"
                )
                StatRow(
                    "Selisih dari rencana",
                    formatRupiahSigned(summary.differenceFromTarget)
                )
                StatRow("Hari tanpa pengeluaran", "${stats.zeroExpenseDays} hari")
                stats.biggestDay?.let { (date, total) ->
                    StatRow("Pengeluaran terbesar", "${date.formatLong()} (${formatRupiah(total)})")
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionTitle("Pengeluaran kumulatif vs jalur ideal")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                CumulativeChart(stats = stats)
                Spacer(Modifier.height(8.dp))
                LegendRow()
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionTitle("Pengeluaran per kategori")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                if (stats.perCategory.isEmpty()) {
                    Text("Belum ada pengeluaran tercatat.")
                } else {
                    val max = stats.perCategory.maxOf { it.total }.coerceAtLeast(1L)
                    stats.perCategory.forEach { item ->
                        val category = state.categories[item.categoryId]
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIcon(
                                    category?.iconKey ?: "category"
                                ),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Row(
                                    Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        category?.name ?: "Lainnya",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    Text(
                                        formatRupiah(item.total),
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                LinearProgressIndicator(
                                    progress = { item.total.toFloat() / max.toFloat() },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))

        SectionTitle("Perkiraan daya tahan")
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                val runOut = summary.estimatedRunOutDate
                if (runOut != null) {
                    Text(
                        "Dengan rata-rata saat ini, dana diperkirakan habis " +
                            "sekitar ${runOut.formatLong()}.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Tingkat keyakinan: ${summary.predictionConfidence.toLabel()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "Ini perkiraan, bukan kepastian. Kelengkapan catatan " +
                            "memengaruhi hasilnya.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        "Belum ada cukup data untuk membuat perkiraan.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

private fun PredictionConfidence.toLabel(): String = when (this) {
    PredictionConfidence.UNAVAILABLE -> "belum tersedia"
    PredictionConfidence.EARLY -> "awal"
    PredictionConfidence.DEVELOPING -> "berkembang"
    PredictionConfidence.ESTABLISHED -> "kuat"
}

@Composable
private fun LegendRow() {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.height(4.dp).width(24.dp)) {
                drawLine(
                    color = primary,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("Aktual", style = MaterialTheme.typography.bodyMedium)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Canvas(Modifier.height(4.dp).width(24.dp)) {
                drawLine(
                    color = outline,
                    start = Offset(0f, size.height / 2),
                    end = Offset(size.width, size.height / 2),
                    strokeWidth = size.height,
                    cap = StrokeCap.Round
                )
            }
            Spacer(Modifier.width(6.dp))
            Text("Jalur ideal", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun CumulativeChart(stats: BudgetStatistics) {
    val primary = MaterialTheme.colorScheme.primary
    val outline = MaterialTheme.colorScheme.outline
    val points = stats.cumulative
    if (points.isEmpty()) return

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
    ) {
        val maxValue = maxOf(
            points.maxOf { it.actualCumulative },
            points.maxOf { it.idealCumulative },
            1L
        ).toFloat()
        val n = points.size
        fun x(i: Int): Float =
            if (n == 1) size.width / 2 else i.toFloat() / (n - 1) * size.width
        fun y(v: Long): Float =
            size.height - (v.toFloat() / maxValue) * (size.height - 8.dp.toPx()) - 4.dp.toPx()

        // Jalur ideal (garis putus-putus sederhana: segmen pendek).
        val idealPath = Path().apply {
            points.forEachIndexed { i, p ->
                val px = x(i)
                val py = y(p.idealCumulative)
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        drawPath(idealPath, color = outline, style = Stroke(width = 3.dp.toPx()))

        // Kumulatif aktual.
        val actualPath = Path().apply {
            points.forEachIndexed { i, p ->
                val px = x(i)
                val py = y(p.actualCumulative)
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
        }
        drawPath(
            actualPath,
            color = primary,
            style = Stroke(width = 5.dp.toPx(), cap = StrokeCap.Round)
        )
    }
}
