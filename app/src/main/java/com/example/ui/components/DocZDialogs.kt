package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DocumentEntity
import com.example.ui.theme.CamScannerTeal
import com.example.ui.theme.CamScannerTealLight

// -------------------------------------------------------------
// 1. ID CARD SCANNER DIALOG (2-IN-1 FRONT + BACK MERGE)
// -------------------------------------------------------------

@Composable
fun IdCardScannerDialog(
    onDismiss: () -> Unit,
    onCreateIdCard: (title: String, frontBmp: Bitmap, backBmp: Bitmap?) -> Unit
) {
    val context = LocalContext.current
    var docTitle by remember { mutableStateOf("ID_Card_Scan_${System.currentTimeMillis() % 10000}") }
    var frontBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var backBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var activeTarget by remember { mutableStateOf("FRONT") } // FRONT or BACK

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bmp ->
            if (bmp != null) {
                if (activeTarget == "FRONT") frontBitmap = bmp
                else backBitmap = bmp
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            if (activeTarget == "FRONT") frontBitmap = bmp
                            else backBitmap = bmp
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                            Icon(Icons.Default.Badge, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("ID Card 2-in-1 Scan", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Standard A4 1:1 format", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = docTitle,
                    onValueChange = { docTitle = it },
                    label = { Text("Document Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // FRONT CARD SLOT
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (frontBitmap != null) 1.5.dp else 1.dp,
                            color = if (frontBitmap != null) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    if (frontBitmap != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = frontBitmap!!.asImageBitmap(),
                                contentDescription = "Front Side",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CamScannerTeal)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("FRONT SIDE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { frontBitmap = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("FRONT SIDE OF ID", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        activeTarget = "FRONT"
                                        cameraLauncher.launch(null)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Camera", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        activeTarget = "FRONT"
                                        galleryLauncher.launch("image/*")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gallery", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // BACK CARD SLOT
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .border(
                            width = if (backBitmap != null) 1.5.dp else 1.dp,
                            color = if (backBitmap != null) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    if (backBitmap != null) {
                        Box(modifier = Modifier.fillMaxSize()) {
                            Image(
                                bitmap = backBitmap!!.asImageBitmap(),
                                contentDescription = "Back Side",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .padding(8.dp)
                                    .align(Alignment.TopStart)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CamScannerTeal)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("BACK SIDE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
                            IconButton(
                                onClick = { backBitmap = null },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .size(24.dp)
                                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = Color.White, modifier = Modifier.size(14.dp))
                            }
                        }
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize().padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("BACK SIDE OF ID (Optional)", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = {
                                        activeTarget = "BACK"
                                        cameraLauncher.launch(null)
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Camera", fontSize = 11.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        activeTarget = "BACK"
                                        galleryLauncher.launch("image/*")
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gallery", fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Button(
                    onClick = {
                        val front = frontBitmap
                        if (front == null) {
                            Toast.makeText(context, "Please scan the Front side first", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        onCreateIdCard(docTitle.trim().ifBlank { "ID_Card_Scan" }, front, backBitmap)
                    },
                    enabled = frontBitmap != null,
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("id_card_create_button")
                ) {
                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Assemble ID Card PDF", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 2. MERGE DOCUMENTS DIALOG
// -------------------------------------------------------------

@Composable
fun MergeDocumentsDialog(
    documents: List<DocumentEntity>,
    onDismiss: () -> Unit,
    onMerge: (docIds: List<String>, title: String) -> Unit
) {
    var mergeTitle by remember { mutableStateOf("Merged_Docs_${System.currentTimeMillis() % 10000}") }
    val selectedIds = remember { mutableStateListOf<String>() }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
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
                            Icon(Icons.Default.CallMerge, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Merge Multiple PDFs", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text("Select 2 or more files", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = mergeTitle,
                    onValueChange = { mergeTitle = it },
                    label = { Text("Output PDF Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("SELECT DOCUMENTS TO MERGE:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        val isSelected = selectedIds.contains(doc.id)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) CamScannerTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) CamScannerTeal else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (isSelected) selectedIds.remove(doc.id)
                                    else selectedIds.add(doc.id)
                                }
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isSelected,
                                onCheckedChange = { checked ->
                                    if (checked) selectedIds.add(doc.id) else selectedIds.remove(doc.id)
                                },
                                colors = CheckboxDefaults.colors(checkedColor = CamScannerTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(doc.title, fontWeight = FontWeight.Medium, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                                Text("${doc.pageCount} sheets • ${doc.fileSizeFormatted}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { onMerge(selectedIds.toList(), mergeTitle.trim().ifBlank { "Merged_Document" }) },
                    enabled = selectedIds.size >= 2,
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.CallMerge, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Merge ${selectedIds.size} Documents", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. SPLIT DOCUMENT DIALOG
// -------------------------------------------------------------

@Composable
fun SplitDocumentDialog(
    document: DocumentEntity,
    onDismiss: () -> Unit,
    onSplit: (doc: DocumentEntity, splitAfterPage: Int) -> Unit
) {
    var splitPage by remember { mutableIntStateOf(1) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
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
                            Icon(Icons.Default.CallSplit, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Split PDF Document", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(document.title, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    "Split after page: $splitPage of ${document.pageCount}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Slider(
                    value = splitPage.toFloat(),
                    onValueChange = { splitPage = it.toInt() },
                    valueRange = 1f..(document.pageCount - 1).coerceAtLeast(1).toFloat(),
                    steps = (document.pageCount - 2).coerceAtLeast(0),
                    colors = SliderDefaults.colors(thumbColor = CamScannerTeal, activeTrackColor = CamScannerTeal)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Part 1: Pages 1..$splitPage", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("Part 2: Pages ${splitPage + 1}..${document.pageCount}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = { onSplit(document, splitPage) },
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                ) {
                    Icon(Icons.Default.CallSplit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Execute Split", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 4. OCR / EXTRACTED TEXT VIEWER DIALOG
// -------------------------------------------------------------

@Composable
fun OcrTextViewerDialog(
    docTitle: String,
    extractedText: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.85f)
                .padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
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
                            Icon(Icons.Default.TextFields, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(20.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("Extracted OCR Text", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                            Text(docTitle, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                        }
                    }
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = extractedText,
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Docs Z OCR", extractedText))
                            Toast.makeText(context, "Copied text to clipboard", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Copy Text", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_TEXT, extractedText)
                                putExtra(android.content.Intent.EXTRA_SUBJECT, "$docTitle - Extracted Text")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Extracted Text"))
                        },
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f).height(44.dp)
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Share", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. RENAME DOCUMENT DIALOG
// -------------------------------------------------------------

@Composable
fun RenameDocumentDialog(
    document: DocumentEntity,
    onDismiss: () -> Unit,
    onRename: (newTitle: String) -> Unit
) {
    var newTitle by remember { mutableStateOf(document.title) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Text("Rename Document", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onRename(newTitle) },
                        colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. MOVE TO FOLDER DIALOG
// -------------------------------------------------------------

@Composable
fun MoveToFolderDialog(
    document: DocumentEntity,
    folders: List<String>,
    onDismiss: () -> Unit,
    onSelectFolder: (folder: String) -> Unit,
    onCreateFolder: (name: String) -> Unit
) {
    var newFolderName by remember { mutableStateOf("") }
    var showCreateField by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Move to Folder", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurface)
                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(folders) { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelectFolder(folder) }
                                .padding(horizontal = 10.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(folder, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                if (showCreateField) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newFolderName,
                            onValueChange = { newFolderName = it },
                            placeholder = { Text("New folder name", fontSize = 12.sp) },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (newFolderName.isNotBlank()) {
                                    onCreateFolder(newFolderName)
                                    onSelectFolder(newFolderName)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Add")
                        }
                    }
                } else {
                    OutlinedButton(
                        onClick = { showCreateField = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("+ Create New Folder", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
