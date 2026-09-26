package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun PageManagerDialog(
    pageCount: Int,
    activePageIndex: Int,
    onSelectPage: (Int) -> Unit,
    onRotatePage: (Int) -> Unit,
    onDuplicatePage: (Int) -> Unit,
    onDeletePage: (Int) -> Unit,
    onInsertBlankPage: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DocSurfaceDark,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoStories,
                        contentDescription = null,
                        tint = DocPrimaryCyan,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Visual Page Assembly",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Button(
                    onClick = onInsertBlankPage,
                    colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp).testTag("insert_blank_page_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("Insert Page", fontSize = 11.sp, color = Color.White)
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 380.dp)) {
                Text(
                    text = "Manage, reorder, rotate, or duplicate document sheets:",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(pageCount) { index ->
                        val isCurrent = index == activePageIndex

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (isCurrent) 2.dp else 1.dp,
                                    color = if (isCurrent) DocPrimaryCyan else DocBorderDark,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { onSelectPage(index) }
                                .testTag("page_card_$index"),
                            colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
                        ) {
                            Column(
                                modifier = Modifier.padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Miniature Sheet Graphic
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(70.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF0F172A))
                                        .border(0.5.dp, Color(0xFF334155), RoundedCornerShape(4.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Description,
                                            contentDescription = null,
                                            tint = if (isCurrent) DocPrimaryCyan else Color(0xFF64748B),
                                            modifier = Modifier.size(24.dp)
                                        )
                                        Text(
                                            text = "Sheet #${index + 1}",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                // Quick Actions: Rotate, Duplicate, Delete
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(
                                        onClick = { onRotatePage(index) },
                                        modifier = Modifier.size(24.dp).testTag("rotate_page_$index")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.RotateRight,
                                            contentDescription = "Rotate 90°",
                                            tint = Color(0xFF38BDF8),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDuplicatePage(index) },
                                        modifier = Modifier.size(24.dp).testTag("duplicate_page_$index")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Duplicate",
                                            tint = Color(0xFFF59E0B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }

                                    if (pageCount > 1) {
                                        IconButton(
                                            onClick = { onDeletePage(index) },
                                            modifier = Modifier.size(24.dp).testTag("delete_page_$index")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Delete,
                                                contentDescription = "Delete",
                                                tint = Color(0xFFEF4444),
                                                modifier = Modifier.size(15.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan)
            ) {
                Text("Done", color = Color.White)
            }
        }
    )
}
