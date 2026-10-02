package com.alamaby.cukupin.ui.screens.summary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.alamaby.cukupin.ui.components.HealthStatusCard
import com.alamaby.cukupin.ui.components.StatRow
import com.alamaby.cukupin.ui.util.formatLong
import com.alamaby.cukupin.ui.util.formatRupiah

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TargetSummaryScreen(
    viewModel: TargetSummaryViewModel,
    onRepeatDone: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.repeated) {
        if (state.repeated) onRepeatDone()
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Ringkasan Target") }) }
    ) { padding ->
        val target = state.target
        val summary = state.summary
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
            target == null || summary == null -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) { Text("Target tidak ditemukan.") }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    Text(
                        text = target.name,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Text(
                        text = "${target.startDate.formatLong()} – ${target.endDate.formatLong()}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))

                    HealthStatusCard(status = summary.healthStatus)
                    Spacer(Modifier.height(12.dp))

                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Hasil akhir", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            StatRow("Dana awal", formatRupiah(summary.totalFund))
                            StatRow("Total pengeluaran", formatRupiah(summary.totalExpense))
                            StatRow("Saldo akhir", formatRupiah(summary.remainingBalance))
                            StatRow(
                                "Rata-rata harian",
                                summary.averageDailyExpense?.let { formatRupiah(it) } ?: "–"
                            )
                            StatRow(
                                "Kategori terbesar",
                                "${state.biggestCategoryName} " +
                                    "(${formatRupiah(state.biggestCategoryTotal)})"
                            )
                            state.biggestDay?.let { (date, total) ->
                                StatRow(
                                    "Hari pengeluaran terbesar",
                                    "${date.formatLong()} (${formatRupiah(total)})"
                                )
                            }
                            StatRow(
                                "Hari tanpa pengeluaran",
                                "${state.zeroExpenseDays} hari"
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = if (state.isSuccess) {
                                    "Target berhasil — dana bertahan sampai akhir periode."
                                } else {
                                    "Pengeluaran melebihi dana sebesar " +
                                        formatRupiah(-summary.remainingBalance) + "."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    state.error?.let {
                        Spacer(Modifier.height(12.dp))
                        Text(
                            it,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    Spacer(Modifier.height(24.dp))
                    Button(
                        onClick = viewModel::repeatTarget,
                        enabled = !state.isRepeating,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (state.isRepeating) "Membuat…" else "Buat Target Baru")
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Target baru menyalin nama, dana, dan durasi. " +
                            "Transaksi lama tidak ikut disalin.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
