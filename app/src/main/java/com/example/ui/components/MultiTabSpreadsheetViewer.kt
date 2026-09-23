package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.ViewColumn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ChartSpec
import com.example.model.ColorTheme
import com.example.model.ColumnSpec
import com.example.model.KpiCardSpec
import com.example.model.SheetPlan
import com.example.model.WorkbookPlan
import com.example.model.toColumnLetter

@Composable
fun MultiTabSpreadsheetViewer(
    workbook: WorkbookPlan,
    activeSheetIndex: Int,
    selectedCell: Pair<Int, Int>?,
    onSelectSheet: (Int) -> Unit,
    onSelectCell: (Int, Int) -> Unit,
    onUpdateCell: (col: Int, row: Int, newVal: String) -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    onDeleteColumn: (Int) -> Unit,
    onDeleteRow: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentSheet = workbook.sheets.getOrNull(activeSheetIndex) ?: workbook.sheets.firstOrNull() ?: return
    val theme = ColorTheme.fromLabel(workbook.theme)
    val primaryColor = parseHexColor(theme.primaryHex)

    var editingCell by remember { mutableStateOf<Triple<Int, Int, String>?>(null) }
    var activeDropdownCell by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 1. Tab Bar Switcher
            ScrollableTabRow(
                selectedTabIndex = activeSheetIndex.coerceIn(0, workbook.sheets.lastIndex),
                edgePadding = 8.dp,
                indicator = { tabPositions ->
                    if (activeSheetIndex in tabPositions.indices) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[activeSheetIndex]),
                            color = primaryColor
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("workbook_tabs")
            ) {
                workbook.sheets.forEachIndexed { index, sheet ->
                    val isSelected = index == activeSheetIndex
                    Tab(
                        selected = isSelected,
                        onClick = { onSelectSheet(index) },
                        modifier = Modifier.testTag("tab_${sheet.name.lowercase()}"),
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                val icon = when {
                                    sheet.isInstructions -> Icons.Default.Info
                                    sheet.isSummary -> Icons.Default.BarChart
                                    else -> Icons.Default.TableChart
                                }
                                Icon(
                                    imageVector = icon,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = sheet.name,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            // 2. Active Tab Content
            when {
                currentSheet.isInstructions -> {
                    InstructionsTabView(
                        sheet = currentSheet,
                        workbookTitle = workbook.title,
                        domain = workbook.domain,
                        primaryColor = primaryColor
                    )
                }
                currentSheet.isSummary -> {
                    DashboardTabView(
                        sheet = currentSheet,
                        workbookTitle = workbook.title,
                        primaryColor = primaryColor
                    )
                }
                else -> {
                    DataSheetTabView(
                        sheet = currentSheet,
                        selectedCell = selectedCell,
                        primaryColor = primaryColor,
                        onSelectCell = onSelectCell,
                        onEditCell = { col, row, currentVal ->
                            editingCell = Triple(col, row, currentVal)
                        },
                        onAddRow = onAddRow,
                        onAddColumn = onAddColumn,
                        onDeleteColumn = onDeleteColumn,
                        onDeleteRow = onDeleteRow,
                        onDropdownOptionSelected = { col, row, opt ->
                            onUpdateCell(col, row, opt)
                        }
                    )
                }
            }
        }
    }

    // Cell Edit Dialog
    editingCell?.let { (col, row, currentVal) ->
        val colSpec = currentSheet.columns.getOrNull(col)
        val colLetter = toColumnLetter(col)
        val cellAddress = "$colLetter${row + 2}"

        CellEditDialog(
            cellAddress = cellAddress,
            currentValue = currentVal,
            onConfirm = { newVal ->
                onUpdateCell(col, row, newVal)
                editingCell = null
            },
            onDismiss = { editingCell = null }
        )
    }
}

@Composable
private fun InstructionsTabView(
    sheet: SheetPlan,
    workbookTitle: String,
    domain: String,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = primaryColor.copy(alpha = 0.1f),
            border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.3f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = primaryColor,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "$workbookTitle Instructions",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = primaryColor
                    )
                    Text(
                        text = "Domain Protocol: $domain",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        val text = sheet.instructionsText ?: "Welcome to your custom tracker."
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                text.lines().forEach { line ->
                    val trimmed = line.trim()
                    if (trimmed.startsWith("1.") || trimmed.startsWith("2.") || trimmed.startsWith("3.") || trimmed.endsWith(":")) {
                        Text(
                            text = trimmed,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = primaryColor,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else if (trimmed.startsWith("•") || trimmed.startsWith("-")) {
                        Row(modifier = Modifier.padding(start = 8.dp)) {
                            Text(
                                text = "• ",
                                color = primaryColor,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = trimmed.removePrefix("•").removePrefix("-").trim(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else if (trimmed.isNotBlank()) {
                        Text(
                            text = trimmed,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTabView(
    sheet: SheetPlan,
    workbookTitle: String,
    primaryColor: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "EXECUTIVE KPI SUMMARY DECK",
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            ),
            color = primaryColor
        )

        // KPI Cards Grid
        if (sheet.kpiCards.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sheet.kpiCards.chunked(2).forEach { rowCards ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowCards.forEach { kpi ->
                            KpiCardItem(
                                kpi = kpi,
                                primaryColor = primaryColor,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowCards.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Charts Section
        if (sheet.charts.isNotEmpty()) {
            Text(
                text = "SUMMARY ANALYTICS & VISUAL FUNNELS",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                color = primaryColor,
                modifier = Modifier.padding(top = 6.dp)
            )

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                sheet.charts.forEach { chart ->
                    ChartCardItem(
                        chart = chart,
                        primaryColor = primaryColor
                    )
                }
            }
        }
    }
}

@Composable
private fun KpiCardItem(
    kpi: KpiCardSpec,
    primaryColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = primaryColor.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = kpi.label,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = kpi.value ?: "Auto-calculated",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = primaryColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Functions,
                    contentDescription = null,
                    tint = primaryColor.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = kpi.formula,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
private fun ChartCardItem(
    chart: ChartSpec,
    primaryColor: Color
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val chartIcon = when (chart.type.lowercase()) {
                "pie" -> Icons.Default.PieChart
                "line" -> Icons.Default.ShowChart
                else -> Icons.Default.BarChart
            }
            Surface(
                shape = CircleShape,
                color = primaryColor.copy(alpha = 0.12f),
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = chartIcon,
                        contentDescription = chart.type,
                        tint = primaryColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = chart.title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Type: ${chart.type.capitalize()} Chart • Data Range: ${chart.range}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DataSheetTabView(
    sheet: SheetPlan,
    selectedCell: Pair<Int, Int>?,
    primaryColor: Color,
    onSelectCell: (Int, Int) -> Unit,
    onEditCell: (col: Int, row: Int, currentVal: String) -> Unit,
    onAddRow: () -> Unit,
    onAddColumn: () -> Unit,
    onDeleteColumn: (Int) -> Unit,
    onDeleteRow: (Int) -> Unit,
    onDropdownOptionSelected: (col: Int, row: Int, opt: String) -> Unit
) {
    val horizontalScroll = rememberScrollState()

    Column(modifier = Modifier.fillMaxWidth()) {
        // Toolbar: Add Row, Add Column, Column count, Row count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${sheet.columns.size} Columns • ${sheet.rows.size} Rows",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                OutlinedButton(
                    onClick = onAddColumn,
                    contentPadding = ButtonDefaults.ContentPadding,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp).testTag("grid_add_column_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Add Column", fontSize = 11.sp)
                }

                OutlinedButton(
                    onClick = onAddRow,
                    contentPadding = ButtonDefaults.ContentPadding,
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(30.dp).testTag("grid_add_row_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Add Row", fontSize = 11.sp)
                }
            }
        }

        // Spreadsheet Grid Table
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(horizontalScroll)
        ) {
            // Header Row
            Row(
                modifier = Modifier
                    .background(primaryColor)
                    .height(36.dp)
            ) {
                // Row index header cell
                Box(
                    modifier = Modifier
                        .width(44.dp)
                        .background(primaryColor.copy(alpha = 0.85f))
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "#",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                sheet.columns.forEachIndexed { cIdx, col ->
                    val colLetter = toColumnLetter(cIdx)
                    Row(
                        modifier = Modifier
                            .widthIn(min = 130.dp)
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "$colLetter: ${col.header}",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                            if (col.validation != null) {
                                Text(
                                    text = "Dropdown ▾",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 9.sp
                                )
                            }
                        }

                        if (sheet.columns.size > 1) {
                            IconButton(
                                onClick = { onDeleteColumn(cIdx) },
                                modifier = Modifier.size(18.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Delete Column",
                                    tint = Color.White.copy(alpha = 0.7f),
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Data Rows
            sheet.rows.forEachIndexed { rIdx, row ->
                val rowNum = rIdx + 2
                val isZebra = rIdx % 2 == 1
                val rowBg = if (isZebra) MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface

                Row(
                    modifier = Modifier
                        .background(rowBg)
                        .height(34.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Row index cell
                    Box(
                        modifier = Modifier
                            .width(44.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .padding(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "$rowNum",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }

                    sheet.columns.forEachIndexed { cIdx, col ->
                        val isSelected = selectedCell?.first == cIdx && selectedCell?.second == rIdx
                        val rawVal = row.getOrNull(cIdx).orEmpty()
                        var menuOpen by remember { mutableStateOf(false) }

                        Box(
                            modifier = Modifier
                                .widthIn(min = 130.dp)
                                .border(
                                    width = if (isSelected) 2.dp else 0.5.dp,
                                    color = if (isSelected) primaryColor else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    onSelectCell(cIdx, rIdx)
                                    if (col.validation != null) {
                                        menuOpen = true
                                    } else {
                                        onEditCell(cIdx, rIdx, rawVal)
                                    }
                                }
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = rawVal,
                                    fontSize = 12.sp,
                                    fontFamily = if (rawVal.startsWith("=")) FontFamily.Monospace else FontFamily.Default,
                                    color = if (rawVal.startsWith("=")) primaryColor else MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1,
                                    modifier = Modifier.weight(1f, fill = false)
                                )

                                if (col.validation != null) {
                                    Text(
                                        text = "▾",
                                        fontSize = 10.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )

                                    DropdownMenu(
                                        expanded = menuOpen,
                                        onDismissRequest = { menuOpen = false }
                                    ) {
                                        col.validation.forEach { option ->
                                            DropdownMenuItem(
                                                text = { Text(option) },
                                                onClick = {
                                                    onDropdownOptionSelected(cIdx, rIdx, option)
                                                    menuOpen = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    val clean = hex.removePrefix("#")
    val colorInt = clean.toLong(16).toInt()
    return Color(0xFF000000 or colorInt.toLong())
}
