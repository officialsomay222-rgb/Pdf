package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentCategory
import com.example.model.DocumentWorkspaceTab
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun WorkspaceTabBar(
    tabs: List<DocumentWorkspaceTab>,
    activeTabId: String,
    onTabSelected: (String) -> Unit,
    onTabClosed: (String) -> Unit,
    onOpenDocumentPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DocSurfaceDark)
            .border(width = 0.5.dp, color = DocBorderDark)
            .padding(horizontal = 6.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Document Library / Open button
        IconButton(
            onClick = onOpenDocumentPicker,
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(DocSurfaceCardDark)
                .testTag("open_document_library_button")
        ) {
            Icon(
                imageVector = Icons.Default.FolderOpen,
                contentDescription = "Open Document Library",
                tint = DocPrimaryCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(6.dp))

        // Tabs List
        Row(
            modifier = Modifier
                .weight(1f)
                .horizontalScroll(scrollState),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabs.forEach { tab ->
                val isActive = tab.id == activeTabId

                val tabBg = if (isActive) DocSurfaceCardDark else Color.Transparent
                val tabBorder = if (isActive) DocPrimaryCyan else DocBorderDark.copy(alpha = 0.5f)
                val textColor = if (isActive) Color.White else Color(0xFF94A3B8)

                Row(
                    modifier = Modifier
                        .padding(end = 4.dp)
                        .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                        .background(tabBg)
                        .border(
                            width = if (isActive) 1.dp else 0.5.dp,
                            color = tabBorder,
                            shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp, bottomStart = 4.dp, bottomEnd = 4.dp)
                        )
                        .clickable { onTabSelected(tab.id) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                        .testTag("workspace_tab_${tab.id}"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val categoryColor = when (tab.category) {
                        DocumentCategory.BLUEPRINT -> Color(0xFF38BDF8)
                        DocumentCategory.CONTRACT -> Color(0xFFF59E0B)
                        DocumentCategory.INSPECTION_FORM -> Color(0xFF10B981)
                        else -> Color(0xFFA855F7)
                    }

                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(categoryColor)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    Text(
                        text = tab.title,
                        color = textColor,
                        fontSize = 12.sp,
                        fontWeight = if (isActive) FontWeight.SemiBold else FontWeight.Normal,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 140.dp)
                    )

                    if (tab.isModified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "•",
                            color = DocPrimaryCyan,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    if (tabs.size > 1) {
                        Spacer(modifier = Modifier.width(6.dp))
                        IconButton(
                            onClick = { onTabClosed(tab.id) },
                            modifier = Modifier
                                .size(16.dp)
                                .testTag("close_tab_${tab.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close tab",
                                tint = Color(0xFF64748B),
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
