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
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun CreateProjectDialog(
    onDismiss: () -> Unit,
    onCreateProject: (title: String, category: DocumentCategory, sheetCount: Int) -> Unit
) {
    var title by remember { mutableStateOf("New Architectural Draft") }
    var selectedCategory by remember { mutableStateOf(DocumentCategory.BLUEPRINT) }
    var sheetCount by remember { mutableStateOf(3) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DocSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null,
                    tint = DocPrimaryCyan,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Initialize New Project",
                    color = Color.White,
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
                    label = { Text("Project / Document Title", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("new_project_title_input"),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = DocPrimaryCyan,
                        unfocusedBorderColor = DocBorderDark
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Project Category & Engine Mode:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                listOf(
                    DocumentCategory.BLUEPRINT to "Architectural CAD Blueprint (with 1/4\" scale grid)",
                    DocumentCategory.CONTRACT to "Master Legal Contract & Agreement",
                    DocumentCategory.INSPECTION_FORM to "Municipal Inspection & Compliance Form",
                    DocumentCategory.BLANK to "Blank Vector Canvas"
                ).forEach { (cat, desc) ->
                    val isSelected = selectedCategory == cat
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) DocPrimaryCyan.copy(alpha = 0.2f) else DocSurfaceCardDark)
                            .border(1.dp, if (isSelected) DocPrimaryCyan else DocBorderDark, RoundedCornerShape(6.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            colors = RadioButtonDefaults.colors(selectedColor = DocPrimaryCyan)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = cat.name.replace("_", " "),
                                color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                            Text(
                                text = desc,
                                color = Color(0xFF94A3B8),
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
                        text = "Initial Sheets: $sheetCount",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Row {
                        listOf(1, 2, 4, 8).forEach { count ->
                            val isCurrent = sheetCount == count
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isCurrent) DocPrimaryCyan else DocSurfaceCardDark)
                                    .clickable { sheetCount = count }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "$count",
                                    color = if (isCurrent) Color.White else Color(0xFF94A3B8),
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
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                modifier = Modifier.testTag("confirm_create_project_button")
            ) {
                Text("Create Project", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
