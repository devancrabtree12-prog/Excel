package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun FeatureToggles(
    compactView: Boolean,
    onCompactViewChange: (Boolean) -> Unit,
    enableCharts: Boolean,
    onEnableChartsChange: (Boolean) -> Unit,
    enableAnalytics: Boolean,
    onEnableAnalyticsChange: (Boolean) -> Unit,
    liveDataEntry: Boolean,
    onLiveDataEntryChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "ADVANCED WORKBOOK FEATURES",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            FeatureItem(
                title = "Concise / Compact View",
                subtitle = "Condenses spacing & headers for high-density, print-ready views",
                checked = compactView,
                onCheckedChange = onCompactViewChange,
                testTag = "toggle_compact"
            )

            FeatureItem(
                title = "Pivot Tables & Summary Charts",
                subtitle = "Embeds visual Column, Line, and Pie charts on the Dashboard tab",
                checked = enableCharts,
                onCheckedChange = onEnableChartsChange,
                testTag = "toggle_charts"
            )

            FeatureItem(
                title = "Dedicated Analytics Page",
                subtitle = "Adds executive KPI summary cards with cross-sheet formula totals",
                checked = enableAnalytics,
                onCheckedChange = onEnableAnalyticsChange,
                testTag = "toggle_analytics"
            )

            FeatureItem(
                title = "Live Data Entry Mode",
                subtitle = "Extends pre-formatted rows with status dropdowns for continuous logging",
                checked = liveDataEntry,
                onCheckedChange = onLiveDataEntryChange,
                testTag = "toggle_live_data"
            )
        }
    }
}

@Composable
private fun FeatureItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.testTag(testTag)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
