package com.example.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.BuildConfig

@Composable
fun ApiKeyDialog(
    currentCustomKey: String,
    onDismiss: () -> Unit,
    onSaveKey: (String) -> Unit
) {
    var keyText by remember { mutableStateOf(currentCustomKey) }
    val isBuildConfigKeyAvailable = BuildConfig.GEMINI_API_KEY.isNotBlank() &&
            BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY"

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Gemini AI Architecture Engine",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column {
                Text(
                    text = "The app uses Google's gemini-3.5-flash model to convert plain English spreadsheet descriptions into fully styled, formula-calculated Excel trackers.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (isBuildConfigKeyAvailable) {
                        "Status: Connected via AI Studio Secrets (BuildConfig)."
                    } else if (currentCustomKey.isNotBlank()) {
                        "Status: Connected using custom API key."
                    } else {
                        "Status: Built-in Intelligent Architecture Mode (offline & resilient). You can also provide an API key below:"
                    },
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isBuildConfigKeyAvailable || currentCustomKey.isNotBlank()) Color(0xFF107C41) else Color(0xFFD97706)
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = keyText,
                    onValueChange = { keyText = it },
                    label = { Text("Gemini API Key (Optional Override)") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("api_key_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveKey(keyText)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF107C41)),
                modifier = Modifier.testTag("save_api_key_button")
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_api_key_dialog")
            ) {
                Text("Close")
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
