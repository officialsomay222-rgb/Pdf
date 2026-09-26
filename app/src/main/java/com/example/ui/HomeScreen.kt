package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentCategory
import com.example.model.DocumentEntity
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

enum class WorkflowShortcut(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color,
    val tag: String
) {
    IMAGE_TO_PDF(
        title = "Image to PDF & Scan",
        description = "Camera scanner & gallery photos to PDF",
        icon = Icons.Default.CameraAlt,
        accentColor = Color(0xFF10B981),
        tag = "shortcut_image_to_pdf"
    ),
    PDF_COMPRESSOR(
        title = "PDF Compressor",
        description = "Reduce & optimize file size for sharing",
        icon = Icons.Default.Compress,
        accentColor = Color(0xFF0284C7),
        tag = "shortcut_pdf_compress"
    ),
    MARKUP_ANNOTATE(
        title = "PDF Editor & Markup",
        description = "Annotations, callouts, highlighter & stamps",
        icon = Icons.Default.Edit,
        accentColor = Color(0xFF38BDF8),
        tag = "shortcut_markup"
    ),
    CAD_MEASURE(
        title = "CAD Measure & Takeoff",
        description = "Calibrated scale, distance rulers & area sq ft",
        icon = Icons.Default.SquareFoot,
        accentColor = Color(0xFFF59E0B),
        tag = "shortcut_cad_measure"
    ),
    FORMS_AND_SIGN(
        title = "Forms & Digital Sign",
        description = "Interactive fields & PAdES crypto certificate",
        icon = Icons.Default.AssignmentTurnedIn,
        accentColor = Color(0xFF06B6D4),
        tag = "shortcut_forms_sign"
    ),
    AI_COPILOT(
        title = "AI Intelligence & Redact",
        description = "Gemini 3.5 RAG document Q&A & PII scrubber",
        icon = Icons.Default.AutoAwesome,
        accentColor = Color(0xFFA855F7),
        tag = "shortcut_ai_copilot"
    ),
    PAGE_ASSEMBLY(
        title = "Visual Page Assembly",
        description = "Reorder sheets, rotate 90°, duplicate & split",
        icon = Icons.Default.AutoStories,
        accentColor = Color(0xFF6366F1),
        tag = "shortcut_page_assembly"
    ),
    SECURITY_WATERMARK(
        title = "Security & Encryption",
        description = "AES-256 protection, watermarks & print locks",
        icon = Icons.Default.Security,
        accentColor = Color(0xFFEF4444),
        tag = "shortcut_security"
    ),
    NEW_BLANK_PROJECT(
        title = "New Blueprint Project",
        description = "Start fresh with orthogonal drafting grid",
        icon = Icons.Default.Add,
        accentColor = Color(0xFF0EA5E9),
        tag = "shortcut_new_blueprint"
    ),
    EXPORT_CONVERT(
        title = "Export & Share PDF",
        description = "Compile native Android PDF to share sheet",
        icon = Icons.Default.FileDownload,
        accentColor = Color(0xFFE2E8F0),
        tag = "shortcut_export"
    ),
    GITHUB_BUILD_CI(
        title = "GitHub CI / Workflow",
        description = "Automated APK build & test workflow",
        icon = Icons.Default.Terminal,
        accentColor = Color(0xFF818CF8),
        tag = "shortcut_github_ci"
    )
}

@Composable
fun HomeScreen(
    uiState: DocumentUiState,
    onOpenDocument: (DocumentEntity) -> Unit,
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onResumeWorkspace: () -> Unit,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenBuildHub: () -> Unit,
    onExportDocument: (DocumentEntity) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onOpenPdfMaker: () -> Unit = {},
    onOpenCompressor: (DocumentEntity) -> Unit = {},
    onCreatePdfFromImages: (String, List<Bitmap>, String) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Studio / Tools, 1: Create / Scan PDF, 2: Documents / Files

    val activeTab = uiState.tabs.find { it.id == uiState.activeTabId }
    val activeDoc = uiState.activeDocument

    val filteredDocs = remember(uiState.documents, uiState.searchQuery, uiState.selectedCategoryFilter) {
        uiState.documents.filter { doc ->
            val matchesSearch = uiState.searchQuery.isBlank() ||
                doc.title.contains(uiState.searchQuery, ignoreCase = true) ||
                doc.category.contains(uiState.searchQuery, ignoreCase = true)

            val matchesCategory = uiState.selectedCategoryFilter == "ALL" ||
                doc.category.equals(uiState.selectedCategoryFilter, ignoreCase = true)

            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DocNavyDark,
        bottomBar = {
            NavigationBar(
                containerColor = DocSurfaceDark,
                contentColor = Color.White,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = selectedNavTab == 0,
                    onClick = { selectedNavTab = 0 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 0) Icons.Default.Dashboard else Icons.Outlined.Dashboard,
                            contentDescription = "Studio"
                        )
                    },
                    label = { Text("Studio", fontSize = 11.sp, fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DocNavyDark,
                        selectedTextColor = DocPrimaryCyanLight,
                        indicatorColor = DocPrimaryCyanLight,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedNavTab == 1,
                    onClick = { selectedNavTab = 1 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 1) Icons.Default.CameraAlt else Icons.Outlined.CameraAlt,
                            contentDescription = "Scan / Create"
                        )
                    },
                    label = { Text("Scan / PDF", fontSize = 11.sp, fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DocNavyDark,
                        selectedTextColor = DocPrimaryCyanLight,
                        indicatorColor = DocPrimaryCyanLight,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )

                NavigationBarItem(
                    selected = selectedNavTab == 2,
                    onClick = { selectedNavTab = 2 },
                    icon = {
                        Icon(
                            imageVector = if (selectedNavTab == 2) Icons.Default.Folder else Icons.Outlined.Folder,
                            contentDescription = "My Files"
                        )
                    },
                    label = { Text("Files (${uiState.documents.size})", fontSize = 11.sp, fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = DocNavyDark,
                        selectedTextColor = DocPrimaryCyanLight,
                        indicatorColor = DocPrimaryCyanLight,
                        unselectedIconColor = Color(0xFF94A3B8),
                        unselectedTextColor = Color(0xFF94A3B8)
                    )
                )
            }
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            when (selectedNavTab) {
                0 -> {
                    // TAB 0: Studio & PDF Power Tools Launchpad
                    StudioTabContent(
                        uiState = uiState,
                        activeDoc = activeDoc,
                        activeTab = activeTab,
                        recentDocs = uiState.documents.take(3),
                        onOpenDocument = onOpenDocument,
                        onLaunchWorkflow = { shortcut ->
                            if (shortcut == WorkflowShortcut.IMAGE_TO_PDF) {
                                selectedNavTab = 1
                            } else if (shortcut == WorkflowShortcut.PDF_COMPRESSOR) {
                                if (activeDoc != null) onOpenCompressor(activeDoc)
                                else onLaunchWorkflow(shortcut)
                            } else {
                                onLaunchWorkflow(shortcut)
                            }
                        },
                        onResumeWorkspace = onResumeWorkspace,
                        onOpenCreateDialog = onOpenCreateDialog,
                        onOpenScanTab = { selectedNavTab = 1 },
                        onViewAllDocs = { selectedNavTab = 2 },
                        onExportDocument = onExportDocument,
                        onCompressDocument = onOpenCompressor,
                        onDeleteDocument = onDeleteDocument
                    )
                }
                1 -> {
                    // TAB 1: Scan / Image to PDF Creator Studio
                    ScanAndCreatePdfTabContent(
                        onCreatePdf = onCreatePdfFromImages,
                        onOpenCreateBlank = onOpenCreateDialog
                    )
                }
                2 -> {
                    // TAB 2: Dedicated Document Repository
                    DocumentsTabContent(
                        uiState = uiState,
                        filteredDocs = filteredDocs,
                        onSearchChange = onSearchChange,
                        onCategoryFilterChange = onCategoryFilterChange,
                        onOpenDocument = onOpenDocument,
                        onExportDocument = onExportDocument,
                        onCompressDocument = onOpenCompressor,
                        onDeleteDocument = onDeleteDocument,
                        onOpenCreateDialog = onOpenCreateDialog,
                        onOpenScanTab = { selectedNavTab = 1 }
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: STUDIO & TOOLS LAUNCHPAD
// -------------------------------------------------------------

@Composable
private fun StudioTabContent(
    uiState: DocumentUiState,
    activeDoc: DocumentEntity?,
    activeTab: com.example.model.DocumentWorkspaceTab?,
    recentDocs: List<DocumentEntity>,
    onOpenDocument: (DocumentEntity) -> Unit,
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onResumeWorkspace: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenScanTab: () -> Unit,
    onViewAllDocs: () -> Unit,
    onExportDocument: (DocumentEntity) -> Unit,
    onCompressDocument: (DocumentEntity) -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Studio Header
        item {
            StudioHeader(
                onOpenCreateDialog = onOpenCreateDialog,
                onOpenScanTab = onOpenScanTab
            )
        }

        // 2. Featured / Active Document Resume Card
        if (activeDoc != null && activeTab != null) {
            item {
                ActiveDocumentResumeCard(
                    document = activeDoc,
                    activeTab = activeTab,
                    onResume = onResumeWorkspace
                )
            }
        }

        // 3. Featured PDF Power Tools (4 primary cards)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FEATURED TOOLS",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Tap to open instant workspace",
                    color = Color(0xFF64748B),
                    fontSize = 11.sp
                )
            }
        }

        item {
            PrimaryToolLaunchpad(
                onLaunchWorkflow = onLaunchWorkflow,
                onOpenScanTab = onOpenScanTab,
                activeDoc = activeDoc,
                onOpenCompressor = onCompressDocument
            )
        }

        // 4. Secondary Tools Scroll Row
        item {
            SecondaryToolsRow(
                onLaunchWorkflow = onLaunchWorkflow,
                onOpenCreateDialog = onOpenCreateDialog
            )
        }

        // 5. Recent Files Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT DOCUMENTS",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                TextButton(
                    onClick = onViewAllDocs,
                    contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                ) {
                    Text("View All (${uiState.documents.size})", color = DocPrimaryCyanLight, fontSize = 12.sp)
                    Spacer(modifier = Modifier.width(2.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp), tint = DocPrimaryCyanLight)
                }
            }
        }

        if (recentDocs.isEmpty()) {
            item {
                EmptyDocumentsPlaceholder(onOpenCreateDialog = onOpenCreateDialog)
            }
        } else {
            items(recentDocs, key = { it.id }) { doc ->
                DocumentRepositoryCard(
                    document = doc,
                    onOpen = { onOpenDocument(doc) },
                    onExport = { onExportDocument(doc) },
                    onCompress = { onCompressDocument(doc) },
                    onDelete = { onDeleteDocument(doc.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// TAB 1: SCAN & IMAGE TO PDF STUDIO
// -------------------------------------------------------------

@Composable
private fun ScanAndCreatePdfTabContent(
    onCreatePdf: (title: String, bitmaps: List<Bitmap>, filter: String) -> Unit,
    onOpenCreateBlank: () -> Unit
) {
    val context = LocalContext.current
    var docTitle by remember { mutableStateOf("Scan_Doc_${SimpleDateFormat("MMdd_HHmm", Locale.US).format(Date())}") }
    val capturedImages = remember { mutableStateListOf<Bitmap>() }
    var selectedFilter by remember { mutableStateOf("ORIGINAL") } // ORIGINAL, BW_DOCUMENT, GRAYSCALE

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                capturedImages.add(bitmap)
            }
        }
    )

    // Gallery Photo Picker (Zero-permission Android Photo Picker)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 20),
        onResult = { uris ->
            uris.forEach { uri ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        val bmp = BitmapFactory.decodeStream(stream)
                        if (bmp != null) {
                            capturedImages.add(bmp)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not load image: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Title
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Image to PDF & Scanner",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Camera scan & gallery photos to standardized PDF",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("STUDIO", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Action Source Buttons
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable { cameraLauncher.launch(null) }
                        .testTag("shortcut_image_to_pdf"),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF062A1F))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF10B981)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Scan Camera", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Instant document scan", color = Color(0xFF6EE7B7), fontSize = 10.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DocPrimaryCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .clickable {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2444))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(DocPrimaryCyan),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Collections, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Pick Gallery", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Select multi-page photos", color = Color(0xFF7DD3FC), fontSize = 10.sp)
                    }
                }
            }
        }

        // PDF Title Configuration
        item {
            OutlinedTextField(
                value = docTitle,
                onValueChange = { docTitle = it },
                label = { Text("Output PDF Document Name", color = Color(0xFF94A3B8), fontSize = 12.sp) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pdf_maker_title_input"),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DocPrimaryCyan,
                    unfocusedBorderColor = DocBorderDark,
                    focusedContainerColor = DocSurfaceCardDark,
                    unfocusedContainerColor = DocSurfaceCardDark
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Enhancement Filter Chips
        item {
            Column {
                Text("SCAN ENHANCEMENT FILTER", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOptionChip(
                        label = "Original Colors",
                        isSelected = selectedFilter == "ORIGINAL",
                        onClick = { selectedFilter = "ORIGINAL" }
                    )
                    FilterOptionChip(
                        label = "High-Contrast B&W Document",
                        isSelected = selectedFilter == "BW_DOCUMENT",
                        onClick = { selectedFilter = "BW_DOCUMENT" }
                    )
                    FilterOptionChip(
                        label = "Grayscale",
                        isSelected = selectedFilter == "GRAYSCALE",
                        onClick = { selectedFilter = "GRAYSCALE" }
                    )
                }
            }
        }

        // Pages / Images List
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "PAGES IN PDF (${capturedImages.size} sheets)",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                if (capturedImages.isNotEmpty()) {
                    TextButton(onClick = { capturedImages.clear() }) {
                        Text("Clear", color = Color(0xFFEF4444), fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            if (capturedImages.isEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DocBorderDark, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No photos added yet", color = Color(0xFFCBD5E1), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                        Text("Tap 'Scan Camera' or 'Pick Gallery' above to add sheets", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(capturedImages) { index, bmp ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, DocBorderDark, RoundedCornerShape(10.dp))
                                .background(Color.Black)
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Page ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Page Number Badge
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .align(Alignment.TopStart)
                                    .clip(CircleShape)
                                    .background(DocPrimaryCyan)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${index + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }

                            // Delete button
                            IconButton(
                                onClick = { capturedImages.removeAt(index) },
                                modifier = Modifier
                                    .size(24.dp)
                                    .align(Alignment.TopEnd)
                                    .padding(2.dp)
                                    .background(Color.Black.copy(alpha = 0.7f), CircleShape)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(12.dp))
                            }
                        }
                    }
                }
            }
        }

        // Generate Action Button
        item {
            Button(
                onClick = {
                    if (capturedImages.isEmpty()) {
                        Toast.makeText(context, "Please add at least 1 image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onCreatePdf(docTitle.trim().ifEmpty { "Scanned_PDF" }, capturedImages.toList(), selectedFilter)
                },
                enabled = capturedImages.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pdf_maker_create_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate Multi-Page PDF (${capturedImages.size} pages)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Alternative: Create Blank Blueprint
        item {
            OutlinedButton(
                onClick = onOpenCreateBlank,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = DocPrimaryCyanLight)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Or Create Blank Blueprint / Drafting Grid", color = Color.White, fontSize = 12.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// TAB 2: DOCUMENTS REPOSITORY
// -------------------------------------------------------------

@Composable
private fun DocumentsTabContent(
    uiState: DocumentUiState,
    filteredDocs: List<DocumentEntity>,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onOpenDocument: (DocumentEntity) -> Unit,
    onExportDocument: (DocumentEntity) -> Unit,
    onCompressDocument: (DocumentEntity) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenScanTab: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Document Repository",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${filteredDocs.size} blueprints, PDFs & forms loaded",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onOpenScanTab,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF10B981).copy(alpha = 0.2f))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Scan to PDF", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                    }

                    Button(
                        onClick = onOpenCreateDialog,
                        colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("home_new_project_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("New", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search blueprints, scans, contracts...", fontSize = 13.sp, color = Color(0xFF64748B)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = Color(0xFF94A3B8),
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color(0xFF94A3B8),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("home_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = DocPrimaryCyan,
                    unfocusedBorderColor = DocBorderDark,
                    focusedContainerColor = DocSurfaceCardDark,
                    unfocusedContainerColor = DocSurfaceCardDark
                ),
                shape = RoundedCornerShape(12.dp)
            )
        }

        // Category Filter Chips
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CategoryFilterChip(
                    label = "All Files",
                    isSelected = uiState.selectedCategoryFilter == "ALL",
                    onClick = { onCategoryFilterChange("ALL") }
                )
                CategoryFilterChip(
                    label = "CAD Blueprints",
                    isSelected = uiState.selectedCategoryFilter == DocumentCategory.BLUEPRINT.name,
                    onClick = { onCategoryFilterChange(DocumentCategory.BLUEPRINT.name) }
                )
                CategoryFilterChip(
                    label = "Scanned PDFs",
                    isSelected = uiState.selectedCategoryFilter == DocumentCategory.SPECIFICATION.name,
                    onClick = { onCategoryFilterChange(DocumentCategory.SPECIFICATION.name) }
                )
                CategoryFilterChip(
                    label = "Contracts",
                    isSelected = uiState.selectedCategoryFilter == DocumentCategory.CONTRACT.name,
                    onClick = { onCategoryFilterChange(DocumentCategory.CONTRACT.name) }
                )
                CategoryFilterChip(
                    label = "Inspection Forms",
                    isSelected = uiState.selectedCategoryFilter == DocumentCategory.INSPECTION_FORM.name,
                    onClick = { onCategoryFilterChange(DocumentCategory.INSPECTION_FORM.name) }
                )
            }
        }

        if (filteredDocs.isEmpty()) {
            item {
                EmptyDocumentsPlaceholder(onOpenCreateDialog = onOpenCreateDialog)
            }
        } else {
            items(filteredDocs, key = { it.id }) { doc ->
                DocumentRepositoryCard(
                    document = doc,
                    onOpen = { onOpenDocument(doc) },
                    onExport = { onExportDocument(doc) },
                    onCompress = { onCompressDocument(doc) },
                    onDelete = { onDeleteDocument(doc.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-COMPONENTS
// -------------------------------------------------------------

@Composable
private fun StudioHeader(
    onOpenCreateDialog: () -> Unit,
    onOpenScanTab: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DocSurfaceDark)
            .border(1.dp, DocBorderDark, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DocPrimaryCyan),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Architecture,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Document OS",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0369A1))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "PRO STUDIO",
                            color = Color(0xFFBAE6FD),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "PDF Editor • Image to PDF • Compressor • CAD",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenScanTab,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DocSurfaceCardDark)
                    .border(1.dp, Color(0xFF10B981).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .testTag("home_github_ci_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Scan & Create PDF",
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .height(36.dp)
                    .testTag("home_new_project_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ActiveDocumentResumeCard(
    document: DocumentEntity,
    activeTab: com.example.model.DocumentWorkspaceTab,
    onResume: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .border(1.dp, DocPrimaryCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable(onClick = onResume)
            .testTag("resume_active_workspace_card"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0C2444))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF10B981))
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "ACTIVE IN WORKSPACE",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = document.title,
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Sheet ${activeTab.activePageIndex + 1} of ${activeTab.pageCount} • Zoom ${(activeTab.zoomLevel * 100).toInt()}% • ${document.fileSizeFormatted}",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }

            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("resume_workspace_button")
            ) {
                Text("Resume", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
private fun PrimaryToolLaunchpad(
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onOpenScanTab: () -> Unit,
    activeDoc: DocumentEntity?,
    onOpenCompressor: (DocumentEntity) -> Unit
) {
    val topShortcuts = listOf(
        WorkflowShortcut.IMAGE_TO_PDF,
        WorkflowShortcut.PDF_COMPRESSOR,
        WorkflowShortcut.MARKUP_ANNOTATE,
        WorkflowShortcut.CAD_MEASURE
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in topShortcuts.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkflowShortcutTile(
                    shortcut = topShortcuts[i],
                    onClick = {
                        if (topShortcuts[i] == WorkflowShortcut.IMAGE_TO_PDF) {
                            onOpenScanTab()
                        } else if (topShortcuts[i] == WorkflowShortcut.PDF_COMPRESSOR) {
                            if (activeDoc != null) onOpenCompressor(activeDoc)
                            else onLaunchWorkflow(topShortcuts[i])
                        } else {
                            onLaunchWorkflow(topShortcuts[i])
                        }
                    },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < topShortcuts.size) {
                    WorkflowShortcutTile(
                        shortcut = topShortcuts[i + 1],
                        onClick = {
                            if (topShortcuts[i + 1] == WorkflowShortcut.PDF_COMPRESSOR) {
                                if (activeDoc != null) onOpenCompressor(activeDoc)
                                else onLaunchWorkflow(topShortcuts[i + 1])
                            } else {
                                onLaunchWorkflow(topShortcuts[i + 1])
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SecondaryToolsRow(
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onOpenCreateDialog: () -> Unit
) {
    val secondaryTools = listOf(
        WorkflowShortcut.FORMS_AND_SIGN,
        WorkflowShortcut.AI_COPILOT,
        WorkflowShortcut.PAGE_ASSEMBLY,
        WorkflowShortcut.SECURITY_WATERMARK,
        WorkflowShortcut.NEW_BLANK_PROJECT,
        WorkflowShortcut.EXPORT_CONVERT,
        WorkflowShortcut.GITHUB_BUILD_CI
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        secondaryTools.forEach { shortcut ->
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(DocSurfaceCardDark)
                    .border(1.dp, DocBorderDark, RoundedCornerShape(10.dp))
                    .clickable { onLaunchWorkflow(shortcut) }
                    .padding(horizontal = 10.dp, vertical = 7.dp)
                    .testTag(shortcut.tag),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = shortcut.icon,
                    contentDescription = null,
                    tint = shortcut.accentColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = shortcut.title,
                    color = Color(0xFFE2E8F0),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun WorkflowShortcutTile(
    shortcut: WorkflowShortcut,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DocBorderDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(shortcut.tag),
        colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shortcut.accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = shortcut.icon,
                    contentDescription = null,
                    tint = shortcut.accentColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = shortcut.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = shortcut.description,
                    color = Color(0xFF94A3B8),
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategoryFilterChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (isSelected) DocPrimaryCyan else DocSurfaceCardDark)
            .border(1.dp, if (isSelected) DocPrimaryCyan else DocBorderDark, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun FilterOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) DocPrimaryCyan else DocSurfaceCardDark)
            .border(1.dp, if (isSelected) DocPrimaryCyan else DocBorderDark, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 5.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

@Composable
private fun DocumentRepositoryCard(
    document: DocumentEntity,
    onOpen: () -> Unit,
    onExport: () -> Unit,
    onCompress: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (document.category) {
        DocumentCategory.BLUEPRINT.name -> Color(0xFF38BDF8)
        DocumentCategory.CONTRACT.name -> Color(0xFFF59E0B)
        DocumentCategory.INSPECTION_FORM.name -> Color(0xFF10B981)
        DocumentCategory.SPECIFICATION.name -> Color(0xFF06B6D4)
        else -> Color(0xFFA855F7)
    }

    val dateFormatted = remember(document.modifiedAt) {
        SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(document.modifiedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DocBorderDark, RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen)
            .testTag("doc_repo_card_${document.id}"),
        colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(categoryColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (document.category) {
                        DocumentCategory.BLUEPRINT.name -> Icons.Default.Layers
                        DocumentCategory.CONTRACT.name -> Icons.Default.Gavel
                        DocumentCategory.INSPECTION_FORM.name -> Icons.AutoMirrored.Filled.FactCheck
                        DocumentCategory.SPECIFICATION.name -> Icons.Default.PhotoLibrary
                        else -> Icons.Default.Description
                    },
                    contentDescription = null,
                    tint = categoryColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = document.title,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (document.isPasswordProtected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Encrypted",
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(categoryColor.copy(alpha = 0.2f))
                            .padding(horizontal = 4.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = document.category.replace("_", " "),
                            color = categoryColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${document.pageCount} sheets • ${document.fileSizeFormatted}",
                        color = Color(0xFF94A3B8),
                        fontSize = 10.sp
                    )
                }
                Text(
                    text = "Modified: $dateFormatted",
                    color = Color(0xFF64748B),
                    fontSize = 9.sp
                )
            }

            // Quick Actions: Compress, Share, Delete, Open
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onCompress,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Compress,
                        contentDescription = "Compress PDF",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onExport,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("export_doc_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share PDF",
                        tint = Color(0xFF10B981),
                        modifier = Modifier.size(15.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(28.dp)
                        .testTag("delete_doc_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(15.dp)
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    onClick = onOpen,
                    colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier
                        .height(30.dp)
                        .testTag("open_doc_${document.id}")
                ) {
                    Text("Open", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EmptyDocumentsPlaceholder(onOpenCreateDialog: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Description,
                contentDescription = null,
                tint = Color(0xFF64748B),
                modifier = Modifier.size(36.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("No matching documents found", color = Color(0xFFCBD5E1), fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text("Try clearing your search or create a new project", color = Color(0xFF64748B), fontSize = 11.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onOpenCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Create New Project", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
