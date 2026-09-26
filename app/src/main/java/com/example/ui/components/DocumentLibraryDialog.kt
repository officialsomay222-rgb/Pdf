package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentCategory
import com.example.model.DocumentEntity
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun DocumentLibraryDialog(
    documents: List<DocumentEntity>,
    onSelectDocument: (DocumentEntity) -> Unit,
    onCreateNewDocument: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DocSurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = null,
                        tint = DocPrimaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Document OS Repository",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onCreateNewDocument,
                    colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("new_project_draft_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 11.sp, color = Color.White)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                Text(
                    text = "Select an architectural plan, legal contract, or inspection form:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(documents) { doc ->
                        val categoryColor = when (doc.category) {
                            DocumentCategory.BLUEPRINT.name -> Color(0xFF38BDF8)
                            DocumentCategory.CONTRACT.name -> Color(0xFFF59E0B)
                            DocumentCategory.INSPECTION_FORM.name -> Color(0xFF10B981)
                            else -> Color(0xFFA855F7)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(1.dp, DocBorderDark, RoundedCornerShape(8.dp))
                                .clickable { onSelectDocument(doc) }
                                .testTag("doc_repo_item_${doc.id}"),
                            colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(categoryColor.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Description,
                                        contentDescription = null,
                                        tint = categoryColor,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = doc.title,
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.weight(1f, fill = false)
                                        )
                                        if (doc.isPasswordProtected) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "Encrypted",
                                                tint = Color(0xFFF59E0B),
                                                modifier = Modifier.size(12.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "${doc.category} • ${doc.pageCount} sheets • ${doc.fileSizeFormatted}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = Color(0xFF94A3B8))
            }
        }
    )
}
