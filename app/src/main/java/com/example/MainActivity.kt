package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.*
import com.example.ui.AppScreen
import com.example.ui.DocumentUiState
import com.example.ui.DocumentViewModel
import com.example.ui.HomeScreen
import com.example.ui.PdfNormalReaderScreen
import com.example.ui.canvas.DocumentCanvas
import com.example.ui.components.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle external PDF opening intent
        intent?.let { viewModel.handleIncomingIntent(it, this) }

        setContent {
            val uiState by viewModel.uiState.collectAsStateWithLifecycle()
            val isSystemDark = isSystemInDarkTheme()
            val isDarkTheme = when (uiState.appThemeMode) {
                AppThemeMode.LIGHT -> false
                AppThemeMode.DARK -> true
                AppThemeMode.SYSTEM -> isSystemDark
            }

            MyApplicationTheme(darkTheme = isDarkTheme) {
                val context = LocalContext.current

                // Runtime notification permission request for Android 13+ (TIRAMISU / API 33+)
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* granted or denied */ }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        if (ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // BackHandler returns to Home screen if currently in workspace or reader
                BackHandler(enabled = uiState.currentScreen != AppScreen.HOME) {
                    viewModel.navigateToHome()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    contentWindowInsets = WindowInsets(0, 0, 0, 0)
                ) { _ ->
                    when (uiState.currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onOpenDocument = { viewModel.openDocument(it) },
                                onLaunchWorkflow = { viewModel.launchWorkflow(it) },
                                onResumeWorkspace = { viewModel.navigateToWorkspace() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppScreen.READER -> {
                            PdfNormalReaderScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onBack = { viewModel.navigateToHome() },
                                onOpenInStudio = { doc -> viewModel.openDocumentInStudio(doc) },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        AppScreen.WORKSPACE -> {
                            DocumentOsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateHome = { viewModel.navigateToHome() },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // -------------------------------------------------------------
                    // Global Dialogs & Modals
                    // -------------------------------------------------------------

                    // 1. ID Card 2-in-1 Scanner Dialog
                    if (uiState.isIdCardScannerOpen) {
                        IdCardScannerDialog(
                            onDismiss = { viewModel.setIdCardScannerOpen(false) },
                            onCreateIdCard = { title, frontBmp, backBmp ->
                                viewModel.createIdCardDocument(title, frontBmp, backBmp, context)
                            }
                        )
                    }

                    // 2. Merge Documents Dialog
                    if (uiState.isMergeDocsDialogOpen) {
                        MergeDocumentsDialog(
                            documents = uiState.documents,
                            onDismiss = { viewModel.setMergeDocsDialogOpen(false) },
                            onMerge = { docIds, title ->
                                viewModel.mergeDocuments(docIds, title, context)
                            }
                        )
                    }

                    // 3. Split Document Dialog
                    if (uiState.isSplitDocDialogOpen && uiState.docToSplit != null) {
                        SplitDocumentDialog(
                            document = uiState.docToSplit!!,
                            onDismiss = { viewModel.setSplitDocDialogOpen(false) },
                            onSplit = { doc, splitAfter ->
                                viewModel.splitDocument(doc, splitAfter, context)
                            }
                        )
                    }

                    // 4. Extracted OCR Text Viewer Dialog
                    if (uiState.isOcrViewerOpen) {
                        OcrTextViewerDialog(
                            docTitle = uiState.ocrDocTitle,
                            extractedText = uiState.extractedOcrText,
                            onDismiss = { viewModel.setOcrViewerOpen(false) }
                        )
                    }

                    // 5. Rename Document Dialog
                    if (uiState.isRenameDialogOpen && uiState.docToRename != null) {
                        RenameDocumentDialog(
                            document = uiState.docToRename!!,
                            onDismiss = { viewModel.setRenameDialogOpen(false) },
                            onRename = { newTitle ->
                                viewModel.renameDocument(uiState.docToRename!!.id, newTitle)
                            }
                        )
                    }

                    // 6. Move to Folder Dialog
                    if (uiState.isMoveFolderDialogOpen && uiState.docToMove != null) {
                        MoveToFolderDialog(
                            document = uiState.docToMove!!,
                            folders = uiState.foldersList,
                            onDismiss = { viewModel.setMoveFolderDialogOpen(false) },
                            onSelectFolder = { folder ->
                                viewModel.moveDocumentToFolder(uiState.docToMove!!.id, folder)
                            },
                            onCreateFolder = { folderName ->
                                viewModel.createFolder(folderName)
                            }
                        )
                    }

                    // 7. Image to PDF Studio Dialog
                    if (uiState.isPdfMakerOpen) {
                        PdfMakerStudioDialog(
                            onDismiss = { viewModel.setPdfMakerOpen(false) },
                            onCreatePdf = { title, bitmaps, filter ->
                                viewModel.createPdfFromScannedImages(title, bitmaps, filter, context)
                            }
                        )
                    }

                    // 8. PDF Compressor Studio Dialog
                    if (uiState.isCompressorOpen) {
                        val targetDoc = uiState.compressTargetDoc ?: uiState.activeDocument ?: uiState.documents.firstOrNull()
                        PdfCompressorDialog(
                            document = targetDoc,
                            allDocuments = uiState.documents,
                            onDismiss = { viewModel.setCompressorOpen(false) },
                            onCompress = { doc, quality ->
                                viewModel.compressDocument(doc, quality, context)
                            }
                        )
                    }

                    // 9. Blank Project / Blueprint Dialog
                    if (uiState.isCreateProjectDialogOpen) {
                        CreateProjectDialog(
                            onDismiss = { viewModel.setCreateProjectDialogOpen(false) },
                            onCreateProject = { title, category, sheetCount ->
                                viewModel.createAndOpenNewProject(title, category, sheetCount)
                            }
                        )
                    }

                    // 10. Document Library Dialog
                    if (uiState.isDocLibraryOpen) {
                        DocumentLibraryDialog(
                            documents = uiState.documents,
                            onSelectDocument = { viewModel.openDocument(it) },
                            onCreateNewDocument = {
                                viewModel.setCreateProjectDialogOpen(true)
                                viewModel.setDocLibraryOpen(false)
                            },
                            onDismiss = { viewModel.setDocLibraryOpen(false) }
                        )
                    }

                    // 11. Build Engine Hub Dialog
                    if (uiState.isBuildDialogOpen) {
                        BuildWorkflowDialog(
                            isBuilding = uiState.isBuildingApp,
                            buildProgress = uiState.buildProgress,
                            buildStatusText = uiState.buildStatusText,
                            buildSteps = uiState.buildSteps,
                            onTriggerBuild = { viewModel.triggerInAppBuild(context) },
                            onDismiss = { viewModel.setBuildDialogOpen(false) }
                        )
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        viewModel.handleIncomingIntent(intent, this)
    }
}

@Composable
fun DocumentOsScreen(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    onNavigateHome: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeTab = uiState.tabs.find { it.id == uiState.activeTabId }
    val activeDoc = uiState.activeDocument
    val renderedBitmap by viewModel.renderedPageBitmap.collectAsStateWithLifecycle()

    val activeDocId = activeDoc?.id ?: ""
    val currentAnnotations = uiState.annotations[activeDocId] ?: emptyList()
    val currentMeasurements = uiState.measurements[activeDocId] ?: emptyList()
    val currentFields = uiState.formFields[activeDocId] ?: emptyList()

    val currentPage = (activeTab?.activePageIndex ?: 0) + 1
    val totalPages = activeTab?.pageCount ?: 1

    var showMoreMenu by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Sleek Modern PDF Top Bar (Full Screen Status Bar Compatible)
            ModernPdfTopBar(
                title = activeDoc?.title ?: "Docs Z",
                category = activeTab?.category ?: DocumentCategory.CONTRACT,
                fileSize = activeDoc?.fileSizeFormatted ?: "2.4 MB",
                currentPage = currentPage,
                totalPages = totalPages,
                onNavigateHome = onNavigateHome,
                onSwitchToReader = { viewModel.switchToReader() },
                onOpenExport = { viewModel.exportDocument(context) },
                onOpenPageManager = { viewModel.setPageManagerOpen(true) },
                onOpenSecurity = { viewModel.setSecurityDialogOpen(true) },
                showMoreMenu = showMoreMenu,
                onToggleMoreMenu = { showMoreMenu = !showMoreMenu },
                onDismissMoreMenu = { showMoreMenu = false },
                onCompress = {
                    showMoreMenu = false
                    if (activeDoc != null) viewModel.openCompressorForDoc(activeDoc)
                },
                onSign = {
                    showMoreMenu = false
                    viewModel.setSignatureDialogOpen(true)
                },
                onCalibrate = {
                    showMoreMenu = false
                    viewModel.setScaleDialogOpen(true)
                },
                onOcr = {
                    showMoreMenu = false
                    if (activeDoc != null) viewModel.extractTextFromDoc(activeDoc)
                }
            )

            // 2. High-Performance Document Canvas Viewport
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DocumentCanvas(
                    category = activeTab?.category ?: DocumentCategory.CONTRACT,
                    pageIndex = activeTab?.activePageIndex ?: 0,
                    pageCount = activeTab?.pageCount ?: 1,
                    documentTitle = activeDoc?.title ?: "Docs Z",
                    watermarkText = activeDoc?.watermarkText ?: "",
                    zoomLevel = activeTab?.zoomLevel ?: 1.0f,
                    panOffsetX = activeTab?.panOffsetX ?: 0f,
                    panOffsetY = activeTab?.panOffsetY ?: 0f,
                    onTransform = { zoomChange, panDx, panDy ->
                        viewModel.handleTransform(zoomChange, panDx, panDy)
                    },
                    toolMode = uiState.toolMode,
                    markupType = uiState.markupType,
                    activeColorHex = uiState.activeColorHex,
                    strokeWidth = uiState.strokeWidth,
                    selectedStamp = uiState.selectedStamp,
                    annotations = currentAnnotations,
                    onAddAnnotation = { viewModel.addAnnotation(it) },
                    onDeleteAnnotation = { viewModel.deleteAnnotation(it) },
                    measurements = currentMeasurements,
                    measurementType = uiState.measurementType,
                    activeScale = uiState.activeScale,
                    onAddMeasurement = { viewModel.addMeasurement(it) },
                    formFields = currentFields,
                    onFormFieldClick = { viewModel.onFormFieldClick(it) },
                    renderedPageBitmap = renderedBitmap
                )

                // Floating Zoom HUD in Top-Right
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                    tonalElevation = 4.dp,
                    shadowElevation = 8.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        IconButton(onClick = { viewModel.zoomOut() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Remove, "Zoom Out", modifier = Modifier.size(15.dp))
                        }
                        Text(
                            "${((activeTab?.zoomLevel ?: 1.0f) * 100).toInt()}%",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                        IconButton(onClick = { viewModel.zoomIn() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.Add, "Zoom In", modifier = Modifier.size(15.dp))
                        }
                        IconButton(onClick = { viewModel.zoomFit() }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.FitScreen, "Fit", modifier = Modifier.size(15.dp), tint = CamScannerTeal)
                        }
                    }
                }

                // Floating Undo / Redo in Top-Left (when in editing modes)
                if (uiState.toolMode == WorkspaceToolMode.MARKUP_DRAW) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                        shadowElevation = 8.dp,
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            IconButton(onClick = { viewModel.undo() }, enabled = viewModel.canUndo(), modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Default.Undo,
                                    "Undo",
                                    tint = if (viewModel.canUndo()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            IconButton(onClick = { viewModel.redo() }, enabled = viewModel.canRedo(), modifier = Modifier.size(28.dp)) {
                                Icon(
                                    Icons.Default.Redo,
                                    "Redo",
                                    tint = if (viewModel.canRedo()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }

                // CAD Mini-Map Navigator (when toggled on)
                if (uiState.showMiniMap && activeTab?.category == DocumentCategory.BLUEPRINT) {
                    CadMiniMap(
                        category = activeTab.category,
                        zoomLevel = activeTab.zoomLevel,
                        panOffsetX = activeTab.panOffsetX,
                        panOffsetY = activeTab.panOffsetY,
                        canvasViewportWidth = 600f,
                        canvasViewportHeight = 800f,
                        docWidth = 720f,
                        docHeight = 980f,
                        onNavigate = { newPanX, newPanY -> viewModel.setPanOffset(newPanX, newPanY) },
                        onClose = { viewModel.toggleMiniMap() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = 80.dp)
                    )
                }
            }

            // 3. Contextual Drawer (Only when an active editing tool is selected)
            AnimatedVisibility(visible = uiState.toolMode == WorkspaceToolMode.MARKUP_DRAW) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    MarkupToolDrawer(
                        selectedMarkup = uiState.markupType,
                        onMarkupSelect = { viewModel.setMarkupType(it) },
                        activeColorHex = uiState.activeColorHex,
                        onColorSelect = { viewModel.setActiveColor(it) },
                        strokeWidth = uiState.strokeWidth,
                        onStrokeWidthChange = { viewModel.setStrokeWidth(it) },
                        selectedStamp = uiState.selectedStamp,
                        onStampSelect = { viewModel.setSelectedStamp(it) }
                    )
                }
            }

            AnimatedVisibility(visible = uiState.toolMode == WorkspaceToolMode.CAD_MEASURE) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 4.dp,
                    border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                ) {
                    MeasurementToolbar(
                        activeScale = uiState.activeScale,
                        selectedMeasurementType = uiState.measurementType,
                        onMeasurementTypeSelect = { viewModel.setMeasurementType(it) },
                        onOpenCalibration = { viewModel.setScaleDialogOpen(true) },
                        onOpenTakeoffSummary = { viewModel.setTakeoffDialogOpen(true) },
                        measurementsCount = currentMeasurements.size
                    )
                }
            }

            // 4. Floating Capsule Action & Page Navigation Bar
            FloatingCapsulePdfDock(
                activeToolMode = uiState.toolMode,
                currentPage = currentPage,
                totalPages = totalPages,
                onSelectToolMode = { mode ->
                    if (uiState.toolMode == mode) {
                        viewModel.setToolMode(WorkspaceToolMode.VIEW_NAVIGATE)
                    } else {
                        viewModel.setToolMode(mode)
                    }
                },
                onPreviousPage = {
                    val curr = activeTab?.activePageIndex ?: 0
                    if (curr > 0) viewModel.setActivePage(curr - 1)
                },
                onNextPage = {
                    val curr = activeTab?.activePageIndex ?: 0
                    val total = activeTab?.pageCount ?: 1
                    if (curr < total - 1) viewModel.setActivePage(curr + 1)
                },
                onOpenPageManager = { viewModel.setPageManagerOpen(true) }
            )
        }

        // 5. Global Document Workspace Modals
        if (uiState.isScaleDialogOpen) {
            ScaleCalibrationDialog(
                currentScale = uiState.activeScale,
                onDismiss = { viewModel.setScaleDialogOpen(false) },
                onSaveScale = { viewModel.saveScale(it) }
            )
        }

        if (uiState.isTakeoffDialogOpen) {
            ScaleTakeoffDialog(
                measurements = currentMeasurements,
                activeScale = uiState.activeScale,
                onDeleteMeasurement = { viewModel.deleteMeasurement(it) },
                onDismiss = { viewModel.setTakeoffDialogOpen(false) }
            )
        }

        if (uiState.isSignatureDialogOpen) {
            SignaturePadDialog(
                onDismiss = { viewModel.setSignatureDialogOpen(false) },
                onSignatureConfirmed = { viewModel.applyDigitalSignature(it) }
            )
        }

        if (uiState.isPageManagerOpen) {
            PageManagerDialog(
                pageCount = activeTab?.pageCount ?: 1,
                activePageIndex = activeTab?.activePageIndex ?: 0,
                onSelectPage = {
                    viewModel.setActivePage(it)
                    viewModel.setPageManagerOpen(false)
                },
                onRotatePage = { viewModel.rotateActivePage(it) },
                onDuplicatePage = { viewModel.duplicatePage(it) },
                onDeletePage = { viewModel.deletePage(it) },
                onInsertBlankPage = { viewModel.insertBlankPage() },
                onDismiss = { viewModel.setPageManagerOpen(false) }
            )
        }

        if (uiState.isSecurityDialogOpen) {
            SecurityWatermarkDialog(
                currentWatermark = activeDoc?.watermarkText ?: "",
                isPasswordProtected = activeDoc?.isPasswordProtected ?: false,
                onSaveSecurity = { watermark, pwd ->
                    viewModel.saveSecuritySettings(watermark, pwd)
                },
                onDismiss = { viewModel.setSecurityDialogOpen(false) }
            )
        }
    }
}

// -------------------------------------------------------------
// CLEAN & PROFESSIONAL PDF TOP APP BAR
// -------------------------------------------------------------

@Composable
private fun ModernPdfTopBar(
    title: String,
    category: DocumentCategory,
    fileSize: String,
    currentPage: Int,
    totalPages: Int,
    onNavigateHome: () -> Unit,
    onSwitchToReader: () -> Unit,
    onOpenExport: () -> Unit,
    onOpenPageManager: () -> Unit,
    onOpenSecurity: () -> Unit,
    showMoreMenu: Boolean,
    onToggleMoreMenu: () -> Unit,
    onDismissMoreMenu: () -> Unit,
    onCompress: () -> Unit,
    onSign: () -> Unit,
    onCalibrate: () -> Unit,
    onOcr: () -> Unit
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
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Back Home Arrow & Document Information
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onNavigateHome,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CamScannerTeal.copy(alpha = 0.12f))
                        .testTag("nav_home_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to Home",
                        tint = CamScannerTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CamScannerTeal.copy(alpha = 0.15f))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = category.name.replace("_", " "),
                                color = CamScannerTeal,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "p.$currentPage/$totalPages • $fileSize",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 10.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(6.dp))

            // Right: Reader switch, Share, and Tools Menu
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onSwitchToReader,
                    modifier = Modifier.size(36.dp).testTag("switch_to_reader_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.MenuBook,
                        contentDescription = "Normal Reader",
                        tint = CamScannerTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                IconButton(
                    onClick = onOpenExport,
                    modifier = Modifier.size(36.dp).testTag("open_export_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share PDF",
                        tint = CamScannerTeal,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Box {
                    IconButton(
                        onClick = onToggleMoreMenu,
                        modifier = Modifier.size(36.dp).testTag("top_bar_more_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showMoreMenu,
                        onDismissRequest = onDismissMoreMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text("Visual Page Assembly") },
                            leadingIcon = { Icon(Icons.Default.AutoStories, contentDescription = null, tint = CamScannerTeal) },
                            onClick = {
                                onDismissMoreMenu()
                                onOpenPageManager()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Security Policies") },
                            leadingIcon = { Icon(Icons.Default.Security, contentDescription = null, tint = Color(0xFFF59E0B)) },
                            onClick = {
                                onDismissMoreMenu()
                                onOpenSecurity()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Compress PDF") },
                            leadingIcon = { Icon(Icons.Default.Compress, contentDescription = null, tint = CamScannerTeal) },
                            onClick = onCompress
                        )
                        DropdownMenuItem(
                            text = { Text("Digital Signature") },
                            leadingIcon = { Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = CamScannerTeal) },
                            onClick = onSign
                        )
                        DropdownMenuItem(
                            text = { Text("CAD Calibration & Takeoff") },
                            leadingIcon = { Icon(Icons.Default.SquareFoot, contentDescription = null, tint = Color(0xFFF59E0B)) },
                            onClick = onCalibrate
                        )
                        DropdownMenuItem(
                            text = { Text("Extract Text (OCR)") },
                            leadingIcon = { Icon(Icons.Default.TextFields, contentDescription = null, tint = Color(0xFF8B5CF6)) },
                            onClick = onOcr
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// FLOATING CAPSULE PDF TOOL DOCK & PAGE STEPPER
// -------------------------------------------------------------

@Composable
private fun FloatingCapsulePdfDock(
    activeToolMode: WorkspaceToolMode,
    currentPage: Int,
    totalPages: Int,
    onSelectToolMode: (WorkspaceToolMode) -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onOpenPageManager: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            shadowElevation = 14.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
            modifier = Modifier.height(56.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // 1. Read Mode
                CapsuleDocModeItem(
                    icon = Icons.Default.Visibility,
                    label = "Read",
                    isSelected = activeToolMode == WorkspaceToolMode.VIEW_NAVIGATE,
                    onClick = { onSelectToolMode(WorkspaceToolMode.VIEW_NAVIGATE) }
                )

                // 2. Annotate / Markup Mode
                CapsuleDocModeItem(
                    icon = Icons.Default.Edit,
                    label = "Markup",
                    isSelected = activeToolMode == WorkspaceToolMode.MARKUP_DRAW,
                    onClick = { onSelectToolMode(WorkspaceToolMode.MARKUP_DRAW) }
                )

                // 3. Digital Sign & Forms
                CapsuleDocModeItem(
                    icon = Icons.Default.AssignmentTurnedIn,
                    label = "Sign",
                    isSelected = activeToolMode == WorkspaceToolMode.FORMS_FILL,
                    onClick = { onSelectToolMode(WorkspaceToolMode.FORMS_FILL) }
                )

                // 4. CAD Measurements
                CapsuleDocModeItem(
                    icon = Icons.Default.SquareFoot,
                    label = "CAD",
                    isSelected = activeToolMode == WorkspaceToolMode.CAD_MEASURE,
                    onClick = { onSelectToolMode(WorkspaceToolMode.CAD_MEASURE) }
                )

                // Divider
                Box(
                    modifier = Modifier
                        .height(24.dp)
                        .width(1.dp)
                        .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                )

                // 5. Page Stepper
                IconButton(
                    onClick = onPreviousPage,
                    enabled = currentPage > 1,
                    modifier = Modifier.size(32.dp).testTag("prev_page_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Previous Page",
                        tint = if (currentPage > 1) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CamScannerTeal.copy(alpha = 0.12f))
                        .clickable(onClick = onOpenPageManager)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .testTag("page_number_indicator"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "$currentPage / $totalPages",
                        color = CamScannerTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                IconButton(
                    onClick = onNextPage,
                    enabled = currentPage < totalPages,
                    modifier = Modifier.size(32.dp).testTag("next_page_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "Next Page",
                        tint = if (currentPage < totalPages) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CapsuleDocModeItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(if (isSelected) CamScannerTeal else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}
