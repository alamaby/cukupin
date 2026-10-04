package com.alamaby.cukupin.ui.screens.createtarget

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.alamaby.cukupin.ui.components.StatRow
import com.alamaby.cukupin.ui.util.formatLong
import com.alamaby.cukupin.ui.util.formatRupiah
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTargetScreen(
    viewModel: CreateTargetViewModel,
    onTargetCreated: () -> Unit,
    onCancel: () -> Unit,
    showBackButton: Boolean
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.created) {
        if (state.created) onTargetCreated()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Target Dana") },
                navigationIcon = {
                    // Tombol batal hanya bermakna kalau layar ini punya tujuan untuk
                    // kembali. Saat Create Target menjadi layar awal (belum ada target
                    // aktif), tidak ada halaman sebelumnya di back stack, sehingga
                    // tombol back akan menggantung tanpa tujuan yang jelas.
                    if (showBackButton) {
                        IconButton(onClick = onCancel) {
                            Icon(Icons.Filled.ArrowBack, contentDescription = "Batal")
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            OutlinedTextField(
                value = state.name,
                onValueChange = viewModel::onNameChange,
                label = { Text("Nama target") },
                placeholder = { Text("cth. Uang saku Oktober") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = state.amountText,
                onValueChange = viewModel::onAmountChange,
                label = { Text("Dana awal (Rp)") },
                placeholder = { Text("cth. 1500000") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                supportingText = {
                    state.amount?.let { Text(formatRupiah(it)) }
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth()) {
                DateField(
                    label = "Tanggal mulai",
                    date = state.startDate,
                    onDateChange = viewModel::onStartDateChange,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.padding(6.dp))
                DateField(
                    label = "Tanggal selesai",
                    date = state.endDate,
                    onDateChange = viewModel::onEndDateChange,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(16.dp))

            // Ringkasan sebelum disimpan (FR-02).
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Ringkasan", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    StatRow(
                        label = "Durasi",
                        value = state.totalDays?.let { "$it hari" } ?: "–"
                    )
                    StatRow(
                        label = "Batas awal rata-rata",
                        value = state.initialDailyBudget?.let {
                            "${formatRupiah(it)} / hari"
                        } ?: "–"
                    )
                }
            }

            state.error?.let {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(24.dp))
            Button(
                onClick = viewModel::save,
                enabled = state.isValid && !state.isSaving,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isSaving) "Menyimpan…" else "Simpan Target")
            }
            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun DateField(
    label: String,
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val dialog = remember(date) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onDateChange(LocalDate.of(year, month + 1, day)) },
            date.year,
            date.monthValue - 1,
            date.dayOfMonth
        )
    }
    OutlinedTextField(
        value = date.formatLong(),
        onValueChange = {},
        readOnly = true,
        label = { Text(label) },
        trailingIcon = {
            IconButton(onClick = { dialog.show() }) {
                Icon(Icons.Filled.CalendarMonth, contentDescription = "Pilih $label")
            }
        },
        modifier = modifier
    )
}
