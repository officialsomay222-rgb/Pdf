package com.example.ui.components

import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.theme.CamScannerTeal
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

@Composable
fun PdfCompressorDialog(
    document: DocumentEntity? = null,
    allDocuments: List<DocumentEntity> = emptyList(),
    onDismiss: () -> Unit,
    onCompress: (document: DocumentEntity, quality: String) -> Unit
) {
    val context = LocalContext.current
    var currentDoc by remember(document) { mutableStateOf(document ?: allDocuments.firstOrNull()) }
    var selectedQuality by remember { mutableStateOf("MEDIUM") } // HIGH, MEDIUM, LOW
    var showDocSelector by remember { mutableStateOf(false) }

    // External PDF File Picker to upload ANY PDF from storage
    val pdfPickerLauncher = rememberLauncherForActivityResult(
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

                    // Copy to internal cache
                    val cacheFile = File(context.cacheDir, "compress_${System.currentTimeMillis()}_$displayName")
                    context.contentResolver.openInputStream(uri)?.use { input ->
                        FileOutputStream(cacheFile).use { output ->
                            input.copyTo(output)
                        }
                    }

                    val realPages = PdfEngine.getPdfPageCount(cacheFile.absolutePath)
                    val realLength = cacheFile.length().coerceAtLeast(fileSize)
                    val sizeMb = String.format(Locale.US, "%.1f MB", (realLength / (1024.0 * 1024.0)).coerceAtLeast(0.1))

                    currentDoc = DocumentEntity(
                        id = "doc-upload-" + System.currentTimeMillis(),
                        title = displayName.removeSuffix(".pdf").replace("_", " "),
                        category = "CONTRACT",
                        pageCount = realPages,
                        fileSizeFormatted = sizeMb,
                        createdAt = System.currentTimeMillis(),
                        modifiedAt = System.currentTimeMillis(),
                        filePath = cacheFile.absolutePath,
                        uriString = uri.toString()
                    )
                    Toast.makeText(context, "Loaded: $displayName", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Cannot read PDF: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    val reductionPct = when (selectedQuality) {
        "HIGH" -> 55
        "MEDIUM" -> 35
        else -> 20
    }

    var isInitializing by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(120)
        isInitializing = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CamScannerTeal.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Compress, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PDF Compressor", color = MaterialTheme.colorScheme.onSurface, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Reduce file size for fast sharing", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Document Selection / Upload Section
                if (isInitializing) {
                    PdfToolComponentSkeleton(height = 68.dp)
                } else if (currentDoc != null) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    currentDoc!!.title,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${currentDoc!!.fileSizeFormatted} • ${currentDoc!!.pageCount} pages",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontSize = 11.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = CamScannerTeal.copy(alpha = 0.16f)
                            ) {
                                Text(
                                    "~$reductionPct% Smaller",
                                    color = CamScannerTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Empty state when no doc is selected yet
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("No PDF selected", color = MaterialTheme.colorScheme.onSurface, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("Choose a PDF from your phone or app library", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Upload from Phone Storage Button (High Visibility)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                        modifier = Modifier.weight(1f).height(38.dp),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(15.dp), tint = CamScannerTeal)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Upload PDF", fontSize = 11.sp, color = CamScannerTeal, fontWeight = FontWeight.SemiBold)
                    }

                    if (allDocuments.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { showDocSelector = !showDocSelector },
                            modifier = Modifier.weight(1f).height(38.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(15.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Choose Doc", fontSize = 11.sp)
                        }
                    }
                }

                // App Docs Picker dropdown if opened
                if (showDocSelector && allDocuments.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 130.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp)
                        ) {
                            allDocuments.take(5).forEach { doc ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable {
                                            currentDoc = doc
                                            showDocSelector = false
                                        }
                                        .padding(horizontal = 8.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(doc.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1, modifier = Modifier.weight(1f))
                                    Text(doc.fileSizeFormatted, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text("COMPRESSION LEVEL", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    CompressionPresetItem(
                        title = "Extreme (~55% smaller)",
                        description = "Best for email & fast sending",
                        isSelected = selectedQuality == "HIGH",
                        onClick = { selectedQuality = "HIGH" }
                    )

                    CompressionPresetItem(
                        title = "Recommended (~35% smaller)",
                        description = "Balanced sharpness & size",
                        isSelected = selectedQuality == "MEDIUM",
                        onClick = { selectedQuality = "MEDIUM" }
                    )

                    CompressionPresetItem(
                        title = "Basic (~20% smaller)",
                        description = "Preserves high image detail",
                        isSelected = selectedQuality == "LOW",
                        onClick = { selectedQuality = "LOW" }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            currentDoc?.let { doc ->
                                onCompress(doc, selectedQuality)
                                onDismiss()
                            } ?: run {
                                Toast.makeText(context, "Please select or upload a PDF first", Toast.LENGTH_SHORT).show()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .height(42.dp)
                            .testTag("confirm_compress_pdf_button")
                    ) {
                        Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Compress", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompressionPresetItem(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(8.dp)
            )
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CamScannerTeal.copy(alpha = 0.10f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = CamScannerTeal),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(description, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
            }
        }
    }
}
