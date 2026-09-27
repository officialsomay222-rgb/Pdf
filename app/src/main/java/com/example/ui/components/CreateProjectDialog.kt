package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentCategory
import com.example.ui.theme.CamScannerTeal

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (title: String, category: DocumentCategory, sheetCount: Int) -> Unit
) {
    var title by remember { mutableStateOf("New Project Draft") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.BLUEPRINT) }
    var sheetCount by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surface,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = CamScannerTeal,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "New Document Project",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("new_project_title_input"),
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Category Preset:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    DocumentCategory.BLUEPRINT to "Blueprint / Plan",
                    DocumentCategory.CONTRACT to "Contract & Agreement",
                    DocumentCategory.INSPECTION_FORM to "Inspection & Form",
                    DocumentCategory.BLANK to "Blank Canvas"
                ).forEach { (cat, desc) ->
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CamScannerTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                            .border(
                                1.dp,
                                if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            colors = RadioButtonDefaults.colors(selectedColor = CamScannerTeal)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = cat.name.replace("_", " "),
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = desc,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Sheet Count
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Sheets: $sheetCount",
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        listOf(1, 2, 4, 8).forEach { count ->
                            val isCurrent = sheetCount == count
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isCurrent) CamScannerTeal else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { sheetCount = count }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onCreateProject(title.trim(), selectedCategory, sheetCount)
                        onDismiss()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create Project", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
