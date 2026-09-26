package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.FactCheck
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    modifier: Modifier = Modifier
) {
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

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DocNavyDark)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Top Enterprise Branding Header
        item {
            HomeTopHeader(
                onOpenCreateDialog = onOpenCreateDialog,
                onOpenBuildHub = onOpenBuildHub
            )
        }

        // 2. Active Document Resume Card (If active document exists)
        if (activeDoc != null && activeTab != null) {
            item {
                ActiveDocumentResumeCard(
                    document = activeDoc,
                    activeTab = activeTab,
                    onResume = onResumeWorkspace
                )
            }
        }

        // 3. Workflow Launchpad Section Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FlashOn,
                        contentDescription = null,
                        tint = DocPrimaryCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WORKFLOW LAUNCHPAD",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Text(
                    text = "All Tools Direct Access",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        // 4. Workflow Launchpad 2-Column Grid
        item {
            WorkflowShortcutGrid(onLaunchWorkflow = onLaunchWorkflow)
        }

        // 5. Search Bar & Filter Pills
        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = Color(0xFFF59E0B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PROJECT REPOSITORY",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                    Text(
                        text = "${filteredDocs.size} Documents",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Box
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchChange,
                    placeholder = { Text("Search blueprints, contracts, specs...", fontSize = 12.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF94A3B8),
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotBlank()) {
                            IconButton(onClick = { onSearchChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = Color(0xFF94A3B8),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
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
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Filter Pills
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CategoryFilterChip(
                        label = "All",
                        isSelected = uiState.selectedCategoryFilter == "ALL",
                        onClick = { onCategoryFilterChange("ALL") }
                    )
                    CategoryFilterChip(
                        label = "Blueprints",
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
        }

        // 6. Documents List
        if (filteredDocs.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
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
                        Text("No matching documents found", color = Color(0xFFCBD5E1), fontSize = 13.sp)
                        Text("Try clearing your search or tap '+ New Project'", color = Color(0xFF64748B), fontSize = 11.sp)
                    }
                }
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

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

// -------------------------------------------------------------
// Sub-Components
// -------------------------------------------------------------

@Composable
private fun HomeTopHeader(
    onOpenCreateDialog: () -> Unit,
    onOpenBuildHub: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(DocSurfaceDark)
            .border(1.dp, DocBorderDark, RoundedCornerShape(12.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
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
                        text = "DOCUMENT OS",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF0369A1))
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "ENTERPRISE",
                            color = Color(0xFFBAE6FD),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    text = "Bluebeam & Acrobat Grade Engineering Engine",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onOpenBuildHub,
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DocSurfaceCardDark)
                    .border(1.dp, DocBorderDark, RoundedCornerShape(8.dp))
                    .testTag("home_github_ci_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Terminal,
                    contentDescription = "GitHub CI & Build Hub",
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onOpenCreateDialog,
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                modifier = Modifier.height(34.dp).testTag("home_new_project_button")
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color.White)
                Spacer(modifier = Modifier.width(4.dp))
                Text("New Project", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
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
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, DocPrimaryCyan.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
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
                    fontSize = 14.sp,
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
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
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
private fun WorkflowShortcutGrid(onLaunchWorkflow: (WorkflowShortcut) -> Unit) {
    val shortcuts = WorkflowShortcut.values()

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (i in shortcuts.indices step 2) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                WorkflowShortcutTile(
                    shortcut = shortcuts[i],
                    onClick = { onLaunchWorkflow(shortcuts[i]) },
                    modifier = Modifier.weight(1f)
                )
                if (i + 1 < shortcuts.size) {
                    WorkflowShortcutTile(
                        shortcut = shortcuts[i + 1],
                        onClick = { onLaunchWorkflow(shortcuts[i + 1]) },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }
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
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, DocBorderDark, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag(shortcut.tag),
        colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
    ) {
        Row(
            modifier = Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(shortcut.accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = shortcut.icon,
                    contentDescription = null,
                    tint = shortcut.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

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
                    fontSize = 9.sp,
                    lineHeight = 12.sp,
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
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, DocBorderDark, RoundedCornerShape(10.dp))
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
                    .background(categoryColor.copy(alpha = 0.2f)),
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
                    modifier = Modifier.size(28.dp).testTag("export_doc_${document.id}")
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
                    modifier = Modifier.size(28.dp).testTag("delete_doc_${document.id}")
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
                    modifier = Modifier.height(30.dp).testTag("open_doc_${document.id}")
                ) {
                    Text("Open", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
