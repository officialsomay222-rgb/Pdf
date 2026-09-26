package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentCategory
import com.example.model.DocumentEntity
import com.example.ui.components.BuildTaskStep
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
    CAD_MEASURE(
        title = "CAD Measure & Takeoff",
        description = "Calibrated scale, distance rulers & area sq ft",
        icon = Icons.Default.SquareFoot,
        accentColor = Color(0xFFF59E0B),
        tag = "shortcut_cad_measure"
    ),
    MARKUP_ANNOTATE(
        title = "Markup & Rev Clouds",
        description = "Revision clouds, callout arrows & stamps",
        icon = Icons.Default.Edit,
        accentColor = Color(0xFF0284C7),
        tag = "shortcut_markup"
    ),
    FORMS_AND_SIGN(
        title = "Forms & Digital Sign",
        description = "Interactive fields & PAdES crypto certificate",
        icon = Icons.Default.AssignmentTurnedIn,
        accentColor = Color(0xFF10B981),
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
        accentColor = Color(0xFF38BDF8),
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
        accentColor = Color(0xFF06B6D4),
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
        title = "GitHub CI & In-App Build",
        description = "Compile APK in-app, notifications & GitHub Actions",
        icon = Icons.Default.Terminal,
        accentColor = Color(0xFF6366F1),
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
    onTriggerBuild: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var selectedNavTab by remember { mutableIntStateOf(0) } // 0: Dashboard, 1: Documents, 2: GitHub & Build CI

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
                            contentDescription = "Dashboard"
                        )
                    },
                    label = { Text("Dashboard", fontSize = 11.sp, fontWeight = if (selectedNavTab == 0) FontWeight.Bold else FontWeight.Normal) },
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
                            imageVector = if (selectedNavTab == 1) Icons.Default.Folder else Icons.Outlined.Folder,
                            contentDescription = "Documents"
                        )
                    },
                    label = { Text("Documents", fontSize = 11.sp, fontWeight = if (selectedNavTab == 1) FontWeight.Bold else FontWeight.Normal) },
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
                        BadgedBox(
                            badge = {
                                if (uiState.isBuildingApp) {
                                    Badge(containerColor = DocAccentAmber) {
                                        Text("${uiState.buildProgress}%")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = if (selectedNavTab == 2) Icons.Default.Terminal else Icons.Outlined.Terminal,
                                contentDescription = "GitHub CI & Build"
                            )
                        }
                    },
                    label = { Text("Build & CI", fontSize = 11.sp, fontWeight = if (selectedNavTab == 2) FontWeight.Bold else FontWeight.Normal) },
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
                    // TAB 0: Clean, Professional Material 3 Dashboard
                    DashboardTabContent(
                        uiState = uiState,
                        activeDoc = activeDoc,
                        activeTab = activeTab,
                        recentDocs = uiState.documents.take(3),
                        onOpenDocument = onOpenDocument,
                        onLaunchWorkflow = { shortcut ->
                            if (shortcut == WorkflowShortcut.GITHUB_BUILD_CI) {
                                selectedNavTab = 2
                            } else {
                                onLaunchWorkflow(shortcut)
                            }
                        },
                        onResumeWorkspace = onResumeWorkspace,
                        onOpenCreateDialog = onOpenCreateDialog,
                        onOpenBuildHub = { selectedNavTab = 2 },
                        onViewAllDocs = { selectedNavTab = 1 },
                        onExportDocument = onExportDocument,
                        onDeleteDocument = onDeleteDocument
                    )
                }
                1 -> {
                    // TAB 1: Dedicated Document Repository
                    DocumentsTabContent(
                        uiState = uiState,
                        filteredDocs = filteredDocs,
                        onSearchChange = onSearchChange,
                        onCategoryFilterChange = onCategoryFilterChange,
                        onOpenDocument = onOpenDocument,
                        onExportDocument = onExportDocument,
                        onDeleteDocument = onDeleteDocument,
                        onOpenCreateDialog = onOpenCreateDialog
                    )
                }
                2 -> {
                    // TAB 2: In-App Compilation & GitHub CI Hub
                    BuildAndCiTabContent(
                        uiState = uiState,
                        onTriggerBuild = onOpenBuildHub
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// TAB 0: DASHBOARD CONTENT
// -------------------------------------------------------------

@Composable
private fun DashboardTabContent(
    uiState: DocumentUiState,
    activeDoc: DocumentEntity?,
    activeTab: com.example.model.DocumentWorkspaceTab?,
    recentDocs: List<DocumentEntity>,
    onOpenDocument: (DocumentEntity) -> Unit,
    onLaunchWorkflow: (WorkflowShortcut) -> Unit,
    onResumeWorkspace: () -> Unit,
    onOpenCreateDialog: () -> Unit,
    onOpenBuildHub: () -> Unit,
    onViewAllDocs: () -> Unit,
    onExportDocument: (DocumentEntity) -> Unit,
    onDeleteDocument: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Sleek Material 3 Top Header
        item {
            DashboardHeader(
                onOpenCreateDialog = onOpenCreateDialog,
                onOpenBuildHub = onOpenBuildHub,
                isBuilding = uiState.isBuildingApp
            )
        }

        // 2. Active Operation Alert Banner (shows if compilation or PDF generation is running)
        if (uiState.isBuildingApp) {
            item {
                ActiveCompilationBanner(
                    progress = uiState.buildProgress,
                    statusText = uiState.buildStatusText,
                    onViewDetails = onOpenBuildHub
                )
            }
        }

        // 3. Featured / Active Document Resume Card
        if (activeDoc != null && activeTab != null) {
            item {
                ActiveDocumentResumeCard(
                    document = activeDoc,
                    activeTab = activeTab,
                    onResume = onResumeWorkspace
                )
            }
        }

        // 4. Primary Workflow Quick Actions (4 key cards for simplicity)
        item {
            Text(
                text = "ENGINEERING TOOLS",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        item {
            PrimaryQuickActionGrid(onLaunchWorkflow = onLaunchWorkflow)
        }

        // 5. Secondary Quick Shortcuts Row
        item {
            SecondaryToolsRow(
                onLaunchWorkflow = onLaunchWorkflow,
                onOpenCreateDialog = onOpenCreateDialog,
                onOpenBuildHub = onOpenBuildHub
            )
        }

        // 6. Recent Documents Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "RECENT PROJECTS",
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
// TAB 1: DOCUMENTS CONTENT
// -------------------------------------------------------------

@Composable
private fun DocumentsTabContent(
    uiState: DocumentUiState,
    filteredDocs: List<DocumentEntity>,
    onSearchChange: (String) -> Unit,
    onCategoryFilterChange: (String) -> Unit,
    onOpenDocument: (DocumentEntity) -> Unit,
    onExportDocument: (DocumentEntity) -> Unit,
    onDeleteDocument: (String) -> Unit,
    onOpenCreateDialog: () -> Unit
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
                        text = "${filteredDocs.size} blueprints & contracts loaded",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Button(
                    onClick = onOpenCreateDialog,
                    colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("home_new_project_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("New Project", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Search Bar
        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Search blueprints, specs, contracts...", fontSize = 13.sp, color = Color(0xFF64748B)) },
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
                    label = "All Documents",
                    isSelected = uiState.selectedCategoryFilter == "ALL",
                    onClick = { onCategoryFilterChange("ALL") }
                )
                CategoryFilterChip(
                    label = "CAD Blueprints",
                    isSelected = uiState.selectedCategoryFilter == DocumentCategory.BLUEPRINT.name,
                    onClick = { onCategoryFilterChange(DocumentCategory.BLUEPRINT.name) }
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
// TAB 2: IN-APP BUILD & GITHUB CI CONTENT
// -------------------------------------------------------------

@Composable
private fun BuildAndCiTabContent(
    uiState: DocumentUiState,
    onTriggerBuild: () -> Unit
) {
    val context = LocalContext.current
    var showYamlDialog by remember { mutableStateOf(false) }

    val workflowYaml = remember {
        """
name: Android Build & CI
on:
  push:
    branches: [ "main", "master" ]
  workflow_dispatch:
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build Debug APK
        run: ./gradlew assembleDebug --no-daemon
      - uses: actions/upload-artifact@v4
        with:
          name: DocumentOS-Debug-APK
          path: app/build/outputs/apk/debug/*.apk
        """.trimIndent()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Build & CI Automation",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "In-App Compilation • Real Notifications • GitHub Actions",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (uiState.isBuildingApp) Color(0xFFF59E0B).copy(alpha = 0.2f) else Color(0xFF10B981).copy(alpha = 0.2f))
                        .border(1.dp, if (uiState.isBuildingApp) Color(0xFFF59E0B) else Color(0xFF10B981), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (uiState.isBuildingApp) "COMPILING" else "READY",
                        color = if (uiState.isBuildingApp) Color(0xFFF59E0B) else Color(0xFF10B981),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Primary In-App Compiler Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DocSurfaceDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DocBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(DocPrimaryCyan.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Build, contentDescription = null, tint = DocPrimaryCyan, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("In-App APK Compiler", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Assembles APK with Live System Notification", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }

                        Button(
                            onClick = onTriggerBuild,
                            enabled = !uiState.isBuildingApp,
                            colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("trigger_in_app_build_button")
                        ) {
                            Icon(
                                imageVector = if (uiState.isBuildingApp) Icons.Default.HourglassTop else Icons.Default.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(if (uiState.isBuildingApp) "Building..." else "Compile App", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Progress Bar
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(uiState.buildStatusText, color = Color(0xFFE2E8F0), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                            Text("${uiState.buildProgress}%", color = DocPrimaryCyanLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        LinearProgressIndicator(
                            progress = { uiState.buildProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = DocPrimaryCyanLight,
                            trackColor = Color(0xFF334155)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Step breakdown
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.buildSteps.forEach { step ->
                            BuildStepRow(step = step)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Notification banner tip
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0F172A))
                            .border(0.5.dp, DocBorderDark, RoundedCornerShape(8.dp))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.NotificationsActive,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Background Task: Pull down your Android notification shade to monitor ongoing compile progress & completion alerts.",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                }
            }
        }

        // GitHub Actions CI Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DocBorderDark)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                                    .background(Color(0xFF6366F1).copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF818CF8), modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("GitHub Actions CI Pipeline", color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("android-build.yml • ubuntu-latest • JDK 17", color = Color(0xFF94A3B8), fontSize = 11.sp)
                            }
                        }

                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("GitHub Workflow", workflowYaml)
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "Copied GitHub Actions YAML to clipboard", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(DocSurfaceDark)
                                .testTag("copy_workflow_yaml_button")
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy YAML", tint = Color(0xFF38BDF8), modifier = Modifier.size(16.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Configured for automatic testing and release APK generation on push to 'main' branch and manual trigger.",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Code snippet preview
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF0A0F1D))
                            .border(1.dp, DocBorderDark, RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = "run: ./gradlew assembleDebug --no-daemon\npath: app/build/outputs/apk/debug/*.apk",
                            color = Color(0xFF10B981),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// -------------------------------------------------------------
// SUB-COMPONENTS
// -------------------------------------------------------------

@Composable
private fun DashboardHeader(
    onOpenCreateDialog: () -> Unit,
    onOpenBuildHub: () -> Unit,
    isBuilding: Boolean
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
                            text = "PRO",
                            color = Color(0xFFBAE6FD),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "CAD Measurements • Markups • Gemini AI",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenBuildHub,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DocSurfaceCardDark)
                    .border(1.dp, DocBorderDark, RoundedCornerShape(8.dp))
                    .testTag("home_github_ci_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "GitHub CI & Build Hub",
                    tint = if (isBuilding) DocAccentAmber else Color(0xFF38BDF8),
                    modifier = Modifier.size(20.dp)
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
private fun ActiveCompilationBanner(
    progress: Int,
    statusText: String,
    onViewDetails: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFF59E0B).copy(alpha = 0.8f), RoundedCornerShape(12.dp))
            .clickable(onClick = onViewDetails),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF291B07))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier.size(28.dp),
                color = Color(0xFFF59E0B),
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Compiling in Background • $progress%",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = statusText,
                    color = Color(0xFFFDE68A),
                    fontSize = 10.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            TextButton(onClick = onViewDetails) {
                Text("Details", color = Color(0xFFF59E0B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
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
                        text = "ACTIVE WORKSPACE",
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
private fun PrimaryQuickActionGrid(onLaunchWorkflow: (WorkflowShortcut) -> Unit) {
    val topShortcuts = listOf(
        WorkflowShortcut.CAD_MEASURE,
        WorkflowShortcut.MARKUP_ANNOTATE,
        WorkflowShortcut.FORMS_AND_SIGN,
        WorkflowShortcut.AI_COPILOT
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in topShortcuts.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkflowShortcutTile(
                    shortcut = topShortcuts[i],
                    onClick = { onLaunchWorkflow(topShortcuts[i]) },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < topShortcuts.size) {
                    WorkflowShortcutTile(
                        shortcut = topShortcuts[i + 1],
                        onClick = { onLaunchWorkflow(topShortcuts[i + 1]) },
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
    onOpenCreateDialog: () -> Unit,
    onOpenBuildHub: () -> Unit
) {
    val secondaryTools = listOf(
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
private fun DocumentRepositoryCard(
    document: DocumentEntity,
    onOpen: () -> Unit,
    onExport: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryColor = when (document.category) {
        DocumentCategory.BLUEPRINT.name -> Color(0xFF38BDF8)
        DocumentCategory.CONTRACT.name -> Color(0xFFF59E0B)
        DocumentCategory.INSPECTION_FORM.name -> Color(0xFF10B981)
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

            // Quick Actions
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onExport,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("export_doc_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share PDF",
                        tint = Color(0xFF38BDF8),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(30.dp)
                        .testTag("delete_doc_${document.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete",
                        tint = Color(0xFFEF4444),
                        modifier = Modifier.size(16.dp)
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
private fun BuildStepRow(step: BuildTaskStep) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(
                    when (step.status) {
                        "success" -> Color(0xFF10B981)
                        "running" -> Color(0xFFF59E0B)
                        "failed" -> Color(0xFFEF4444)
                        else -> Color(0xFF475569)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when (step.status) {
                "success" -> Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                "running" -> CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp, color = Color.White)
                "failed" -> Icon(Icons.Default.Close, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
                else -> Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(Color(0xFF94A3B8)))
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = step.name,
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = step.description,
                color = Color(0xFF94A3B8),
                fontSize = 10.sp
            )
        }

        Text(
            text = step.status.uppercase(),
            color = when (step.status) {
                "success" -> Color(0xFF10B981)
                "running" -> Color(0xFFF59E0B)
                "failed" -> Color(0xFFEF4444)
                else -> Color(0xFF64748B)
            },
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold
        )
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
