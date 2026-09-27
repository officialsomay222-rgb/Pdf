package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CamScannerTeal

@Composable
fun SecurityWatermarkDialog(
    currentWatermark: String,
    isPasswordProtected: Boolean,
    onSaveSecurity: (watermark: String, passwordProtected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var watermarkText by remember { mutableStateOf(currentWatermark) }
    var passwordEnabled by remember { mutableStateOf(isPasswordProtected) }
    var passwordValue by remember { mutableStateOf("DocSecurity2026") }
    var preventPrinting by remember { mutableStateOf(true) }
    var preventTextCopy by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CamScannerTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Security & Watermark",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Diagonal Security Watermark Input
                OutlinedTextField(
                    value = watermarkText,
                    onValueChange = { watermarkText = it },
                    label = { Text("Watermark Text", fontSize = 11.sp) },
                    placeholder = { Text("e.g. CONFIDENTIAL / DRAFT") },
                    modifier = Modifier.fillMaxWidth().testTag("watermark_text_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Document Encryption Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Password Protection",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Require passphrase to open document",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = passwordEnabled,
                        onCheckedChange = { passwordEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = CamScannerTeal),
                        modifier = Modifier.testTag("encryption_toggle_switch")
                    )
                }

                if (passwordEnabled) {
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = passwordValue,
                        onValueChange = { passwordValue = it },
                        label = { Text("Passphrase", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("encryption_password_input"),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Permissions Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "PERMISSIONS",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Checkbox(
                                checked = preventPrinting,
                                onCheckedChange = { preventPrinting = it },
                                colors = CheckboxDefaults.colors(checkedColor = CamScannerTeal)
                            )
                            Text("Disable Printing", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = preventTextCopy,
                                onCheckedChange = { preventTextCopy = it },
                                colors = CheckboxDefaults.colors(checkedColor = CamScannerTeal)
                            )
                            Text("Disable Text Extraction", color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSaveSecurity(watermarkText, passwordEnabled)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_security_button")
            ) {
                Text("Save", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
