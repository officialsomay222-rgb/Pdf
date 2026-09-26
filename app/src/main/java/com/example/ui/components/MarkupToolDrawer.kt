package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MarkupType
import com.example.model.StampType
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark

@Composable
fun MarkupToolDrawer(
    selectedMarkup: MarkupType,
    onMarkupSelect: (MarkupType) -> Unit,
    activeColorHex: Long,
    onColorSelect: (Long) -> Unit,
    strokeWidth: Float,
    onStrokeWidthChange: (Float) -> Unit,
    selectedStamp: StampType,
    onStampSelect: (StampType) -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    var showStampMenu by remember { mutableStateOf(false) }

    val presetColors = listOf(
        0xFF0284C7, // Blue
        0xFFEF4444, // Red (standard engineering revisions)
        0xFFF59E0B, // Amber / Yellow Highlighter
        0xFF10B981, // Emerald Green
        0xFF8B5CF6, // Purple
        0xFF000000  // Black (Redaction)
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DocSurfaceCardDark)
            .border(width = 0.5.dp, color = DocBorderDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        // Row 1: Markup Tools
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            MarkupButton(
                type = MarkupType.PEN,
                label = "Pen",
                icon = Icons.Default.Gesture,
                isSelected = selectedMarkup == MarkupType.PEN,
                onClick = { onMarkupSelect(MarkupType.PEN) }
            )
            MarkupButton(
                type = MarkupType.HIGHLIGHTER,
                label = "Highlighter",
                icon = Icons.Default.BorderColor,
                isSelected = selectedMarkup == MarkupType.HIGHLIGHTER,
                onClick = { onMarkupSelect(MarkupType.HIGHLIGHTER) }
            )
            MarkupButton(
                type = MarkupType.REVISION_CLOUD,
                label = "Rev Cloud",
                icon = Icons.Default.CloudQueue,
                isSelected = selectedMarkup == MarkupType.REVISION_CLOUD,
                onClick = { onMarkupSelect(MarkupType.REVISION_CLOUD) }
            )
            MarkupButton(
                type = MarkupType.CALLOUT,
                label = "Callout",
                icon = Icons.Default.ChatBubbleOutline,
                isSelected = selectedMarkup == MarkupType.CALLOUT,
                onClick = { onMarkupSelect(MarkupType.CALLOUT) }
            )
            MarkupButton(
                type = MarkupType.RECTANGLE,
                label = "Rectangle",
                icon = Icons.Default.CropSquare,
                isSelected = selectedMarkup == MarkupType.RECTANGLE,
                onClick = { onMarkupSelect(MarkupType.RECTANGLE) }
            )
            MarkupButton(
                type = MarkupType.ARROW,
                label = "Arrow",
                icon = Icons.Default.TrendingFlat,
                isSelected = selectedMarkup == MarkupType.ARROW,
                onClick = { onMarkupSelect(MarkupType.ARROW) }
            )
            MarkupButton(
                type = MarkupType.STAMP,
                label = "Stamp (${selectedStamp.label.take(6)})",
                icon = Icons.Default.Approval,
                isSelected = selectedMarkup == MarkupType.STAMP,
                onClick = {
                    onMarkupSelect(MarkupType.STAMP)
                    showStampMenu = true
                }
            )
            MarkupButton(
                type = MarkupType.REDACTION_BOX,
                label = "Redact",
                icon = Icons.Default.VisibilityOff,
                isSelected = selectedMarkup == MarkupType.REDACTION_BOX,
                onClick = { onMarkupSelect(MarkupType.REDACTION_BOX) }
            )
            MarkupButton(
                type = MarkupType.MAGIC_ERASER,
                label = "Eraser",
                icon = Icons.Default.AutoFixNormal,
                isSelected = selectedMarkup == MarkupType.MAGIC_ERASER,
                onClick = { onMarkupSelect(MarkupType.MAGIC_ERASER) }
            )
            MarkupButton(
                type = MarkupType.LASER_POINTER,
                label = "Laser",
                icon = Icons.Default.Highlight,
                isSelected = selectedMarkup == MarkupType.LASER_POINTER,
                onClick = { onMarkupSelect(MarkupType.LASER_POINTER) }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Row 2: Color Palette & Stroke Thickness Slider
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Colors
            Row(verticalAlignment = Alignment.CenterVertically) {
                presetColors.forEach { hex ->
                    val isSelected = activeColorHex == hex
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(Color(hex))
                            .border(
                                width = if (isSelected) 2.5.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Gray.copy(alpha = 0.5f),
                                shape = CircleShape
                            )
                            .clickable { onColorSelect(hex) }
                            .padding(2.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
            }

            // Stroke Width Selector
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = "Width: ${strokeWidth.toInt()}pt",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                listOf(2f, 4f, 8f).forEach { width ->
                    val isCurrent = strokeWidth == width
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isCurrent) DocPrimaryCyan else Color(0xFF334155))
                            .clickable { onStrokeWidthChange(width) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${width.toInt()}",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(3.dp))
                }
            }
        }
    }

    // Stamp Selection Dropdown
    if (showStampMenu) {
        AlertDialog(
            onDismissRequest = { showStampMenu = false },
            title = { Text("Select Architectural Stamp", color = Color.White) },
            containerColor = Color(0xFF0F172A),
            text = {
                Column {
                    StampType.values().forEach { stamp ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onStampSelect(stamp)
                                    showStampMenu = false
                                }
                                .padding(vertical = 8.dp, horizontal = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .clip(CircleShape)
                                    .background(Color(stamp.colorHex))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = stamp.label,
                                color = Color(stamp.colorHex),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStampMenu = false }) {
                    Text("Cancel", color = DocPrimaryCyan)
                }
            }
        )
    }
}

@Composable
private fun MarkupButton(
    type: MarkupType,
    label: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) DocPrimaryCyan.copy(alpha = 0.25f) else Color.Transparent
    val border = if (isSelected) DocPrimaryCyan else DocBorderDark

    Row(
        modifier = Modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("markup_tool_${type.name.lowercase()}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) DocPrimaryCyan else Color(0xFF94A3B8),
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}
