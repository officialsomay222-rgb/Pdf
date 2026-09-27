package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentEntity
import com.example.ui.theme.CamScannerTeal

/**
 * High-performance Shutter Bottom Action Sheet.
 * Opens smoothly with full options and zero stutter or clipping on any device.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentActionShutterSheet(
    document: DocumentEntity,
    onDismiss: () -> Unit,
    onOpenReader: () -> Unit,
    onOpenStudio: () -> Unit,
    onShare: () -> Unit,
    onCompress: () -> Unit,
    onRename: () -> Unit,
    onOcr: () -> Unit,
    onMoveFolder: () -> Unit,
    onSplit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 8.dp,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
            )
        },
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 4.dp)
        ) {
            // Document Header Banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CamScannerTeal.copy(alpha = 0.16f)),
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
                        tint = CamScannerTeal,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(CamScannerTeal)
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "${document.pageCount} PAGES",
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${document.fileSizeFormatted} • ${document.category.replace("_", " ")}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Items in sleek, scrollable column
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 380.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                ShutterActionRow(
                    icon = Icons.Default.MenuBook,
                    iconTint = CamScannerTeal,
                    title = "Quick View (Normal Reader)",
                    subtitle = "Fast reading, extra fast scrollbar & night modes",
                    testTag = "shutter_open_reader",
                    onClick = onOpenReader
                )
                ShutterActionRow(
                    icon = Icons.Default.Handyman,
                    iconTint = Color(0xFFF59E0B),
                    title = "Studio Editor (All-in-One)",
                    subtitle = "Markup, CAD takeoff, forms & security",
                    testTag = "shutter_open_studio",
                    onClick = onOpenStudio
                )
                ShutterActionRow(
                    icon = Icons.Default.Share,
                    iconTint = Color(0xFF3B82F6),
                    title = "Share & Export PDF",
                    subtitle = "Compile and send via WhatsApp, Drive, Email",
                    testTag = "shutter_share_doc",
                    onClick = onShare
                )
                ShutterActionRow(
                    icon = Icons.Default.Compress,
                    iconTint = Color(0xFF8B5CF6),
                    title = "Compress PDF",
                    subtitle = "Optimize file size (up to 70% reduction)",
                    testTag = "shutter_compress_doc",
                    onClick = onCompress
                )
                ShutterActionRow(
                    icon = Icons.Default.TextFields,
                    iconTint = Color(0xFF10B981),
                    title = "Extract Text (OCR)",
                    subtitle = "Convert scanned pages into copyable text",
                    testTag = "shutter_ocr_doc",
                    onClick = onOcr
                )
                ShutterActionRow(
                    icon = Icons.Default.DriveFileRenameOutline,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    title = "Rename Document",
                    subtitle = "Change document title",
                    testTag = "shutter_rename_doc",
                    onClick = onRename
                )
                ShutterActionRow(
                    icon = Icons.Default.FolderOpen,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    title = "Move to Folder",
                    subtitle = "Organize in Work, Receipts, or Personal",
                    testTag = "shutter_move_doc",
                    onClick = onMoveFolder
                )
                ShutterActionRow(
                    icon = Icons.Default.CallSplit,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    title = "Split Document",
                    subtitle = "Extract individual pages or range",
                    testTag = "shutter_split_doc",
                    onClick = onSplit
                )
                ShutterActionRow(
                    icon = Icons.Default.ControlPointDuplicate,
                    iconTint = MaterialTheme.colorScheme.onSurface,
                    title = "Duplicate Document",
                    subtitle = "Make an identical copy in your workspace",
                    testTag = "shutter_duplicate_doc",
                    onClick = onDuplicate
                )

                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )

                ShutterActionRow(
                    icon = Icons.Default.Delete,
                    iconTint = Color(0xFFEF4444),
                    title = "Delete Document",
                    subtitle = "Remove document permanently",
                    isDestructive = true,
                    testTag = "shutter_delete_doc",
                    onClick = onDelete
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ShutterActionRow(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isDestructive: Boolean = false,
    testTag: String = "",
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 52.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = if (isDestructive) 0.12f else 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDestructive) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}
