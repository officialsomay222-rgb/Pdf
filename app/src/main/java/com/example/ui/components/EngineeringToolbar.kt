package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.WorkspaceToolMode
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun EngineeringToolbar(
    activeMode: WorkspaceToolMode,
    onModeChange: (WorkspaceToolMode) -> Unit,
    zoomLevel: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onZoomFit: () -> Unit,
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    showMiniMap: Boolean,
    onToggleMiniMap: () -> Unit,
    onOpenExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DocSurfaceDark)
            .border(width = 0.5.dp, color = DocBorderDark)
    ) {
        // Mode Selector Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState)
                .padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ModeButton(
                mode = WorkspaceToolMode.VIEW_NAVIGATE,
                label = "View",
                icon = Icons.Default.PanTool,
                isActive = activeMode == WorkspaceToolMode.VIEW_NAVIGATE,
                onClick = { onModeChange(WorkspaceToolMode.VIEW_NAVIGATE) }
            )
            ModeButton(
                mode = WorkspaceToolMode.MARKUP_DRAW,
                label = "Markup",
                icon = Icons.Default.Edit,
                isActive = activeMode == WorkspaceToolMode.MARKUP_DRAW,
                onClick = { onModeChange(WorkspaceToolMode.MARKUP_DRAW) }
            )
            ModeButton(
                mode = WorkspaceToolMode.CAD_MEASURE,
                label = "Measure",
                icon = Icons.Default.SquareFoot,
                isActive = activeMode == WorkspaceToolMode.CAD_MEASURE,
                onClick = { onModeChange(WorkspaceToolMode.CAD_MEASURE) }
            )
            ModeButton(
                mode = WorkspaceToolMode.FORMS_FILL,
                label = "Forms",
                icon = Icons.Default.AssignmentTurnedIn,
                isActive = activeMode == WorkspaceToolMode.FORMS_FILL,
                onClick = { onModeChange(WorkspaceToolMode.FORMS_FILL) }
            )
            ModeButton(
                mode = WorkspaceToolMode.TEXT_EDIT,
                label = "Text",
                icon = Icons.Default.TextFields,
                isActive = activeMode == WorkspaceToolMode.TEXT_EDIT,
                onClick = { onModeChange(WorkspaceToolMode.TEXT_EDIT) }
            )
            ModeButton(
                mode = WorkspaceToolMode.AI_COPILOT,
                label = "AI Copilot",
                icon = Icons.Default.AutoAwesome,
                isActive = activeMode == WorkspaceToolMode.AI_COPILOT,
                onClick = { onModeChange(WorkspaceToolMode.AI_COPILOT) },
                accentColor = Color(0xFFA855F7)
            )
            ModeButton(
                mode = WorkspaceToolMode.SECURITY_ASSEMBLY,
                label = "Security",
                icon = Icons.Default.Security,
                isActive = activeMode == WorkspaceToolMode.SECURITY_ASSEMBLY,
                onClick = { onModeChange(WorkspaceToolMode.SECURITY_ASSEMBLY) }
            )
        }

        HorizontalDivider(color = DocBorderDark, thickness = 0.5.dp)

        // Utility Bar: Undo, Redo, Zoom Controls, MiniMap Toggle, Export
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: Undo / Redo
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(32.dp).testTag("action_undo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(32.dp).testTag("action_redo_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) Color.White else Color(0xFF64748B),
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                // MiniMap Navigator Toggle
                FilterChip(
                    selected = showMiniMap,
                    onClick = onToggleMiniMap,
                    label = { Text("MiniMap", fontSize = 11.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Layers,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp)
                        )
                    },
                    modifier = Modifier.height(28.dp).testTag("toggle_minimap_chip"),
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = DocPrimaryCyan.copy(alpha = 0.3f),
                        selectedLabelColor = Color.White
                    )
                )
            }

            // Right: Zoom controls & Export
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onZoomOut,
                    modifier = Modifier.size(30.dp).testTag("zoom_out_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomOut,
                        contentDescription = "Zoom Out",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Text(
                    text = "${(zoomLevel * 100).toInt()}%",
                    color = Color(0xFF38BDF8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onZoomFit() }
                        .padding(horizontal = 4.dp)
                        .testTag("zoom_percentage_indicator")
                )

                IconButton(
                    onClick = onZoomIn,
                    modifier = Modifier.size(30.dp).testTag("zoom_in_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.ZoomIn,
                        contentDescription = "Zoom In",
                        tint = Color.White,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Quick Export Button
                IconButton(
                    onClick = onOpenExport,
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(DocPrimaryCyan)
                        .testTag("action_export_pdf_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.FileDownload,
                        contentDescription = "Export PDF",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ModeButton(
    mode: WorkspaceToolMode,
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    onClick: () -> Unit,
    accentColor: Color = DocPrimaryCyan
) {
    val bg = if (isActive) accentColor.copy(alpha = 0.2f) else Color.Transparent
    val border = if (isActive) accentColor else Color.Transparent
    val tint = if (isActive) accentColor else Color(0xFF94A3B8)

    Row(
        modifier = Modifier
            .padding(horizontal = 2.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag("tool_mode_${mode.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (isActive) Color.White else Color(0xFFCBD5E1),
            fontSize = 12.sp,
            fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
