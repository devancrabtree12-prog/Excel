package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Functions
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackerPlan
import com.example.model.toColumnLetter

@Composable
fun FormulaBar(
    plan: TrackerPlan,
    selectedCell: Pair<Int, Int>?,
    onEditClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (colIdx, rowIdx) = selectedCell ?: Pair(0, 0)
    val cellRef = "${toColumnLetter(colIdx)}${rowIdx + 2}" // 2 because row 1 is header
    val cellValue = plan.sample_rows.getOrNull(rowIdx)?.getOrNull(colIdx) ?: ""
    val formula = plan.getFormulaForCell(cellRef) ?: if (cellValue.startsWith("=")) cellValue else null
    val displayContent = formula ?: cellValue.ifEmpty { "(empty cell)" }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Cell address badge (e.g. "E2")
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(Color(0xFF107C41))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = cellRef,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // fx symbol
        Icon(
            imageVector = Icons.Default.Functions,
            contentDescription = "Formula indicator",
            tint = if (formula != null) Color(0xFF107C41) else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(end = 4.dp)
        )

        // Formula / value text
        Text(
            text = displayContent,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            fontSize = 13.sp,
            fontFamily = if (formula != null) FontFamily.Monospace else FontFamily.Default,
            color = if (formula != null) Color(0xFF0D5C31) else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (formula != null) FontWeight.SemiBold else FontWeight.Normal,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Edit button
        IconButton(
            onClick = onEditClicked,
            modifier = Modifier.testTag("edit_cell_button")
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit cell value",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}
