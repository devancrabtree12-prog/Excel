package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColumnType
import com.example.model.TrackerPlan
import com.example.model.toColumnLetter

@Composable
fun SpreadsheetGrid(
    plan: TrackerPlan,
    selectedCell: Pair<Int, Int>?,
    onCellSelected: (col: Int, row: Int) -> Unit,
    onCellDoubleTapped: (col: Int, row: Int) -> Unit,
    onDeleteRow: (rowIndex: Int) -> Unit,
    onAddColumn: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val horizontalScrollState = rememberScrollState()
    val rowNumberWidth = 44.dp
    val rowHeight = 44.dp
    val headerHeight = 56.dp

    // Calculate column widths
    val colWidths: List<Dp> = plan.headers.mapIndexed { colIdx, _ ->
        val colWidthUnits = plan.getColumnWidth(colIdx)
        (colWidthUnits * 9).coerceIn(120, 220).dp
    }

    val themeObj = com.example.model.SpreadsheetTheme.fromId(plan.theme)
    val headerBgColor = Color(themeObj.headerArgb)
    val headerBorderColor = headerBgColor.copy(alpha = 0.7f)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.horizontalScroll(horizontalScrollState)) {

            // Column Headers Row
            Row(
                modifier = Modifier
                    .background(headerBgColor)
                    .height(headerHeight),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Top-left corner cell (empty intersection above row numbers)
                Box(
                    modifier = Modifier
                        .width(rowNumberWidth)
                        .height(headerHeight)
                        .border(0.5.dp, headerBorderColor),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "№",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Header Cells
                plan.headers.forEachIndexed { colIdx, headerText ->
                    val colType = plan.getColumnType(colIdx)
                    val colLetter = toColumnLetter(colIdx)

                    Column(
                        modifier = Modifier
                            .width(colWidths[colIdx])
                            .height(headerHeight)
                            .border(0.5.dp, headerBorderColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Letter badge & Type Pill
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = colLetter,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                fontFamily = FontFamily.Monospace
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color.White.copy(alpha = 0.2f))
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "${colType.symbol} ${colType.label}",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(2.dp))

                        // Header Name
                        Text(
                            text = headerText,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                // Add Column button in header
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(headerHeight)
                        .border(0.5.dp, Color(0xFF0C5E31))
                        .clickable { onAddColumn() }
                        .padding(horizontal = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Column",
                            tint = Color.White.copy(alpha = 0.9f),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "+ Col",
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Row action spacer
                Box(modifier = Modifier.width(48.dp))
            }

            // Data Rows
            LazyColumn(
                modifier = Modifier.height((rowHeight * plan.sample_rows.size.coerceAtLeast(1).coerceAtMost(10)).coerceIn(180.dp, 440.dp))
            ) {
                itemsIndexed(plan.sample_rows) { rowIdx, rowData ->
                    val isZebra = rowIdx % 2 == 1
                    val rowNum = rowIdx + 2 // Row 1 was header
                    val isRowSelected = selectedCell?.second == rowIdx

                    Row(
                        modifier = Modifier
                            .height(rowHeight)
                            .background(
                                when {
                                    isRowSelected -> Color(0xFFE8F5E9)
                                    isZebra -> Color(0xFFF7FAF8)
                                    else -> Color.White
                                }
                            ),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Row Number Column
                        Box(
                            modifier = Modifier
                                .width(rowNumberWidth)
                                .height(rowHeight)
                                .background(Color(0xFFF1F5F2))
                                .border(0.5.dp, Color(0xFFE2E8E4)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rowNum",
                                color = Color(0xFF4A5568),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontFamily = FontFamily.Monospace
                            )
                        }

                        // Cells for each column
                        plan.headers.forEachIndexed { colIdx, _ ->
                            val cellRef = "${toColumnLetter(colIdx)}$rowNum"
                            val cellValue = rowData.getOrNull(colIdx) ?: ""
                            val colType = plan.getColumnType(colIdx)
                            val hasFormula = plan.getFormulaForCell(cellRef) != null || cellValue.startsWith("=")
                            val isCellSelected = selectedCell?.first == colIdx && selectedCell.second == rowIdx

                            Box(
                                modifier = Modifier
                                    .width(colWidths[colIdx])
                                    .height(rowHeight)
                                    .border(
                                        if (isCellSelected) 2.dp else 0.5.dp,
                                        if (isCellSelected) Color(0xFF107C41) else Color(0xFFE2E8E4)
                                    )
                                    .clickable {
                                        onCellSelected(colIdx, rowIdx)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = when (colType) {
                                    ColumnType.CURRENCY, ColumnType.PERCENT, ColumnType.NUMBER -> Alignment.CenterEnd
                                    ColumnType.DATE, ColumnType.STATUS -> Alignment.Center
                                    else -> Alignment.CenterStart
                                }
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Formula icon tag if formula
                                    if (hasFormula) {
                                        Box(
                                            modifier = Modifier
                                                .padding(end = 4.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(Color(0xFFE8F5E9))
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "fx",
                                                color = Color(0xFF107C41),
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = FontFamily.Monospace
                                            )
                                        }
                                    }

                                    // Formatted content or Status badge
                                    if (colType == ColumnType.STATUS && cellValue.isNotBlank()) {
                                        val statusColor = when {
                                            cellValue.contains("Paid", ignoreCase = true) ||
                                                    cellValue.contains("Done", ignoreCase = true) ||
                                                    cellValue.contains("Passed", ignoreCase = true) ||
                                                    cellValue.contains("Optimal", ignoreCase = true) ||
                                                    cellValue.contains("On Track", ignoreCase = true) ||
                                                    cellValue.contains("Approved", ignoreCase = true) -> Color(0xFF107C41)

                                            cellValue.contains("Pending", ignoreCase = true) ||
                                                    cellValue.contains("Progress", ignoreCase = true) ||
                                                    cellValue.contains("Low", ignoreCase = true) ||
                                                    cellValue.contains("Review", ignoreCase = true) -> Color(0xFFD97706)

                                            cellValue.contains("Critical", ignoreCase = true) ||
                                                    cellValue.contains("Action", ignoreCase = true) ||
                                                    cellValue.contains("Over", ignoreCase = true) ||
                                                    cellValue.contains("Failed", ignoreCase = true) -> Color(0xFFDC2626)

                                            else -> Color(0xFF4B5563)
                                        }

                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(statusColor.copy(alpha = 0.12f))
                                                .padding(horizontal = 8.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = cellValue,
                                                color = statusColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                maxLines = 1
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = formatDisplayValue(cellValue, colType, hasFormula),
                                            fontSize = 12.sp,
                                            fontFamily = if (hasFormula || colType == ColumnType.NUMBER || colType == ColumnType.CURRENCY || colType == ColumnType.PERCENT) FontFamily.Monospace else FontFamily.Default,
                                            color = if (hasFormula) Color(0xFF0F5132) else Color(0xFF1F2937),
                                            fontWeight = if (hasFormula || isCellSelected) FontWeight.SemiBold else FontWeight.Normal,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }

                        // Row action: delete row button
                        Box(
                            modifier = Modifier
                                .width(48.dp)
                                .height(rowHeight),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = { onDeleteRow(rowIdx) },
                                modifier = Modifier
                                    .size(28.dp)
                                    .testTag("delete_row_${rowIdx}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete row",
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatDisplayValue(raw: String, type: ColumnType, hasFormula: Boolean): String {
    if (raw.isBlank()) return ""
    if (hasFormula && raw.startsWith("=")) {
        return raw
    }
    return when (type) {
        ColumnType.CURRENCY -> {
            val num = raw.replace("$", "").replace(",", "").toDoubleOrNull()
            if (num != null) String.format("$%,.2f", num) else raw
        }
        ColumnType.PERCENT -> {
            val num = raw.replace("%", "").toDoubleOrNull()
            if (num != null) {
                if (num <= 1.0 && !raw.contains("%")) {
                    String.format("%.1f%%", num * 100.0)
                } else {
                    String.format("%.1f%%", num)
                }
            } else raw
        }
        ColumnType.NUMBER -> {
            val num = raw.replace(",", "").toDoubleOrNull()
            if (num != null) {
                if (num % 1.0 == 0.0) String.format("%,.0f", num) else String.format("%,.2f", num)
            } else raw
        }
        else -> raw
    }
}
