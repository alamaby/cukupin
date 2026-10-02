package com.alamaby.cukupin.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.alamaby.cukupin.domain.model.BudgetHealthStatus
import com.alamaby.cukupin.ui.util.toUi

/**
 * Kartu status kesehatan dana: ikon + label + penjelasan + rekomendasi.
 * Tidak mengandalkan warna saja (FR-09, NFR-06).
 */
@Composable
fun HealthStatusCard(
    status: BudgetHealthStatus,
    modifier: Modifier = Modifier
) {
    val ui = status.toUi()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Status: ${ui.label}. ${ui.description}" },
        colors = CardDefaults.cardColors(containerColor = ui.containerColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = ui.icon,
                contentDescription = null,
                tint = ui.contentColor,
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = ui.label,
                    style = MaterialTheme.typography.titleMedium,
                    color = ui.contentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = ui.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ui.contentColor
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = ui.action,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ui.contentColor
                )
            }
        }
    }
}

@Composable
fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(vertical = 8.dp)
    )
}

/** Baris label–nilai sederhana untuk ringkasan. */
@Composable
fun StatRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/** Baris info berikon umum. */
@Composable
fun InfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
