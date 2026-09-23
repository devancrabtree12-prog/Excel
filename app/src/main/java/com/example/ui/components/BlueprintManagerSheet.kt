package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.data.BlueprintEntity

enum class BlueprintCategoryFilter(val label: String) {
    ALL("All Blueprints"),
    MILITARY("🎖️ TacticalGrid Defense"),
    ENTERPRISE("🏢 QuantGrid Enterprise"),
    CUSTOM("⭐ Custom Saved")
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlueprintManagerSheet(
    blueprints: List<BlueprintEntity>,
    selectedBlueprint: BlueprintEntity?,
    isMilitaryMode: Boolean = false,
    onSelectBlueprint: (BlueprintEntity) -> Unit,
    onDeleteBlueprint: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var selectedFilter by remember {
        mutableStateOf(if (isMilitaryMode) BlueprintCategoryFilter.MILITARY else BlueprintCategoryFilter.ALL)
    }

    val displayBlueprints = remember(blueprints, isMilitaryMode, selectedFilter) {
        val filtered = when (selectedFilter) {
            BlueprintCategoryFilter.ALL -> blueprints
            BlueprintCategoryFilter.MILITARY -> blueprints.filter {
                it.domain.contains("Military", true) ||
                it.category.contains("Military", true) ||
                it.category.contains("Property", true) ||
                it.category.contains("Tactical", true) ||
                it.category.contains("Duty", true)
            }
            BlueprintCategoryFilter.ENTERPRISE -> blueprints.filter {
                !it.domain.contains("Military", true) &&
                !it.category.contains("Military", true) &&
                !it.category.contains("Property", true) &&
                !it.category.contains("Tactical", true) &&
                !it.category.contains("Duty", true)
            }
            BlueprintCategoryFilter.CUSTOM -> blueprints.filter { !it.isPrebuilt }
        }

        if (selectedFilter == BlueprintCategoryFilter.ALL) {
            if (isMilitaryMode) {
                // Emphasize tactical/military blueprints at the top in TacticalGrid mode
                filtered.sortedByDescending {
                    it.domain.contains("Military", true) ||
                    it.category.contains("Military", true) ||
                    it.category.contains("Property", true) ||
                    it.category.contains("Tactical", true) ||
                    it.category.contains("Duty", true)
                }
            } else {
                // Emphasize enterprise blueprints at the top in QuantGrid mode
                filtered.sortedBy {
                    it.domain.contains("Military", true) ||
                    it.category.contains("Military", true) ||
                    it.category.contains("Property", true) ||
                    it.category.contains("Tactical", true) ||
                    it.category.contains("Duty", true)
                }
            }
        } else {
            filtered
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isMilitaryMode) "TacticalGrid Blueprint & Mission Library" else "QuantGrid Enterprise Blueprint Library",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = if (isMilitaryMode) Color(0xFF2E3D29) else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isMilitaryMode) {
                            "Operational defense templates, PERSTAT matrices, and duty desk architectures"
                        } else {
                            "Pre-built financial, healthcare, sales, and HR models for 1-click workbook creation"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                BlueprintCategoryFilter.values().forEach { filter ->
                    FilterChip(
                        selected = selectedFilter == filter,
                        onClick = { selectedFilter = filter },
                        label = { Text(filter.label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = if (isMilitaryMode) Color(0xFF384A33) else MaterialTheme.colorScheme.primaryContainer,
                            selectedLabelColor = if (isMilitaryMode) Color.White else MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
            ) {
                items(displayBlueprints, key = { it.id }) { bp ->
                    val isSelected = bp.id == selectedBlueprint?.id
                    BlueprintCard(
                        blueprint = bp,
                        isSelected = isSelected,
                        isMilitaryContext = isMilitaryMode,
                        onSelect = {
                            onSelectBlueprint(bp)
                            onDismiss()
                        },
                        onDelete = { onDeleteBlueprint(bp.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlueprintCard(
    blueprint: BlueprintEntity,
    isSelected: Boolean,
    isMilitaryContext: Boolean = false,
    onSelect: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isMilitaryBlueprint = blueprint.domain.contains("Military", true) ||
            blueprint.category.contains("Military", true) ||
            blueprint.category.contains("Property", true) ||
            blueprint.category.contains("Tactical", true) ||
            blueprint.category.contains("Duty", true)

    val cardBorderColor = when {
        isSelected && isMilitaryBlueprint -> Color(0xFF8FBC8F)
        isSelected -> MaterialTheme.colorScheme.primary
        isMilitaryBlueprint -> Color(0xFF4A5D44)
        else -> MaterialTheme.colorScheme.outlineVariant
    }

    val cardBgColor = when {
        isSelected && isMilitaryBlueprint -> Color(0xFF263323)
        isSelected -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        isMilitaryBlueprint -> Color(0xFF1E281C).copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        border = BorderStroke(width = if (isSelected) 2.dp else 1.dp, color = cardBorderColor),
        modifier = modifier
            .fillMaxWidth()
            .testTag("blueprint_card_${blueprint.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = blueprint.title,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (isMilitaryBlueprint) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF384A33)
                        ) {
                            Text(
                                text = "TACTICAL",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color(0xFFA3B899),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    } else if (blueprint.isPrebuilt) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF0F5132).copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "QUANTGRID",
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 9.sp),
                                color = Color(0xFF0F5132),
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                if (!blueprint.isPrebuilt) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Badges: Category, Domain, Effectiveness
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = blueprint.category,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = blueprint.domain,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Score",
                        tint = Color(0xFFF59E0B),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = "${blueprint.effectivenessScore}% Score",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color(0xFFB45309)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = blueprint.notes,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Formulas used
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = "Formulas",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Key Formulas: ${blueprint.keyFormulas}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Button(
                onClick = onSelect,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("use_template_${blueprint.id}"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = if (isSelected) Icons.Default.Check else Icons.Default.AutoAwesome,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (isSelected) "Active Base Template" else "Use as Base Template",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
