package com.alamaby.cukupin.ui.screens.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alamaby.cukupin.domain.model.ExpenseTransaction
import com.alamaby.cukupin.domain.model.TargetLifecycleStatus
import com.alamaby.cukupin.ui.components.HealthStatusCard
import com.alamaby.cukupin.ui.components.SectionTitle
import com.alamaby.cukupin.ui.util.categoryIcon
import com.alamaby.cukupin.ui.util.formatDay
import com.alamaby.cukupin.ui.util.formatLong
import com.alamaby.cukupin.ui.util.formatRupiah
import com.alamaby.cukupin.ui.util.formatRupiahSigned

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onAddExpense: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    onCreateTarget: () -> Unit,
    onTargetCompleted: (String) -> Unit,
    onOpenSummary: (String) -> Unit,
    onOpenAbout: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val active = state.active

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = active?.target?.name ?: "Cukupin",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                actions = {
                    IconButton(onClick = onOpenAbout) {
                        Icon(
                            Icons.Filled.Info,
                            contentDescription = "Tentang aplikasi"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            if (active != null) {
                FloatingActionButton(onClick = onAddExpense) {
                    Icon(Icons.Filled.Add, contentDescription = "Catat pengeluaran")
                }
            }
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            active == null -> {
                NoActiveTargetContent(
                    latestName = state.latestTarget?.name,
                    latestCompleted = state.latestTarget?.lifecycleStatus ==
                        TargetLifecycleStatus.COMPLETED,
                    onCreateTarget = onCreateTarget,
                    onOpenSummary = {
                        state.latestTarget?.id?.let(onOpenSummary)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                DashboardContent(
                    state = state,
                    isPastDue = viewModel.isPastDue(),
                    onComplete = { viewModel.completeTarget(onTargetCompleted) },
                    onOpenHistory = onOpenHistory,
                    onOpenStatistics = onOpenStatistics,
                    modifier = Modifier.padding(padding)
                )
            }
        }
    }
}

@Composable
private fun NoActiveTargetContent(
    latestName: String?,
    latestCompleted: Boolean,
    onCreateTarget: () -> Unit,
    onOpenSummary: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (latestCompleted) {
            Text(
                "Target \"$latestName\" sudah selesai.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onOpenSummary) {
                Text("Lihat ringkasan")
            }
            Spacer(Modifier.height(16.dp))
        } else {
            Text(
                "Belum ada target aktif.",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Buat target agar uangmu terpantau sampai tanggal yang kamu tentukan.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
        }
        Button(onClick = onCreateTarget, modifier = Modifier.fillMaxWidth()) {
            Text("Buat Target Baru")
        }
    }
}

@Composable
private fun DashboardContent(
    state: DashboardUiState,
    isPastDue: Boolean,
    onComplete: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenStatistics: () -> Unit,
    modifier: Modifier = Modifier
) {
    val active = state.active ?: return
    val target = active.target
    val summary = active.summary

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "${target.startDate.formatLong()} – ${target.endDate.formatLong()}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        // 1. Saldo tersisa — informasi paling penting.
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.padding(20.dp)) {
                Text("Saldo tersisa", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = formatRupiah(summary.remainingBalance),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "dari ${formatRupiah(summary.totalFund)} • " +
                        "terpakai ${formatRupiah(summary.totalExpense)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        // 2 & 3. Hari tersisa + batas aman.
        Row(Modifier.fillMaxWidth()) {
            MiniStatCard(
                label = "Hari tersisa",
                value = "${summary.remainingDays} hari",
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(12.dp))
            MiniStatCard(
                label = "Batas aman",
                value = summary.currentDailyBudget?.let {
                    "${formatRupiah(it)}/hari"
                } ?: "–",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(Modifier.height(12.dp))

        // 4 & 5. Status + rekomendasi.
        HealthStatusCard(status = summary.healthStatus)
        Spacer(Modifier.height(12.dp))

        // Selisih terhadap jalur ideal.
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Posisi terhadap rencana", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                val diff = summary.differenceFromTarget
                Text(
                    text = if (diff >= 0) {
                        "${formatRupiah(diff)} lebih hemat dari jalur ideal"
                    } else {
                        "${formatRupiah(-diff)} lebih cepat dari jalur ideal"
                    },
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Pengeluaran hari ini: ${formatRupiah(state.todayExpense)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        if (isPastDue) {
            Button(onClick = onComplete, modifier = Modifier.fillMaxWidth()) {
                Text("Selesaikan Target")
            }
            Spacer(Modifier.height(12.dp))
        }

        Row(Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = onOpenHistory,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.History, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Riwayat")
            }
            Spacer(Modifier.width(12.dp))
            OutlinedButton(
                onClick = onOpenStatistics,
                modifier = Modifier.weight(1f)
            ) {
                Icon(Icons.Filled.BarChart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Statistik")
            }
        }

        SectionTitle("Transaksi terbaru")
        if (state.recentTransactions.isEmpty()) {
            Text(
                "Belum ada pengeluaran tercatat.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            state.recentTransactions.forEach { tx ->
                TransactionRow(
                    transaction = tx,
                    categoryName = state.categoryNames[tx.categoryId] ?: "Lainnya",
                    categoryIconKey = tx.categoryId
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun MiniStatCard(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(
                label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
fun TransactionRow(
    transaction: ExpenseTransaction,
    categoryName: String,
    categoryIconKey: String?,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = categoryIcon(categoryIconKey ?: "category"),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(categoryName, style = MaterialTheme.typography.titleMedium)
                transaction.note?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    transaction.transactionDate.formatDay(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                formatRupiahSigned(-transaction.amount),
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
