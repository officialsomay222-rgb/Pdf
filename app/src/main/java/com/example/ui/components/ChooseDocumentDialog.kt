package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.engine.PdfEngine
import com.example.model.DocumentEntity
import com.example.model.ToolActionType
import com.example.ui.theme.CamScannerTeal
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChooseDocumentDialog(
    actionType: ToolActionType,
    documents: List<DocumentEntity>,
    onDismiss: () -> Unit,
    onDocumentSelected: (DocumentEntity) -> Unit,
    onMultipleDocumentsSelected: (List<String>) -> Unit = {},
    onWordFileImported: (uri: Uri) -> Unit = {}
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    val selectedMergeDocIds = remember { mutableStateListOf<String>() }
    var isInitializing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        isInitializing = false
    }

    // System File Picker for PDF / Word documents
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri: Uri? ->
            if (uri != null) {
                try {
                    var displayName = "Document_${System.currentTimeMillis() % 10000}.pdf"
                    var fileSize = 0L

                    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                        if (cursor.moveToFirst()) {
                            if (nameIndex != -1) cursor.getString(nameIndex)?.let { displayName = it }
                            if (sizeIndex != -1) fileSize = cursor.getLong(sizeIndex)
                        }
                    }

                    if (actionType == ToolActionType.WORD_OPEN) {
                        onWordFileImported(uri)
                        return@rememberLauncherForActivityResult
                    }

                    val cacheFile = File(context.cacheDir, "imported_${System.currentTimeMillis()}_$displayName")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val realPages = PdfEngine.getPdfPageCount(cacheFile.absolutePath)
                    val realLength = cacheFile.length().coerceAtLeast(fileSize)
                    val sizeMb = String.format(Locale.US, "%.1f MB", (realLength / (1024.0 * 1024.0)).coerceAtLeast(0.1))

                    val importedDoc = DocumentEntity(
                        id = "doc-imported-" + UUID.randomUUID().toString().take(8),
                        title = displayName.removeSuffix(".pdf").replace("_", " "),
                        category = "CONTRACT",
                        pageCount = realPages,
                        fileSizeFormatted = sizeMb,
                        createdAt = System.currentTimeMillis(),
                        modifiedAt = System.currentTimeMillis(),
                        filePath = cacheFile.absolutePath,
                        uriString = uri.toString()
                    )

                    if (actionType == ToolActionType.MERGE) {
                        selectedMergeDocIds.add(importedDoc.id)
                    } else {
                        onDocumentSelected(importedDoc)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open document: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val filteredDocs = remember(searchQuery, documents) {
        if (searchQuery.isBlank()) documents
        else documents.filter { it.title.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.85f)
                .clip(RoundedCornerShape(20.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(20.dp))
                .testTag("choose_document_dialog"),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
            ) {
                // Header with Action Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CamScannerTeal.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = when (actionType) {
                                    ToolActionType.WORD_OPEN -> Icons.Default.Article
                                    ToolActionType.PDF_TO_IMAGE -> Icons.Default.Collections
                                    ToolActionType.COMPRESS -> Icons.Default.Compress
                                    ToolActionType.MERGE -> Icons.Default.CallMerge
                                    ToolActionType.SPLIT -> Icons.Default.CallSplit
                                    ToolActionType.SIGN -> Icons.Default.Draw
                                    ToolActionType.LOCK -> Icons.Default.Lock
                                },
                                contentDescription = null,
                                tint = CamScannerTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Choose Document",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "For ${actionType.title}",
                                fontSize = 11.sp,
                                color = CamScannerTeal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Primary Action: Browse Device Storage
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val mimeTypes = if (actionType == ToolActionType.WORD_OPEN) {
                                arrayOf(
                                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                                    "application/msword",
                                    "text/plain",
                                    "*/*"
                                )
                            } else {
                                arrayOf("application/pdf")
                            }
                            filePickerLauncher.launch(mimeTypes)
                        }
                        .border(1.dp, CamScannerTeal.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                        .testTag("choose_doc_browse_storage"),
                    shape = RoundedCornerShape(12.dp),
                    color = CamScannerTeal.copy(alpha = 0.08f)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(CamScannerTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (actionType == ToolActionType.WORD_OPEN) Icons.Default.Description else Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (actionType == ToolActionType.WORD_OPEN) "Browse Device for Word (.docx / .doc)" else "Browse Device Storage / Files",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (actionType == ToolActionType.WORD_OPEN) "Select any .docx document from phone storage" else "Pick any PDF from Downloads, Drive, or SD Card",
                                fontSize = 10.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null,
                            tint = CamScannerTeal,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Search Bar for App Documents
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Filter your docs...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }, modifier = Modifier.size(20.dp)) {
                                Icon(Icons.Default.Clear, contentDescription = null, modifier = Modifier.size(14.dp))
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CamScannerTeal,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Section Label
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (actionType == ToolActionType.MERGE) "SELECT DOCUMENTS TO MERGE (${selectedMergeDocIds.size} SELECTED)" else "OR PICK FROM YOUR DOCUMENTS (${filteredDocs.size})",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 0.5.sp
                    )
                    if (actionType == ToolActionType.MERGE && selectedMergeDocIds.isNotEmpty()) {
                        TextButton(
                            onClick = { selectedMergeDocIds.clear() },
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Clear", fontSize = 10.sp, color = CamScannerTeal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Documents List
                if (isInitializing) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        repeat(4) {
                            DocumentCardSkeleton()
                        }
                    }
                } else if (filteredDocs.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.FindInPage, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(42.dp))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No matching documents found", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 12.sp)
                            Text("Use \"Browse Device Storage\" above", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), fontSize = 10.sp)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredDocs, key = { it.id }) { doc ->
                            val isSelected = selectedMergeDocIds.contains(doc.id)

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        if (actionType == ToolActionType.MERGE) {
                                            if (isSelected) selectedMergeDocIds.remove(doc.id)
                                            else selectedMergeDocIds.add(doc.id)
                                        } else {
                                            onDocumentSelected(doc)
                                        }
                                    }
                                    .border(
                                        width = if (isSelected) 1.5.dp else 0.5.dp,
                                        color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) CamScannerTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                                )
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (actionType == ToolActionType.MERGE) {
                                        Checkbox(
                                            checked = isSelected,
                                            onCheckedChange = { checked ->
                                                if (checked) selectedMergeDocIds.add(doc.id)
                                                else selectedMergeDocIds.remove(doc.id)
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = CamScannerTeal),
                                            modifier = Modifier.size(28.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .size(34.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.PictureAsPdf,
                                                contentDescription = null,
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(10.dp))
                                    }

                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = doc.title,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("${doc.pageCount} pgs", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text(doc.fileSizeFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("•", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            val dateStr = SimpleDateFormat("MMM dd", Locale.US).format(Date(doc.modifiedAt))
                                            Text(dateStr, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }

                                    if (actionType != ToolActionType.MERGE) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowForwardIos,
                                            contentDescription = "Select",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // If Merge action, show "Proceed with X Documents" bottom button
                if (actionType == ToolActionType.MERGE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (selectedMergeDocIds.size < 2) {
                                Toast.makeText(context, "Please select at least 2 documents to merge", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            onMultipleDocumentsSelected(selectedMergeDocIds.toList())
                        },
                        enabled = selectedMergeDocIds.size >= 2,
                        colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                    ) {
                        Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Merge Selected (${selectedMergeDocIds.size} Documents)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
