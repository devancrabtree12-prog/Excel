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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.TrackerPlan
import com.example.model.toColumnLetter

@Composable
fun CellEditDialog(
    plan: TrackerPlan,
    cellCoords: Pair<Int, Int>,
    onDismiss: () -> Unit,
    onSave: (newValue: String) -> Unit
) {
    val (colIdx, rowIdx) = cellCoords
    val cellRef = "${toColumnLetter(colIdx)}${rowIdx + 2}"
    val initialValue = plan.sample_rows.getOrNull(rowIdx)?.getOrNull(colIdx) ?: ""
    val formula = plan.getFormulaForCell(cellRef)
    val colHeader = plan.headers.getOrNull(colIdx) ?: "Column"

    var textValue by remember { mutableStateOf(formula ?: initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Edit Cell $cellRef",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "Column: $colHeader",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Cell Value or =Formula") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cell_input_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Formula helper chips
                Text(
                    text = "Quick formula starters:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { textValue = "=SUM(" },
                        label = { Text("=SUM", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    SuggestionChip(
                        onClick = { textValue = "=AVERAGE(" },
                        label = { Text("=AVG", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    SuggestionChip(
                        onClick = { textValue = "=IF(" },
                        label = { Text("=IF", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(textValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                modifier = Modifier.testTag("save_cell_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_cell_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun CellEditDialog(
    cellAddress: String,
    currentValue: String,
    onConfirm: (newValue: String) -> Unit,
    onDismiss: () -> Unit
) {
    var textValue by remember { mutableStateOf(currentValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Edit Cell $cellAddress",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = textValue,
                    onValueChange = { textValue = it },
                    label = { Text("Cell Value or =Formula") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("cell_input_field")
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Quick formula starters:",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SuggestionChip(
                        onClick = { textValue = "=SUM(" },
                        label = { Text("=SUM", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    SuggestionChip(
                        onClick = { textValue = "=AVERAGE(" },
                        label = { Text("=AVG", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                    SuggestionChip(
                        onClick = { textValue = "=IF(" },
                        label = { Text("=IF", fontSize = 11.sp, fontFamily = FontFamily.Monospace) }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(textValue)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                modifier = Modifier.testTag("save_cell_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("cancel_cell_button")
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
