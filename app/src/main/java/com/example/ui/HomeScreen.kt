package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.example.engine.PdfEngine
import com.example.ui.components.DocumentActionShutterSheet
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
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
    ID_CARD_SCAN(
        title = "ID Card 2-in-1",
        description = "Front & back onto one standard A4 sheet",
        icon = Icons.Default.Badge,
        accentColor = Color(0xFF00897B),
        tag = "shortcut_id_card"
    ),
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
    OCR_TO_TEXT(
        title = "To Word / Text OCR",
        description = "Extract text & table data from documents",
        icon = Icons.Default.TextFields,
        accentColor = Color(0xFF8B5CF6),
        tag = "shortcut_ocr_text"
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
        title = "Digital Signatures",
        description = "Interactive fields & crypto certificates",
        icon = Icons.Default.AssignmentTurnedIn,
        accentColor = Color(0xFF06B6D4),
        tag = "shortcut_forms_sign"
    ),
    MERGE_PDFS(
        title = "Merge Multiple PDFs",
        description = "Combine multiple documents into one",
        icon = Icons.Default.CallMerge,
        accentColor = Color(0xFFEC4899),
        tag = "shortcut_merge_pdfs"
    ),
    PAGE_ASSEMBLY(
        title = "Page Assembly",
        description = "Reorder sheets, rotate 90°, duplicate & split",
        icon = Icons.Default.AutoStories,
        accentColor = Color(0xFF6366F1),
        tag = "shortcut_page_assembly"
    ),
    SECURITY_WATERMARK(
        title = "Security & Protect",
        description = "AES-256 protection, watermarks & print locks",
        icon = Icons.Default.Security,
        accentColor = Color(0xFFEF4444),
        tag = "shortcut_security"
    ),
    NEW_BLANK_PROJECT(
        title = "New Blueprint Project",
        description = "Start fresh with drafting grid",
        icon = Icons.Default.Add,
        accentColor = Color(0xFF0EA5E9),
        tag = "shortcut_new_blueprint"
    ),
    EXPORT_CONVERT(
        title = "Export & Share PDF",
        description = "Compile native PDF to share sheet",
        icon = Icons.Default.FileDownload,
        accentColor = Color(0xFFE2E8F0),
        tag = "shortcut_export"
    ),
    GITHUB_BUILD_CI(
        title = "Docs Z Engine Hub",
        description = "Automated compiler & APK verification",
        icon = Icons.Default.Terminal,
        accentColor = Color(0xFF818CF8),
        tag = "shortcut_github_ci"
    )
}

@Composable
fun HomeScreen(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    onOpenDocument: (DocumentEntity) -> Unit,
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onResumeWorkspace: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeDoc = uiState.activeDocument
    val activeTab = uiState.tabs.find { it.id == uiState.activeTabId }

    // Filter & Sort documents
    val filteredDocs = remember(
        uiState.documents,
        uiState.searchQuery,
        uiState.selectedCategoryFilter,
        uiState.selectedFolder,
        uiState.sortOption
    ) {
        var list = uiState.documents.filter { doc ->
            val matchesSearch = uiState.searchQuery.isBlank() ||
                doc.title.contains(uiState.searchQuery, ignoreCase = true) ||
                doc.category.contains(uiState.searchQuery, ignoreCase = true)

            val matchesCategory = uiState.selectedCategoryFilter == "ALL" ||
                doc.category.equals(uiState.selectedCategoryFilter, ignoreCase = true)

            val matchesFolder = uiState.selectedFolder == "All Docs" ||
                doc.folder.equals(uiState.selectedFolder, ignoreCase = true) ||
                (uiState.selectedFolder == "ID Cards" && doc.category == "ID_CARD")

            matchesSearch && matchesCategory && matchesFolder
        }

        list = when (uiState.sortOption) {
            DocSortOption.DATE_DESC -> list.sortedByDescending { it.modifiedAt }
            DocSortOption.DATE_ASC -> list.sortedBy { it.modifiedAt }
            DocSortOption.NAME_ASC -> list.sortedBy { it.title.lowercase() }
            DocSortOption.NAME_DESC -> list.sortedByDescending { it.title.lowercase() }
            DocSortOption.SIZE_DESC -> list.sortedByDescending {
                it.fileSizeFormatted.replace("MB", "").trim().toDoubleOrNull() ?: 0.0
            }
        }
        list
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        bottomBar = {
            DocsZBottomNavBar(
                activeTab = uiState.activeNavTab,
                docCount = uiState.documents.size,
                onSelectTab = { viewModel.setNavTab(it) }
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(contentPadding)
        ) {
            Crossfade(
                targetState = uiState.activeNavTab,
                animationSpec = tween(durationMillis = 50, easing = LinearEasing),
                label = "activeTabCrossfade"
            ) { targetTab ->
                when (targetTab) {
                    CamScannerNavTab.DOCS -> {
                        DocsTabHomeContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            filteredDocs = filteredDocs,
                            activeDoc = activeDoc,
                            activeTab = activeTab,
                            onOpenDocument = onOpenDocument,
                            onLaunchWorkflow = onLaunchWorkflow,
                            onResumeWorkspace = onResumeWorkspace
                        )
                    }
                    CamScannerNavTab.TOOLS -> {
                        ToolsTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onLaunchWorkflow = onLaunchWorkflow
                        )
                    }
                    CamScannerNavTab.SCAN -> {
                        ScanStudioTabContent(
                            viewModel = viewModel,
                            onCreatePdf = { title, bmps, filter, addWhiteBorder ->
                                viewModel.createPdfFromScannedImages(title, bmps, filter, context, addWhiteBorder)
                            },
                            onOpenIdCard = { viewModel.setIdCardScannerOpen(true) }
                        )
                    }
                    CamScannerNavTab.FOLDERS -> {
                        FoldersTabContent(
                            uiState = uiState,
                            viewModel = viewModel,
                            onOpenDocument = onOpenDocument
                        )
                    }
                    CamScannerNavTab.SETTINGS -> {
                        SettingsTabContent(
                            uiState = uiState,
                            viewModel = viewModel
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// BOTTOM NAVIGATION BAR (Floating Capsule Design)
// -------------------------------------------------------------

@Composable
private fun DocsZBottomNavBar(
    activeTab: CamScannerNavTab,
    docCount: Int,
    onSelectTab: (CamScannerNavTab) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CapsuleTabItem(
                    icon = if (activeTab == CamScannerNavTab.DOCS) Icons.Default.Description else Icons.Outlined.Description,
                    label = "Docs",
                    isSelected = activeTab == CamScannerNavTab.DOCS,
                    onClick = { onSelectTab(CamScannerNavTab.DOCS) },
                    modifier = Modifier.testTag("nav_tab_docs")
                )

                CapsuleTabItem(
                    icon = if (activeTab == CamScannerNavTab.TOOLS) Icons.Default.GridView else Icons.Outlined.GridView,
                    label = "Tools",
                    isSelected = activeTab == CamScannerNavTab.TOOLS,
                    onClick = { onSelectTab(CamScannerNavTab.TOOLS) },
                    modifier = Modifier.testTag("nav_tab_tools")
                )

                // Hero Center Action - Camera Scan Button
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CamScannerTeal)
                        .clickable { onSelectTab(CamScannerNavTab.SCAN) }
                        .testTag("home_camera_fab"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = "Scan Document",
                        tint = Color.White,
                        modifier = Modifier.size(22.dp)
                    )
                }

                CapsuleTabItem(
                    icon = if (activeTab == CamScannerNavTab.FOLDERS) Icons.Default.Folder else Icons.Outlined.Folder,
                    label = "Folders",
                    isSelected = activeTab == CamScannerNavTab.FOLDERS,
                    onClick = { onSelectTab(CamScannerNavTab.FOLDERS) },
                    modifier = Modifier.testTag("nav_tab_folders")
                )

                CapsuleTabItem(
                    icon = if (activeTab == CamScannerNavTab.SETTINGS) Icons.Default.Settings else Icons.Outlined.Settings,
                    label = "Settings",
                    isSelected = activeTab == CamScannerNavTab.SETTINGS,
                    onClick = { onSelectTab(CamScannerNavTab.SETTINGS) },
                    modifier = Modifier.testTag("nav_tab_settings")
                )
            }
        }
    }
}

@Composable
private fun CapsuleTabItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
    Column(
        modifier = modifier
            .clip(CircleShape)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(if (isSelected) CamScannerTeal.copy(alpha = 0.16f) else Color.Transparent)
                .padding(horizontal = 10.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(modifier = Modifier.height(1.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// -------------------------------------------------------------
// STAGGERED CAPSULE ENTRANCE CONTAINER
// -------------------------------------------------------------

@Composable
private fun StaggeredCapsuleContainer(
    index: Int,
    isLoaded: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0f,
        animationSpec = tween(
            durationMillis = 320,
            delayMillis = (index * 50).coerceAtMost(350),
            easing = FastOutSlowInEasing
        ),
        label = "capsuleAlpha_$index"
    )
    val animatedOffsetY by animateDpAsState(
        targetValue = if (isLoaded) 0.dp else (22.dp + (index * 3).dp),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "capsuleOffset_$index"
    )
    val animatedScale by animateFloatAsState(
        targetValue = if (isLoaded) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "capsuleScale_$index"
    )

    Box(
        modifier = modifier
            .offset(y = animatedOffsetY)
            .graphicsLayer {
                alpha = animatedAlpha
                scaleX = animatedScale
                scaleY = animatedScale
            }
    ) {
        content()
    }
}

// -------------------------------------------------------------
// TAB 1: DOCS (HOME SCREEN)
// -------------------------------------------------------------

@Composable
private fun DocsTabHomeContent(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    filteredDocs: List<DocumentEntity>,
    activeDoc: DocumentEntity?,
    activeTab: DocumentWorkspaceTab?,
    onOpenDocument: (DocumentEntity) -> Unit,
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onResumeWorkspace: () -> Unit
) {
    val context = LocalContext.current
    var showSortMenu by remember { mutableStateOf(false) }
    var actionSheetDoc by remember { mutableStateOf<DocumentEntity?>(null) }
    var screenLoaded by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(20)
        screenLoaded = true
    }

    // PDF & Document File Picker Launcher
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            if (uri != null) {
                viewModel.importPdfFromUri(uri, context)
            }
        }
    )

    // Multi-file photo picker
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(20),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                viewModel.importMultipleUris(uris, context)
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Multi-select Action Bar or Top Header
        if (uiState.isMultiSelectMode) {
            MultiSelectActionBar(
                selectedCount = uiState.selectedDocIds.size,
                totalCount = filteredDocs.size,
                onSelectAll = { viewModel.selectAllDocs(it) },
                onCancel = { viewModel.setMultiSelectMode(false) },
                onDelete = { viewModel.batchDeleteSelected() },
                onShare = { viewModel.batchShareSelected(context) },
                onMerge = { viewModel.setMergeDocsDialogOpen(true) }
            )
        } else {
            // Main Top Bar
            DocsZTopBar(
                searchQuery = uiState.searchQuery,
                onSearchChange = { viewModel.setSearchQuery(it) },
                docViewMode = uiState.docViewMode,
                onToggleViewMode = {
                    val nextMode = if (uiState.docViewMode == DocViewMode.LIST) DocViewMode.GRID else DocViewMode.LIST
                    viewModel.setDocViewMode(nextMode)
                },
                appThemeMode = uiState.appThemeMode,
                onToggleTheme = {
                    val allThemes = AppThemeMode.entries
                    val currentIndex = allThemes.indexOf(uiState.appThemeMode)
                    val nextTheme = allThemes[(currentIndex + 1) % allThemes.size]
                    viewModel.setAppThemeMode(nextTheme)
                },
                onOpenSortMenu = { showSortMenu = true },
                onImportFiles = {
                    pdfPicker.launch(arrayOf("application/pdf", "image/*"))
                }
            )
        }

        // Sort Dropdown Menu
        DropdownMenu(
            expanded = showSortMenu,
            onDismissRequest = { showSortMenu = false }
        ) {
            DocSortOption.values().forEach { option ->
                DropdownMenuItem(
                    text = {
                        Text(
                            text = option.displayName,
                            fontWeight = if (uiState.sortOption == option) FontWeight.Bold else FontWeight.Normal,
                            color = if (uiState.sortOption == option) CamScannerTeal else MaterialTheme.colorScheme.onSurface
                        )
                    },
                    onClick = {
                        viewModel.setSortOption(option)
                        showSortMenu = false
                    },
                    leadingIcon = {
                        if (uiState.sortOption == option) {
                            Icon(Icons.Default.Check, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(16.dp))
                        }
                    }
                )
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentPadding = PaddingValues(bottom = 80.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Quick Tool Action Shortcuts Carousel (CamScanner Style)
            item {
                StaggeredCapsuleContainer(index = 1, isLoaded = screenLoaded) {
                    CamScannerShortcutCarousel(
                        onShortcutClick = { shortcut ->
                            when (shortcut) {
                                WorkflowShortcut.ID_CARD_SCAN -> viewModel.setIdCardScannerOpen(true)
                                WorkflowShortcut.IMAGE_TO_PDF -> viewModel.setNavTab(CamScannerNavTab.SCAN)
                                WorkflowShortcut.PDF_COMPRESSOR -> {
                                    if (activeDoc != null) viewModel.openCompressorForDoc(activeDoc)
                                    else if (filteredDocs.isNotEmpty()) viewModel.openCompressorForDoc(filteredDocs.first())
                                    else onLaunchWorkflow(shortcut)
                                }
                                WorkflowShortcut.OCR_TO_TEXT -> {
                                    val target = activeDoc ?: filteredDocs.firstOrNull()
                                    if (target != null) viewModel.extractTextFromDoc(target)
                                    else Toast.makeText(context, "No document loaded to extract text", Toast.LENGTH_SHORT).show()
                                }
                                WorkflowShortcut.MERGE_PDFS -> viewModel.setMergeDocsDialogOpen(true)
                                else -> onLaunchWorkflow(shortcut)
                            }
                        }
                    )
                }
            }

            // 2. Folder Tabs & Filter Chips Row
            item {
                StaggeredCapsuleContainer(index = 2, isLoaded = screenLoaded) {
                    CategoryChipsRow(
                        selectedCategory = uiState.selectedCategoryFilter,
                        onSelectCategory = { viewModel.setCategoryFilter(it) }
                    )
                }
            }

            // 3. Featured / Recent Scanned Document Resume Card
            if (activeDoc != null && activeTab != null) {
                item {
                    StaggeredCapsuleContainer(index = 3, isLoaded = screenLoaded) {
                        FeaturedDocumentCard(
                            doc = activeDoc,
                            tab = activeTab,
                            onResume = onResumeWorkspace,
                            onExport = { viewModel.exportDocument(context) },
                            onCompress = { viewModel.openCompressorForDoc(activeDoc) },
                            onOcr = { viewModel.extractTextFromDoc(activeDoc) }
                        )
                    }
                }
            }

            // Active Folder Filter Indicator (if a specific folder is selected)
            if (uiState.selectedFolder != "All Docs") {
                item {
                    StaggeredCapsuleContainer(index = 4, isLoaded = screenLoaded) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = CamScannerTeal.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, CamScannerTeal.copy(alpha = 0.35f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Folder,
                                        contentDescription = null,
                                        tint = CamScannerTeal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Folder: ${uiState.selectedFolder} (${filteredDocs.size} docs)",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                TextButton(
                                    onClick = { viewModel.setSelectedFolder("All Docs") },
                                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                    modifier = Modifier.height(28.dp)
                                ) {
                                    Text("Clear Filter", fontSize = 11.sp, color = CamScannerTeal, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // 4. Section Header with View All & Multi-select trigger
            item {
                StaggeredCapsuleContainer(index = 5, isLoaded = screenLoaded) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "ALL DOCUMENTS",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${filteredDocs.size}",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(
                                onClick = { viewModel.setMultiSelectMode(!uiState.isMultiSelectMode) },
                                contentPadding = PaddingValues(horizontal = 6.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.CheckCircleOutline, contentDescription = null, modifier = Modifier.size(14.dp), tint = CamScannerTeal)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Select", fontSize = 12.sp, color = CamScannerTeal, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }

            // 5. Documents Grid or List
            if (filteredDocs.isEmpty()) {
                item {
                    StaggeredCapsuleContainer(index = 6, isLoaded = screenLoaded) {
                        EmptyDocumentsPlaceholder(
                            onOpenScan = { viewModel.setNavTab(CamScannerNavTab.SCAN) },
                            onOpenImport = { pdfPicker.launch(arrayOf("application/pdf", "image/*")) },
                            onOpenCreate = { viewModel.setCreateProjectDialogOpen(true) }
                        )
                    }
                }
            } else {
                if (uiState.docViewMode == DocViewMode.LIST) {
                    itemsIndexed(filteredDocs, key = { _, doc -> doc.id }) { index, doc ->
                        StaggeredCapsuleContainer(index = 6 + index.coerceAtMost(6), isLoaded = screenLoaded) {
                            DocZListItemCard(
                                document = doc,
                                isSelected = uiState.selectedDocIds.contains(doc.id),
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                onToggleSelect = { viewModel.toggleSelectDoc(doc.id) },
                                onOpen = { onOpenDocument(doc) },
                                onOpenStudio = { viewModel.openDocumentInStudio(doc) },
                                onOpenMore = { actionSheetDoc = doc }
                            )
                        }
                    }
                } else {
                    // Grid View
                    item {
                        StaggeredCapsuleContainer(index = 6, isLoaded = screenLoaded) {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 1200.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(filteredDocs, key = { it.id }) { doc ->
                                    DocZGridItemCard(
                                        document = doc,
                                        isSelected = uiState.selectedDocIds.contains(doc.id),
                                        isMultiSelectMode = uiState.isMultiSelectMode,
                                        onToggleSelect = { viewModel.toggleSelectDoc(doc.id) },
                                        onOpen = { onOpenDocument(doc) },
                                        onOpenStudio = { viewModel.openDocumentInStudio(doc) },
                                        onOpenMore = { actionSheetDoc = doc }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Shutter Bottom Action Sheet for three-dot menus (smooth, no lag, full options)
        actionSheetDoc?.let { targetDoc ->
            DocumentActionShutterSheet(
                document = targetDoc,
                onDismiss = { actionSheetDoc = null },
                onOpenReader = {
                    actionSheetDoc = null
                    onOpenDocument(targetDoc)
                },
                onOpenStudio = {
                    actionSheetDoc = null
                    viewModel.openDocumentInStudio(targetDoc)
                },
                onShare = {
                    actionSheetDoc = null
                    viewModel.openDocument(targetDoc)
                    viewModel.exportDocument(context)
                },
                onCompress = {
                    actionSheetDoc = null
                    viewModel.openCompressorForDoc(targetDoc)
                },
                onRename = {
                    actionSheetDoc = null
                    viewModel.setRenameDialogOpen(true, targetDoc)
                },
                onOcr = {
                    actionSheetDoc = null
                    viewModel.extractTextFromDoc(targetDoc)
                },
                onMoveFolder = {
                    actionSheetDoc = null
                    viewModel.setMoveFolderDialogOpen(true, targetDoc)
                },
                onSplit = {
                    actionSheetDoc = null
                    viewModel.setSplitDocDialogOpen(true, targetDoc)
                },
                onDuplicate = {
                    actionSheetDoc = null
                    viewModel.duplicateDocument(targetDoc)
                },
                onDelete = {
                    actionSheetDoc = null
                    viewModel.deleteDocument(targetDoc.id)
                }
            )
        }
    }
}

// -------------------------------------------------------------
// TOP APP BAR (CamScanner SEARCH & ACTIONS)
// -------------------------------------------------------------

@Composable
private fun DocsZTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    docViewMode: DocViewMode,
    onToggleViewMode: () -> Unit,
    appThemeMode: AppThemeMode,
    onToggleTheme: () -> Unit,
    onOpenSortMenu: () -> Unit,
    onImportFiles: () -> Unit
) {
    var isSearchExpanded by rememberSaveable { mutableStateOf(searchQuery.isNotEmpty()) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        AnimatedContent(
            targetState = isSearchExpanded,
            transitionSpec = {
                (fadeIn(animationSpec = tween(180)) + expandHorizontally())
                    .togetherWith(fadeOut(animationSpec = tween(140)) + shrinkHorizontally())
            },
            label = "searchBarExpand"
        ) { expanded ->
            if (expanded) {
                // Extended Full-Width Search Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp)
                            .clip(RoundedCornerShape(22.dp))
                            .testTag("home_search_input"),
                        shape = RoundedCornerShape(22.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                        border = BorderStroke(1.dp, CamScannerTeal.copy(alpha = 0.5f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(
                                onClick = {
                                    onSearchChange("")
                                    isSearchExpanded = false
                                },
                                modifier = Modifier.size(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Collapse search",
                                    tint = CamScannerTeal,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.CenterStart
                            ) {
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search docs, tags, text...",
                                        fontSize = 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                                        maxLines = 1
                                    )
                                }
                                BasicTextField(
                                    value = searchQuery,
                                    onValueChange = onSearchChange,
                                    singleLine = true,
                                    textStyle = androidx.compose.ui.text.TextStyle(
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium
                                    ),
                                    cursorBrush = androidx.compose.ui.graphics.SolidColor(CamScannerTeal),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                            if (searchQuery.isNotEmpty()) {
                                IconButton(
                                    onClick = { onSearchChange("") },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = "Clear search",
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Collapsed Compact Bar with Search as an Icon Button
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // DOCS Z Brand Logo Pill (Compact, non-crowding)
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = CamScannerTeal,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.DocumentScanner,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "DOCS Z",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    // Action Icons Row (Well-proportioned to prevent overlay on any screen)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Expandable Search Button (Icon Capsule)
                        IconButton(
                            onClick = { isSearchExpanded = true },
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CamScannerTeal.copy(alpha = 0.12f))
                                .border(0.5.dp, CamScannerTeal.copy(alpha = 0.4f), CircleShape)
                                .testTag("home_search_expand_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = CamScannerTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Import Document / PDF Button
                        IconButton(
                            onClick = onImportFiles,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(CamScannerTeal.copy(alpha = 0.12f))
                                .border(0.5.dp, CamScannerTeal.copy(alpha = 0.4f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FileOpen,
                                contentDescription = "Import PDF",
                                tint = CamScannerTeal,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // View Mode Toggle (List vs Grid)
                        IconButton(
                            onClick = onToggleViewMode,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = if (docViewMode == DocViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                                contentDescription = "Toggle View",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Sort Selector
                        IconButton(
                            onClick = onOpenSortMenu,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sort,
                                contentDescription = "Sort",
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(17.dp)
                            )
                        }

                        // Theme Toggle (Supports all 7 themes)
                        IconButton(
                            onClick = onToggleTheme,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                                .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
                        ) {
                            Icon(
                                imageVector = when (appThemeMode) {
                                    AppThemeMode.LIGHT -> Icons.Default.LightMode
                                    AppThemeMode.DARK -> Icons.Default.DarkMode
                                    AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                    AppThemeMode.OLED_BLACK -> Icons.Default.NightlightRound
                                    AppThemeMode.WARM_SEPIA -> Icons.Default.MenuBook
                                    AppThemeMode.NORDIC_FROST -> Icons.Default.AcUnit
                                    AppThemeMode.SUNSET_AMBER -> Icons.Default.WbSunny
                                },
                                contentDescription = "Theme Mode",
                                tint = CamScannerTeal,
                                modifier = Modifier.size(17.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MULTI-SELECT ACTION BAR
// -------------------------------------------------------------

@Composable
private fun MultiSelectActionBar(
    selectedCount: Int,
    totalCount: Int,
    onSelectAll: (Boolean) -> Unit,
    onCancel: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    onMerge: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(CamScannerTeal)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "$selectedCount selected",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { onSelectAll(selectedCount < totalCount) }, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = if (selectedCount == totalCount) Icons.Default.CheckBox else Icons.Default.SelectAll,
                    contentDescription = "Select All",
                    tint = Color.White
                )
            }
            if (selectedCount >= 2) {
                IconButton(onClick = onMerge, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.CallMerge, contentDescription = "Merge", tint = Color.White)
                }
            }
            IconButton(onClick = onShare, enabled = selectedCount > 0, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
            }
            IconButton(onClick = onDelete, enabled = selectedCount > 0, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White)
            }
        }
    }
}
}

// -------------------------------------------------------------
// QUICK SHORTCUT CAROUSEL (CamScanner Style)
// -------------------------------------------------------------

@Composable
private fun CamScannerShortcutCarousel(
    onShortcutClick: (WorkflowShortcut) -> Unit
) {
    val shortcuts = listOf(
        WorkflowShortcut.ID_CARD_SCAN,
        WorkflowShortcut.IMAGE_TO_PDF,
        WorkflowShortcut.PDF_COMPRESSOR,
        WorkflowShortcut.OCR_TO_TEXT,
        WorkflowShortcut.FORMS_AND_SIGN,
        WorkflowShortcut.MARKUP_ANNOTATE,
        WorkflowShortcut.CAD_MEASURE,
        WorkflowShortcut.MERGE_PDFS
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(shortcuts) { shortcut ->
            Card(
                modifier = Modifier
                    .width(100.dp)
                    .height(90.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onShortcutClick(shortcut) }
                    .testTag(shortcut.tag),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(shortcut.accentColor.copy(alpha = 0.14f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = shortcut.icon,
                            contentDescription = shortcut.title,
                            tint = shortcut.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = shortcut.title,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// CATEGORY CHIPS ROW
// -------------------------------------------------------------

@Composable
private fun CategoryChipsRow(
    selectedCategory: String,
    onSelectCategory: (String) -> Unit
) {
    val categories = listOf(
        "ALL" to "All Docs",
        "ID_CARD" to "ID Cards",
        "SPECIFICATION" to "Scans",
        "CONTRACT" to "Contracts",
        "INSPECTION_FORM" to "Forms",
        "BLUEPRINT" to "Blueprints"
    )

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { (key, label) ->
            val isSelected = selectedCategory == key
            FilterChip(
                selected = isSelected,
                onClick = { onSelectCategory(key) },
                label = {
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CamScannerTeal,
                    selectedLabelColor = Color.White,
                    containerColor = MaterialTheme.colorScheme.surface,
                    labelColor = MaterialTheme.colorScheme.onSurface
                ),
                shape = RoundedCornerShape(16.dp),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                    selectedBorderColor = CamScannerTeal
                )
            )
        }
    }
}

// -------------------------------------------------------------
// FEATURED ACTIVE DOCUMENT CARD
// -------------------------------------------------------------

@Composable
private fun FeaturedDocumentCard(
    doc: DocumentEntity,
    tab: DocumentWorkspaceTab,
    onResume: () -> Unit,
    onExport: () -> Unit,
    onCompress: () -> Unit,
    onOcr: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onResume),
        colors = CardDefaults.cardColors(
            containerColor = CamScannerTeal.copy(alpha = 0.08f)
        ),
        border = BorderStroke(1.dp, CamScannerTeal.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CamScannerTeal.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Description,
                        contentDescription = null,
                        tint = CamScannerTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = doc.title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Recent • Page ${tab.activePageIndex + 1} of ${tab.pageCount} • ${doc.fileSizeFormatted}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            Button(
                onClick = onResume,
                colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier.height(32.dp).testTag("resume_workspace_button")
            ) {
                Text("Resume", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// -------------------------------------------------------------
// LIST ITEM DOCUMENT CARD (CamScanner Style)
// -------------------------------------------------------------

@Composable
private fun DocZListItemCard(
    document: DocumentEntity,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onToggleSelect: () -> Unit,
    onOpen: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenMore: () -> Unit
) {
    val formattedDate = remember(document.modifiedAt) {
        SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(Date(document.modifiedAt))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (isMultiSelectMode) onToggleSelect() else onOpen()
            }
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CamScannerTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Multi-select Checkbox or Thumbnail
            if (isMultiSelectMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onToggleSelect() },
                    colors = CheckboxDefaults.colors(checkedColor = CamScannerTeal)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }

            // Thumbnail Preview Badge
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        when (document.category) {
                            "ID_CARD" -> Color(0xFF00897B).copy(alpha = 0.15f)
                            "BLUEPRINT" -> Color(0xFF0284C7).copy(alpha = 0.15f)
                            "CONTRACT" -> Color(0xFF6366F1).copy(alpha = 0.15f)
                            "INSPECTION_FORM" -> Color(0xFFF59E0B).copy(alpha = 0.15f)
                            else -> CamScannerTeal.copy(alpha = 0.15f)
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when (document.category) {
                        "ID_CARD" -> Icons.Default.Badge
                        "BLUEPRINT" -> Icons.Default.Architecture
                        "CONTRACT" -> Icons.Default.Description
                        "INSPECTION_FORM" -> Icons.Default.AssignmentTurnedIn
                        else -> Icons.Default.PictureAsPdf
                    },
                    contentDescription = null,
                    tint = when (document.category) {
                        "ID_CARD" -> Color(0xFF00897B)
                        "BLUEPRINT" -> Color(0xFF0284C7)
                        "CONTRACT" -> Color(0xFF6366F1)
                        "INSPECTION_FORM" -> Color(0xFFF59E0B)
                        else -> CamScannerTeal
                    },
                    modifier = Modifier.size(24.dp)
                )

                // Page count pill in corner
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(2.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 3.dp, vertical = 1.dp)
                ) {
                    Text("${document.pageCount}p", color = Color.White, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Title & Metadata
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedDate,
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ${document.fileSizeFormatted}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (document.isPasswordProtected) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.Default.Lock, contentDescription = "Protected", tint = Color(0xFFEF4444), modifier = Modifier.size(10.dp))
                    }
                }
            }

            // Quick Studio Editor Button
            IconButton(
                onClick = onOpenStudio,
                modifier = Modifier.size(34.dp).testTag("doc_item_edit_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Studio Editor",
                    tint = CamScannerTeal,
                    modifier = Modifier.size(18.dp)
                )
            }

            // 3-dot context menu (triggers buttery smooth Shutter sheet)
            IconButton(
                onClick = onOpenMore,
                modifier = Modifier.size(34.dp).testTag("doc_item_more_button")
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More actions",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// GRID ITEM DOCUMENT CARD (CamScanner Style)
// -------------------------------------------------------------

@Composable
private fun DocZGridItemCard(
    document: DocumentEntity,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    onToggleSelect: () -> Unit,
    onOpen: () -> Unit,
    onOpenStudio: () -> Unit,
    onOpenMore: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable {
                if (isMultiSelectMode) onToggleSelect() else onOpen()
            }
            .border(
                width = if (isSelected) 1.5.dp else 0.5.dp,
                color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                shape = RoundedCornerShape(12.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CamScannerTeal.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(CamScannerTeal.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = when (document.category) {
                            "ID_CARD" -> Icons.Default.Badge
                            "BLUEPRINT" -> Icons.Default.Architecture
                            else -> Icons.Default.Description
                        },
                        contentDescription = null,
                        tint = CamScannerTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Text("${document.pageCount} sheets", fontSize = 8.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Column {
                Text(
                    text = document.title,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${document.fileSizeFormatted} • ${document.category.take(8)}",
                    fontSize = 9.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onOpenStudio, modifier = Modifier.size(28.dp).testTag("grid_doc_edit_button")) {
                    Icon(Icons.Default.Edit, contentDescription = "Studio Editor", tint = CamScannerTeal, modifier = Modifier.size(16.dp))
                }

                IconButton(onClick = onOpenMore, modifier = Modifier.size(28.dp).testTag("grid_doc_more_button")) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 2: TOOLS (CamScanner Categorized Hub)
// -------------------------------------------------------------

@Composable
private fun ToolsTabContent(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    onLaunchWorkflow: (WorkflowShortcut) -> Unit
) {
    val context = LocalContext.current

    val toolsList = remember {
        listOf(
            ToolCardItem(
                title = "Word Opener",
                subtitle = "Open, read & convert .docx to PDF",
                icon = Icons.Default.Description,
                color = Color(0xFF2563EB)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.WORD_OPEN)
            },
            ToolCardItem(
                title = "Image to PDF",
                subtitle = "Photos & camera scans to PDF",
                icon = Icons.Default.PictureAsPdf,
                color = Color(0xFFEF4444)
            ) {
                viewModel.setPdfMakerOpen(true)
            },
            ToolCardItem(
                title = "PDF to Image",
                subtitle = "Export PDF pages as JPG images",
                icon = Icons.Default.Collections,
                color = Color(0xFF8B5CF6)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.PDF_TO_IMAGE)
            },
            ToolCardItem(
                title = "PDF Compressor",
                subtitle = "Reduce PDF file size up to 60%",
                icon = Icons.Default.Compress,
                color = Color(0xFF0284C7)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.COMPRESS)
            },
            ToolCardItem(
                title = "PDF Merger",
                subtitle = "Combine multiple PDFs into one",
                icon = Icons.Default.CallMerge,
                color = Color(0xFFEC4899)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.MERGE)
            },
            ToolCardItem(
                title = "PDF Splitter",
                subtitle = "Divide PDF into separate pages",
                icon = Icons.Default.CallSplit,
                color = Color(0xFFF59E0B)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.SPLIT)
            },
            ToolCardItem(
                title = "PDF Sign",
                subtitle = "Draw & stamp digital signatures",
                icon = Icons.Default.Draw,
                color = Color(0xFF10B981)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.SIGN)
            },
            ToolCardItem(
                title = "PDF Lock",
                subtitle = "Password protect & encrypt PDF",
                icon = Icons.Default.Lock,
                color = Color(0xFFDC2626)
            ) {
                viewModel.requestToolAction(com.example.model.ToolActionType.LOCK)
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
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
                        text = "Docs Z Tool Hub",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "8 Essential High-Speed Document Utilities",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CamScannerTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("PRO SUITE", color = CamScannerTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Render 8 Tools cleanly in pairs of 2 without heavy sub-grids
        items(toolsList.chunked(2)) { rowTools ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                for (tool in rowTools) {
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .height(105.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = tool.onClick)
                            .border(1.dp, tool.color.copy(alpha = 0.35f), RoundedCornerShape(14.dp))
                            .testTag("tool_card_${tool.title.lowercase().replace(" ", "_")}"),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(tool.color.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(tool.icon, contentDescription = null, tint = tool.color, modifier = Modifier.size(20.dp))
                                }
                                Icon(
                                    Icons.Default.ArrowOutward,
                                    contentDescription = null,
                                    tint = tool.color.copy(alpha = 0.7f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = tool.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    maxLines = 1
                                )
                                Text(
                                    text = tool.subtitle,
                                    fontSize = 9.5.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                if (rowTools.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private data class ToolCardItem(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val color: Color,
    val onClick: () -> Unit
)

// -------------------------------------------------------------
// TAB 3: SCAN STUDIO (Camera Shutter & Multi-photo Picker)
// -------------------------------------------------------------

@Composable
private fun ScanStudioTabContent(
    viewModel: DocumentViewModel,
    onCreatePdf: (title: String, bitmaps: List<Bitmap>, filter: String, extraWhiteBorder: Boolean) -> Unit,
    onOpenIdCard: () -> Unit
) {
    val context = LocalContext.current
    var docTitle by remember { mutableStateOf("Scan_${SimpleDateFormat("MMdd_HHmm", Locale.US).format(Date())}") }
    val capturedImages = remember { mutableStateListOf<Bitmap>() }
    var selectedFilter by remember { mutableStateOf("ORIGINAL") } // ORIGINAL, BW_DOCUMENT, GRAYSCALE
    var addExtraWhiteBorder by remember { mutableStateOf(true) }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                capturedImages.add(bitmap)
            }
        }
    )

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                try {
                    cameraLauncher.launch(null)
                } catch (e: Exception) {
                    Toast.makeText(context, "Could not open camera: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(context, "Camera permission is required to scan sheets. You can also pick photos from gallery.", Toast.LENGTH_LONG).show()
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(20),
        onResult = { uris ->
            uris.forEach { uri ->
                try {
                    PdfEngine.decodeSampledBitmapFromUri(context, uri, targetMaxDim = 1400)?.let {
                        capturedImages.add(it)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Image load error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Docs Z Camera Scanner", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Capture multi-page documents with filters", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CamScannerTeal.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text("HD SCAN", color = CamScannerTeal, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Camera Shutter & Pick Gallery Action Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            val hasCamPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.CAMERA
                            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                            if (hasCamPermission) {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Cannot open camera: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
                            }
                        }
                        .border(1.dp, CamScannerTeal.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CamScannerTeal.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CamScannerTeal),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Open Camera", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Snap sheets", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }

                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        }
                        .border(1.dp, CamScannerBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = CamScannerBlue.copy(alpha = 0.12f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(CamScannerBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("From Gallery", color = MaterialTheme.colorScheme.onSurface, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text("Pick photos", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
                    }
                }
            }
        }

        // ID Card Quick Scan Shortcut
        item {
            OutlinedButton(
                onClick = onOpenIdCard,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Badge, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Or Launch ID Card (2-in-1 Front + Back) Scanner", fontSize = 11.sp, color = CamScannerTeal)
            }
        }

        // Document Name Field
        item {
            OutlinedTextField(
                value = docTitle,
                onValueChange = { docTitle = it },
                label = { Text("Output PDF Name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp)
            )
        }

        // Enhancement Filter Chips
        item {
            Column {
                Text("FILTER ENHANCEMENT", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterOptionChip(label = "Original Colors", isSelected = selectedFilter == "ORIGINAL", onClick = { selectedFilter = "ORIGINAL" })
                    FilterOptionChip(label = "Magic B&W Document", isSelected = selectedFilter == "BW_DOCUMENT", onClick = { selectedFilter = "BW_DOCUMENT" })
                    FilterOptionChip(label = "Grayscale", isSelected = selectedFilter == "GRAYSCALE", onClick = { selectedFilter = "GRAYSCALE" })
                }
            }
        }

        // Captured Images Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("PAGES IN SCAN (${capturedImages.size})", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (capturedImages.isNotEmpty()) {
                    TextButton(onClick = { capturedImages.clear() }) {
                        Text("Clear All", color = Color(0xFFEF4444), fontSize = 11.sp)
                    }
                }
            }
        }

        item {
            if (capturedImages.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(36.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No photos captured yet", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                        Text("Tap 'Open Camera' or 'From Gallery' above to add sheets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(capturedImages) { index, bmp ->
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                        ) {
                            Image(
                                bitmap = bmp.asImageBitmap(),
                                contentDescription = "Page ${index + 1}",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            Box(
                                modifier = Modifier
                                    .padding(6.dp)
                                    .align(Alignment.TopStart)
                                    .clip(CircleShape)
                                    .background(CamScannerTeal)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("${index + 1}", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                            }
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

        // Extra White Border Toggle
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.BorderOuter, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Extra Whiter Border Margin", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                        }
                        Text("Add clean crisp white border frame around sheets", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = addExtraWhiteBorder,
                        onCheckedChange = { addExtraWhiteBorder = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = CamScannerTeal
                        )
                    )
                }
            }
        }

        // Generate PDF Button
        item {
            Button(
                onClick = {
                    if (capturedImages.isEmpty()) {
                        Toast.makeText(context, "Please capture at least 1 image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onCreatePdf(docTitle.trim().ifEmpty { "Scanned_PDF" }, capturedImages.toList(), selectedFilter, addExtraWhiteBorder)
                },
                enabled = capturedImages.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("pdf_maker_create_pdf_button")
            ) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Generate PDF (${capturedImages.size} pages)", fontSize = 13.sp, fontWeight = FontWeight.Bold)
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

// -------------------------------------------------------------
// TAB 4: FOLDERS (Directory Organizer)
// -------------------------------------------------------------

@Composable
private fun FoldersTabContent(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    onOpenDocument: (DocumentEntity) -> Unit
) {
    var newFolderName by remember { mutableStateOf("") }
    var showCreateDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Folders & Tags", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
                    Text("Organize documents into directories", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Button(
                    onClick = { showCreateDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.height(34.dp)
                ) {
                    Icon(Icons.Default.CreateNewFolder, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Folder", fontSize = 11.sp)
                }
            }
        }

        // Folders List
        items(uiState.foldersList) { folder ->
            val isSelected = uiState.selectedFolder == folder
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable {
                        viewModel.setSelectedFolder(folder)
                        viewModel.setNavTab(CamScannerNavTab.DOCS)
                    }
                    .border(
                        width = if (isSelected) 1.5.dp else 0.5.dp,
                        color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                        shape = RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CamScannerTeal.copy(alpha = 0.12f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(folder, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
                            val count = if (folder == "All Docs") {
                                uiState.documents.size
                            } else {
                                uiState.documents.count {
                                    it.folder.equals(folder, ignoreCase = true) ||
                                        (folder == "ID Cards" && it.category == "ID_CARD")
                                }
                            }
                            Text(
                                "$count ${if (count == 1) "document" else "documents"}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("Create New Folder") },
            text = {
                OutlinedTextField(
                    value = newFolderName,
                    onValueChange = { newFolderName = it },
                    placeholder = { Text("Folder Name") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.createFolder(newFolderName)
                        newFolderName = ""
                        showCreateDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateDialog = false }) { Text("Cancel") }
            }
        )
    }
}

// -------------------------------------------------------------
// TAB 5: SETTINGS & PREFERENCES
// -------------------------------------------------------------

@Composable
private fun SettingsTabContent(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Settings & Preferences", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Customize Docs Z theme and engine", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        // Appearance / Theme Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APPEARANCE & THEME", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 1: Primary Dark & Light Modes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            label = "Light",
                            icon = Icons.Default.LightMode,
                            isSelected = uiState.appThemeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Dark",
                            icon = Icons.Default.DarkMode,
                            isSelected = uiState.appThemeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "AMOLED",
                            icon = Icons.Default.NightlightRound,
                            isSelected = uiState.appThemeMode == AppThemeMode.OLED_BLACK,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.OLED_BLACK) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 2: Reading & Tinted Themes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            label = "Sepia",
                            icon = Icons.Default.MenuBook,
                            isSelected = uiState.appThemeMode == AppThemeMode.WARM_SEPIA,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.WARM_SEPIA) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Frost",
                            icon = Icons.Default.AcUnit,
                            isSelected = uiState.appThemeMode == AppThemeMode.NORDIC_FROST,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.NORDIC_FROST) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Amber",
                            icon = Icons.Default.WbSunny,
                            isSelected = uiState.appThemeMode == AppThemeMode.SUNSET_AMBER,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.SUNSET_AMBER) },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Row 3: Follow OS System Auto
                    ThemeOptionButton(
                        label = "System Auto (Follow Device Default)",
                        icon = Icons.Default.BrightnessAuto,
                        isSelected = uiState.appThemeMode == AppThemeMode.SYSTEM,
                        onClick = { viewModel.setAppThemeMode(AppThemeMode.SYSTEM) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // PDF Generation Settings
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("PDF EXPORT DEFAULTS", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingRow(title = "Default Page Size", value = "A4 (595 x 842 pt)")
                    SettingRow(title = "Scan Image Resolution", value = "300 DPI High-Definition")
                    SettingRow(title = "Compressor Algorithm", value = "DCT Optimized Downsampling")
                    SettingRow(title = "Intent Integration", value = "Open with Docs Z (Active)")
                }
            }
        }

        // Storage & App Info
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("APPLICATION INFO", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(10.dp))

                    SettingRow(title = "Application Name", value = "Docs Z")
                    SettingRow(title = "Version", value = "2.0.0 (CamScanner Edition)")
                    SettingRow(title = "Engine", value = "Document OS Multi-Layer PDF & CAD")
                    SettingRow(title = "Database", value = "Room SQLite Encrypted")
                }
            }
        }

        // Official App Attribution Card (Kept gracefully at the very bottom/last)
        item {
            PremiumOwnerCard()
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun ThemeOptionButton(
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CamScannerTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.onSurface, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(label, fontSize = 9.sp, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun SettingRow(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface)
        Text(value, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = CamScannerTeal)
    }
}

/**
 * Official App Attribution Card:
 * Clean, luxury Material 3 design sitting gracefully at the bottom of Settings.
 */
@Composable
private fun PremiumOwnerCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = 1.dp,
                color = CamScannerTeal.copy(alpha = 0.35f),
                shape = RoundedCornerShape(18.dp)
            )
            .testTag("owner_official_card"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = CamScannerTeal.copy(alpha = 0.12f),
                border = BorderStroke(1.dp, CamScannerTeal.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Verified,
                        contentDescription = "Verified Creator",
                        tint = CamScannerTeal,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        "OFFICIAL DEVELOPER",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = CamScannerTeal,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "This app is made by",
                fontSize = 14.sp,
                fontWeight = FontWeight.Normal,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Owner_Official",
                fontSize = 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = CamScannerTeal,
                textAlign = TextAlign.Center,
                letterSpacing = 0.4.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Lead Architect & Master Creator",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )
        }
    }
}

// -------------------------------------------------------------
// FILTER CHIP & PLACEHOLDER HELPERS
// -------------------------------------------------------------

@Composable
fun FilterOptionChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) CamScannerTeal else MaterialTheme.colorScheme.surfaceVariant)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun EmptyDocumentsPlaceholder(
    onOpenScan: () -> Unit,
    onOpenImport: () -> Unit,
    onOpenCreate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, CamScannerTeal.copy(alpha = 0.25f), RoundedCornerShape(18.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(CamScannerTeal.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.DocumentScanner,
                    contentDescription = null,
                    tint = CamScannerTeal,
                    modifier = Modifier.size(34.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                "Docs Z Vault is Clean & Fresh",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "No documents stored yet. Use the HD camera to scan papers, or import PDF files directly from your device.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 17.sp,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenScan,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Sheet", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                OutlinedButton(
                    onClick = onOpenImport,
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CamScannerTeal),
                    border = BorderStroke(1.2.dp, CamScannerTeal),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
