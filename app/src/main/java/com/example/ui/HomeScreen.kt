package com.example.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
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

            matchesSearch && matchesCategory
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
            when (uiState.activeNavTab) {
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
                        onCreatePdf = { title, bmps, filter ->
                            viewModel.createPdfFromScannedImages(title, bmps, filter, context)
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
                    val nextTheme = when (uiState.appThemeMode) {
                        AppThemeMode.LIGHT -> AppThemeMode.DARK
                        AppThemeMode.DARK -> AppThemeMode.SYSTEM
                        AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
                    }
                    viewModel.setAppThemeMode(nextTheme)
                },
                onOpenSortMenu = { showSortMenu = true },
                onImportFiles = {
                    galleryPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
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

            // 2. Folder Tabs & Filter Chips Row
            item {
                CategoryChipsRow(
                    selectedCategory = uiState.selectedCategoryFilter,
                    onSelectCategory = { viewModel.setCategoryFilter(it) }
                )
            }

            // 3. Featured / Recent Scanned Document Resume Card
            if (activeDoc != null && activeTab != null) {
                item {
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

            // 4. Section Header with View All & Multi-select trigger
            item {
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

            // 5. Documents Grid or List
            if (filteredDocs.isEmpty()) {
                item {
                    EmptyDocumentsPlaceholder(
                        onOpenScan = { viewModel.setNavTab(CamScannerNavTab.SCAN) },
                        onOpenCreate = { viewModel.setCreateProjectDialogOpen(true) }
                    )
                }
            } else {
                if (uiState.docViewMode == DocViewMode.LIST) {
                    items(filteredDocs, key = { it.id }) { doc ->
                        DocZListItemCard(
                            document = doc,
                            isSelected = uiState.selectedDocIds.contains(doc.id),
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            onToggleSelect = { viewModel.toggleSelectDoc(doc.id) },
                            onOpen = { onOpenDocument(doc) },
                            onRename = { viewModel.setRenameDialogOpen(true, doc) },
                            onShare = {
                                viewModel.openDocument(doc)
                                viewModel.exportDocument(context)
                            },
                            onCompress = { viewModel.openCompressorForDoc(doc) },
                            onOcr = { viewModel.extractTextFromDoc(doc) },
                            onMoveFolder = { viewModel.setMoveFolderDialogOpen(true, doc) },
                            onSplit = { viewModel.setSplitDocDialogOpen(true, doc) },
                            onDuplicate = { viewModel.duplicateDocument(doc) },
                            onDelete = { viewModel.deleteDocument(doc.id) }
                        )
                    }
                } else {
                    // Grid View
                    item {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 1000.dp),
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
                                    onRename = { viewModel.setRenameDialogOpen(true, doc) },
                                    onShare = {
                                        viewModel.openDocument(doc)
                                        viewModel.exportDocument(context)
                                    },
                                    onCompress = { viewModel.openCompressorForDoc(doc) },
                                    onOcr = { viewModel.extractTextFromDoc(doc) },
                                    onDelete = { viewModel.deleteDocument(doc.id) }
                                )
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .statusBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // App Identity & Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search docs, tags, text...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = CamScannerTeal,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { onSearchChange("") }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(46.dp)
                    .testTag("home_search_input"),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedBorderColor = CamScannerTeal,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(24.dp)
            )

            // View Mode Toggle (List vs Grid)
            IconButton(
                onClick = onToggleViewMode,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = if (docViewMode == DocViewMode.LIST) Icons.Default.GridView else Icons.Default.ViewList,
                    contentDescription = "Toggle View",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Sort Selector
            IconButton(
                onClick = onOpenSortMenu,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Sort,
                    contentDescription = "Sort",
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Theme Toggle (Light ☀️ / Dark 🌙 / System ⚙️)
            IconButton(
                onClick = onToggleTheme,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.surface)
                    .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), CircleShape)
            ) {
                Icon(
                    imageVector = when (appThemeMode) {
                        AppThemeMode.LIGHT -> Icons.Default.LightMode
                        AppThemeMode.DARK -> Icons.Default.DarkMode
                        AppThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                    },
                    contentDescription = "Theme Mode",
                    tint = CamScannerTeal,
                    modifier = Modifier.size(18.dp)
                )
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
        border = BorderStroke(1.dp, CamScannerTeal.copy(alpha = 0.4f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(CamScannerTeal)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("ACTIVE WORKSPACE", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sheet ${tab.activePageIndex + 1} of ${tab.pageCount}",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp).testTag("resume_workspace_button")
                ) {
                    Text("Open", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = doc.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "${doc.category.replace("_", " ")} • ${doc.fileSizeFormatted} • Scale: 100px = ${doc.scaleRealDistance} ${doc.scaleUnit}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Actions on Active Doc
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = onExport,
                    label = { Text("Share PDF", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamScannerTeal) },
                    modifier = Modifier.height(28.dp)
                )
                AssistChip(
                    onClick = onCompress,
                    label = { Text("Compress", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamScannerBlue) },
                    modifier = Modifier.height(28.dp)
                )
                AssistChip(
                    onClick = onOcr,
                    label = { Text("Extract Text", fontSize = 10.sp) },
                    leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(12.dp), tint = CamScannerPurple) },
                    modifier = Modifier.height(28.dp)
                )
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
    onRename: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit,
    onOcr: () -> Unit,
    onMoveFolder: () -> Unit,
    onSplit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
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

            // 3-dot context menu
            Box {
                IconButton(onClick = { showMenu = true }, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.MoreVert, contentDescription = "More actions", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Open Workspace") },
                        onClick = { showMenu = false; onOpen() },
                        leadingIcon = { Icon(Icons.Default.OpenInNew, contentDescription = null, tint = CamScannerTeal) }
                    )
                    DropdownMenuItem(
                        text = { Text("Rename") },
                        onClick = { showMenu = false; onRename() },
                        leadingIcon = { Icon(Icons.Default.DriveFileRenameOutline, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Share / Export PDF") },
                        onClick = { showMenu = false; onShare() },
                        leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Compress PDF") },
                        onClick = { showMenu = false; onCompress() },
                        leadingIcon = { Icon(Icons.Default.Compress, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Extract Text (OCR)") },
                        onClick = { showMenu = false; onOcr() },
                        leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Move to Folder") },
                        onClick = { showMenu = false; onMoveFolder() },
                        leadingIcon = { Icon(Icons.Default.FolderOpen, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Split Document") },
                        onClick = { showMenu = false; onSplit() },
                        leadingIcon = { Icon(Icons.Default.CallSplit, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Duplicate") },
                        onClick = { showMenu = false; onDuplicate() },
                        leadingIcon = { Icon(Icons.Default.ControlPointDuplicate, contentDescription = null) }
                    )
                    HorizontalDivider()
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color(0xFFEF4444)) },
                        onClick = { showMenu = false; onDelete() },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFEF4444)) }
                    )
                }
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
    onRename: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit,
    onOcr: () -> Unit,
    onDelete: () -> Unit
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
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onShare, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
                }
                IconButton(onClick = onCompress, modifier = Modifier.size(24.dp)) {
                    Icon(Icons.Default.Compress, contentDescription = "Compress", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(14.dp))
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Docs Z Tool Hub",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "All mobile scanning, PDF conversion & editor utilities",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Section 1: Scan & Capture
        item {
            ToolCategorySection(
                title = "SCAN & CAPTURE",
                tools = listOf(
                    ToolCardItem("ID Card (2-in-1)", "Front & back onto one A4 sheet", Icons.Default.Badge, Color(0xFF00897B)) {
                        viewModel.setIdCardScannerOpen(true)
                    },
                    ToolCardItem("Smart Camera Scan", "Auto-crop, magic color & high-res", Icons.Default.CameraAlt, Color(0xFF10B981)) {
                        viewModel.setNavTab(CamScannerNavTab.SCAN)
                    },
                    ToolCardItem("Book Scan", "Two-page spread split format", Icons.Default.MenuBook, Color(0xFF0284C7)) {
                        viewModel.setNavTab(CamScannerNavTab.SCAN)
                    },
                    ToolCardItem("Batch Scan", "Multi-sheet rapid capture", Icons.Default.Collections, Color(0xFF8B5CF6)) {
                        viewModel.setNavTab(CamScannerNavTab.SCAN)
                    }
                )
            )
        }

        // Section 2: Convert & Export
        item {
            ToolCategorySection(
                title = "CONVERT & EXPORT",
                tools = listOf(
                    ToolCardItem("Image to PDF", "Photos to standardized PDF", Icons.Default.PictureAsPdf, Color(0xFFEF4444)) {
                        viewModel.setNavTab(CamScannerNavTab.SCAN)
                    },
                    ToolCardItem("To Word / Text OCR", "Extract text & data fields", Icons.Default.TextFields, Color(0xFF06B6D4)) {
                        val target = uiState.activeDocument ?: uiState.documents.firstOrNull()
                        if (target != null) viewModel.extractTextFromDoc(target)
                        else Toast.makeText(context, "No doc loaded", Toast.LENGTH_SHORT).show()
                    },
                    ToolCardItem("Merge PDFs", "Combine multiple documents", Icons.Default.CallMerge, Color(0xFFEC4899)) {
                        viewModel.setMergeDocsDialogOpen(true)
                    },
                    ToolCardItem("Split PDF", "Extract or divide pages", Icons.Default.CallSplit, Color(0xFFF59E0B)) {
                        val target = uiState.activeDocument ?: uiState.documents.firstOrNull()
                        if (target != null) viewModel.setSplitDocDialogOpen(true, target)
                        else Toast.makeText(context, "No doc loaded", Toast.LENGTH_SHORT).show()
                    }
                )
            )
        }

        // Section 3: PDF Utilities & Edit
        item {
            ToolCategorySection(
                title = "PDF UTILITIES & EDIT",
                tools = listOf(
                    ToolCardItem("PDF Compressor", "Reduce file size with live preview", Icons.Default.Compress, Color(0xFF0284C7)) {
                        val target = uiState.activeDocument ?: uiState.documents.firstOrNull()
                        if (target != null) viewModel.openCompressorForDoc(target)
                    },
                    ToolCardItem("Markup & Annotate", "Pen, highlighter, shapes & stamps", Icons.Default.Edit, Color(0xFF38BDF8)) {
                        onLaunchWorkflow(WorkflowShortcut.MARKUP_ANNOTATE)
                    },
                    ToolCardItem("CAD Measurements", "Calibrated scales, lengths & areas", Icons.Default.SquareFoot, Color(0xFFF59E0B)) {
                        onLaunchWorkflow(WorkflowShortcut.CAD_MEASURE)
                    },
                    ToolCardItem("Digital Signatures", "Interactive fill & crypto certificates", Icons.Default.AssignmentTurnedIn, Color(0xFF10B981)) {
                        onLaunchWorkflow(WorkflowShortcut.FORMS_AND_SIGN)
                    },
                    ToolCardItem("Page Assembly", "Reorder, rotate 90° & duplicate", Icons.Default.AutoStories, Color(0xFF6366F1)) {
                        onLaunchWorkflow(WorkflowShortcut.PAGE_ASSEMBLY)
                    },
                    ToolCardItem("Security & Watermark", "AES password lock & watermark", Icons.Default.Security, Color(0xFFEF4444)) {
                        onLaunchWorkflow(WorkflowShortcut.SECURITY_WATERMARK)
                    }
                )
            )
        }

        item {
            Spacer(modifier = Modifier.height(20.dp))
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

@Composable
private fun ToolCategorySection(
    title: String,
    tools: List<ToolCardItem>
) {
    Column {
        Text(
            text = title,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 400.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(tools) { item ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(86.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = item.onClick)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(item.color.copy(alpha = 0.14f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(item.icon, contentDescription = null, tint = item.color, modifier = Modifier.size(18.dp))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.title, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface, maxLines = 1)
                            Text(item.subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2, lineHeight = 11.sp)
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 3: SCAN STUDIO (Camera Shutter & Multi-photo Picker)
// -------------------------------------------------------------

@Composable
private fun ScanStudioTabContent(
    viewModel: DocumentViewModel,
    onCreatePdf: (title: String, bitmaps: List<Bitmap>, filter: String) -> Unit,
    onOpenIdCard: () -> Unit
) {
    val context = LocalContext.current
    var docTitle by remember { mutableStateOf("Scan_${SimpleDateFormat("MMdd_HHmm", Locale.US).format(Date())}") }
    val capturedImages = remember { mutableStateListOf<Bitmap>() }
    var selectedFilter by remember { mutableStateOf("ORIGINAL") } // ORIGINAL, BW_DOCUMENT, GRAYSCALE

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { bitmap ->
            if (bitmap != null) {
                capturedImages.add(bitmap)
            }
        }
    )

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(20),
        onResult = { uris ->
            uris.forEach { uri ->
                try {
                    context.contentResolver.openInputStream(uri)?.use { stream ->
                        BitmapFactory.decodeStream(stream)?.let { capturedImages.add(it) }
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
                        .clickable { cameraLauncher.launch(null) }
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

        // Generate PDF Button
        item {
            Button(
                onClick = {
                    if (capturedImages.isEmpty()) {
                        Toast.makeText(context, "Please capture at least 1 image", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    onCreatePdf(docTitle.trim().ifEmpty { "Scanned_PDF" }, capturedImages.toList(), selectedFilter)
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
                            Text(
                                if (folder == "All Docs") "${uiState.documents.size} total items" else "Folder collection",
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

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ThemeOptionButton(
                            label = "Light (White)",
                            icon = Icons.Default.LightMode,
                            isSelected = uiState.appThemeMode == AppThemeMode.LIGHT,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.LIGHT) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "Dark (Black)",
                            icon = Icons.Default.DarkMode,
                            isSelected = uiState.appThemeMode == AppThemeMode.DARK,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.DARK) },
                            modifier = Modifier.weight(1f)
                        )
                        ThemeOptionButton(
                            label = "System Auto",
                            icon = Icons.Default.BrightnessAuto,
                            isSelected = uiState.appThemeMode == AppThemeMode.SYSTEM,
                            onClick = { viewModel.setAppThemeMode(AppThemeMode.SYSTEM) },
                            modifier = Modifier.weight(1f)
                        )
                    }
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

        item {
            Spacer(modifier = Modifier.height(20.dp))
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
    onOpenCreate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(Icons.Default.FolderOpen, contentDescription = null, tint = CamScannerTeal, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(10.dp))
            Text("No Documents in Repository", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text("Scan a document or import a PDF from your phone", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Button(
                    onClick = onOpenScan,
                    colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Scan Document")
                }
            }
        }
    }
}
