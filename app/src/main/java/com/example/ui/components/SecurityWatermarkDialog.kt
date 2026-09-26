package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
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
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun SecurityWatermarkDialog(
    currentWatermark: String,
    isPasswordProtected: Boolean,
    onSaveSecurity: (watermark: String, passwordProtected: Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    var watermarkText by remember { mutableStateOf(currentWatermark) }
    var passwordEnabled by remember { mutableStateOf(isPasswordProtected) }
    var passwordValue by remember { mutableStateOf("EnterpriseDoc2026!") }
    var preventPrinting by remember { mutableStateOf(true) }
    var preventTextCopy by remember { mutableStateOf(true) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DocSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = DocPrimaryCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Enterprise Security & Watermark",
                    color = Color.White,
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
                    label = { Text("Diagonal Security Watermark Text", fontSize = 11.sp) },
                    placeholder = { Text("e.g. CONFIDENTIAL - STAGE 4 BID") },
                    modifier = Modifier.fillMaxWidth().testTag("watermark_text_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DocPrimaryCyan,
                        unfocusedBorderColor = DocBorderDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // AES-256 Military Encryption Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "AES-256 Bit Document Encryption",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Require cryptographic passphrase to open",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                    }
                    Switch(
                        checked = passwordEnabled,
                        onCheckedChange = { passwordEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = DocPrimaryCyan),
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
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DocPrimaryCyan,
                            unfocusedBorderColor = DocBorderDark
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Granular Permissions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Text(
                            text = "RESTRICTED PERMISSIONS",
                            color = Color(0xFF94A3B8),
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
                                colors = CheckboxDefaults.colors(checkedColor = DocPrimaryCyan)
                            )
                            Text("Disable High-Res Printing", color = Color.White, fontSize = 11.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = preventTextCopy,
                                onCheckedChange = { preventTextCopy = it },
                                colors = CheckboxDefaults.colors(checkedColor = DocPrimaryCyan)
                            )
                            Text("Disable Vector & Text Extraction", color = Color.White, fontSize = 11.sp)
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
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                modifier = Modifier.testTag("save_security_button")
            ) {
                Text("Save Policies", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
