package com.example

import android.Manifest
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
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import com.example.ui.canvas.DocumentCanvas
import com.example.ui.components.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {
    private val viewModel: DocumentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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

                // BackHandler returns to Home screen if currently in workspace
                BackHandler(enabled = uiState.currentScreen == AppScreen.WORKSPACE) {
                    viewModel.navigateToHome()
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = DocNavyDark,
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    when (uiState.currentScreen) {
                        AppScreen.HOME -> {
                            HomeScreen(
                                uiState = uiState,
                                onOpenDocument = { viewModel.openDocument(it) },
                                onLaunchWorkflow = { viewModel.launchWorkflow(it) },
                                onResumeWorkspace = { viewModel.navigateToWorkspace() },
                                onSearchChange = { viewModel.setSearchQuery(it) },
                                onCategoryFilterChange = { viewModel.setCategoryFilter(it) },
                                onOpenCreateDialog = { viewModel.setCreateProjectDialogOpen(true) },
                                onOpenBuildHub = { viewModel.triggerInAppBuild(context) },
                                onExportDocument = { doc ->
                                    viewModel.openDocument(doc)
                                    viewModel.exportDocument(context)
                                },
                                onDeleteDocument = { viewModel.deleteDocument(it) },
                                onTriggerBuild = { viewModel.triggerInAppBuild(context) },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                        AppScreen.WORKSPACE -> {
                            DocumentOsScreen(
                                uiState = uiState,
                                viewModel = viewModel,
                                onNavigateHome = { viewModel.navigateToHome() },
                                modifier = Modifier.padding(innerPadding)
                            )
                        }
                    }

                    // Dialogs accessible from both Home and Workspace
                    if (uiState.isCreateProjectDialogOpen) {
                        CreateProjectDialog(
                            onDismiss = { viewModel.setCreateProjectDialogOpen(false) },
                            onCreateProject = { title, category, sheetCount ->
                                viewModel.createAndOpenNewProject(title, category, sheetCount)
                            }
                        )
                    }

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

    val activeDocId = activeDoc?.id ?: ""
    val currentAnnotations = uiState.annotations[activeDocId] ?: emptyList()
    val currentMeasurements = uiState.measurements[activeDocId] ?: emptyList()
    val currentFields = uiState.formFields[activeDocId] ?: emptyList()

    Box(modifier = modifier.fillMaxSize().background(DocNavyDark)) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Enterprise Control Bar with Home Navigation
            TopEnterpriseBar(
                title = activeDoc?.title ?: "Document OS",
                category = activeTab?.category ?: DocumentCategory.BLUEPRINT,
                currentPage = (activeTab?.activePageIndex ?: 0) + 1,
                totalPages = activeTab?.pageCount ?: 1,
                onNavigateHome = onNavigateHome,
                onPreviousPage = {
                    val curr = activeTab?.activePageIndex ?: 0
                    if (curr > 0) viewModel.setActivePage(curr - 1)
                },
                onNextPage = {
                    val curr = activeTab?.activePageIndex ?: 0
                    val total = activeTab?.pageCount ?: 1
                    if (curr < total - 1) viewModel.setActivePage(curr + 1)
                },
                onOpenPageManager = { viewModel.setPageManagerOpen(true) },
                onOpenSecurity = { viewModel.setSecurityDialogOpen(true) },
                onOpenRepo = { viewModel.setDocLibraryOpen(true) }
            )

            // 2. Multi-Tab Workspace Dock
            WorkspaceTabBar(
                tabs = uiState.tabs,
                activeTabId = uiState.activeTabId,
                onTabSelected = { viewModel.selectTab(it) },
                onTabClosed = { viewModel.closeTab(it) },
                onOpenDocumentPicker = { viewModel.setDocLibraryOpen(true) }
            )

            // 3. Primary Engineering Toolbar
            EngineeringToolbar(
                activeMode = uiState.toolMode,
                onModeChange = { viewModel.setToolMode(it) },
                zoomLevel = activeTab?.zoomLevel ?: 1.0f,
                onZoomIn = { viewModel.zoomIn() },
                onZoomOut = { viewModel.zoomOut() },
                onZoomFit = { viewModel.zoomFit() },
                canUndo = viewModel.canUndo(),
                canRedo = viewModel.canRedo(),
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                showMiniMap = uiState.showMiniMap,
                onToggleMiniMap = { viewModel.toggleMiniMap() },
                onOpenExport = { viewModel.exportDocument(context) }
            )

            // 4. Contextual Tool Drawers
            AnimatedVisibility(visible = uiState.toolMode == WorkspaceToolMode.MARKUP_DRAW) {
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

            AnimatedVisibility(visible = uiState.toolMode == WorkspaceToolMode.CAD_MEASURE) {
                MeasurementToolbar(
                    activeScale = uiState.activeScale,
                    selectedMeasurementType = uiState.measurementType,
                    onMeasurementTypeSelect = { viewModel.setMeasurementType(it) },
                    onOpenCalibration = { viewModel.setScaleDialogOpen(true) },
                    onOpenTakeoffSummary = { viewModel.setTakeoffDialogOpen(true) },
                    measurementsCount = currentMeasurements.size
                )
            }

            // 5. The Document Canvas Area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                DocumentCanvas(
                    category = activeTab?.category ?: DocumentCategory.BLUEPRINT,
                    pageIndex = activeTab?.activePageIndex ?: 0,
                    pageCount = activeTab?.pageCount ?: 1,
                    documentTitle = activeDoc?.title ?: "Document OS",
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
                    onFormFieldClick = { viewModel.onFormFieldClick(it) }
                )

                // 6. CAD Mini-Map Navigator (Floating overlay in bottom-right corner)
                if (uiState.showMiniMap) {
                    CadMiniMap(
                        category = activeTab?.category ?: DocumentCategory.BLUEPRINT,
                        zoomLevel = activeTab?.zoomLevel ?: 1.0f,
                        panOffsetX = activeTab?.panOffsetX ?: 0f,
                        panOffsetY = activeTab?.panOffsetY ?: 0f,
                        canvasViewportWidth = 600f,
                        canvasViewportHeight = 800f,
                        docWidth = 720f,
                        docHeight = 980f,
                        onNavigate = { newPanX, newPanY ->
                            viewModel.setPanOffset(newPanX, newPanY)
                        },
                        onClose = { viewModel.toggleMiniMap() },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(12.dp)
                    )
                }
            }
        }

        // 7. Bottom AI Intelligence Copilot Sheet
        AnimatedVisibility(
            visible = uiState.toolMode == WorkspaceToolMode.AI_COPILOT,
            enter = slideInVertically(initialOffsetY = { it }),
            exit = slideOutVertically(targetOffsetY = { it }),
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            AiCopilotSheet(
                chatMessages = uiState.chatMessages,
                isAiLoading = uiState.isAiLoading,
                onSendMessage = { viewModel.sendAiMessage(it) },
                piiSuggestions = uiState.piiSuggestions,
                onApplyAutoRedaction = { viewModel.applyAutoRedaction() },
                onClose = { viewModel.setToolMode(WorkspaceToolMode.VIEW_NAVIGATE) }
            )
        }

        // 8. Dialogs
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

@Composable
private fun TopEnterpriseBar(
    title: String,
    category: DocumentCategory,
    currentPage: Int,
    totalPages: Int,
    onNavigateHome: () -> Unit,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onOpenPageManager: () -> Unit,
    onOpenSecurity: () -> Unit,
    onOpenRepo: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(DocSurfaceDark)
            .border(width = 0.5.dp, color = DocBorderDark)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Home button & Title
        Row(
            modifier = Modifier.weight(1f, fill = false),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onNavigateHome,
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DocSurfaceCardDark)
                    .testTag("nav_home_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Back to Home",
                    tint = DocPrimaryCyan,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 160.dp)
                )
                Text(
                    text = category.name.replace("_", " "),
                    color = DocPrimaryCyanLight,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Center: Sheet Pager Controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(DocSurfaceCardDark)
                .border(0.5.dp, DocBorderDark, RoundedCornerShape(6.dp))
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            IconButton(
                onClick = onPreviousPage,
                enabled = currentPage > 1,
                modifier = Modifier.size(24.dp).testTag("prev_page_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Previous Page",
                    tint = if (currentPage > 1) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
            }

            Text(
                text = "$currentPage / $totalPages",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(onClick = onOpenPageManager)
                    .padding(horizontal = 6.dp)
                    .testTag("page_number_indicator")
            )

            IconButton(
                onClick = onNextPage,
                enabled = currentPage < totalPages,
                modifier = Modifier.size(24.dp).testTag("next_page_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Next Page",
                    tint = if (currentPage < totalPages) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Right Actions: Page Manager & Security
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenPageManager,
                modifier = Modifier.size(30.dp).testTag("open_page_assembly_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = "Visual Page Assembly",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }

            IconButton(
                onClick = onOpenSecurity,
                modifier = Modifier.size(30.dp).testTag("open_security_dialog_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = "Security Policies",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
