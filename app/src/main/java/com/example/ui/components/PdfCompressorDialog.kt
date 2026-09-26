package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.model.DocumentEntity
import com.example.ui.theme.*

@Composable
fun PdfCompressorDialog(
    document: DocumentEntity,
    onDismiss: () -> Unit,
    onCompress: (document: DocumentEntity, quality: String) -> Unit
) {
    var selectedQuality by remember { mutableStateOf("MEDIUM") } // HIGH, MEDIUM, LOW

    val reductionPct = when (selectedQuality) {
        "HIGH" -> 55
        "MEDIUM" -> 35
        else -> 20
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .clip(RoundedCornerShape(16.dp))
                .border(1.dp, DocBorderDark, RoundedCornerShape(16.dp)),
            colors = CardDefaults.cardColors(containerColor = DocSurfaceDark)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0284C7)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Compress, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("PDF Compressor Tool", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                            Text("Optimize file size without losing legibility", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }
                    }

                    IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF94A3B8))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // File Info Card
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(DocSurfaceCardDark)
                        .border(1.dp, DocBorderDark, RoundedCornerShape(10.dp))
                        .padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(document.title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("${document.pageCount} sheets • Original Size: ${document.fileSizeFormatted}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFF10B981).copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("~$reductionPct% REDUCTION", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text("CHOOSE COMPRESSION PRESET", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    CompressionPresetOption(
                        title = "Extreme Compression (~55% smaller)",
                        description = "72 DPI downsampling • Best for fast email, WhatsApp & mobile messaging",
                        isSelected = selectedQuality == "HIGH",
                        onClick = { selectedQuality = "HIGH" }
                    )

                    CompressionPresetOption(
                        title = "Recommended (~35% smaller)",
                        description = "150 DPI balanced vector stream • Perfect balance of clarity and size",
                        isSelected = selectedQuality == "MEDIUM",
                        onClick = { selectedQuality = "MEDIUM" }
                    )

                    CompressionPresetOption(
                        title = "High Quality (~20% smaller)",
                        description = "300 DPI preservation • Maximum architectural detail & crisp line art",
                        isSelected = selectedQuality == "LOW",
                        onClick = { selectedQuality = "LOW" }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Cancel", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            onCompress(document, selectedQuality)
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .weight(1.5f)
                            .testTag("confirm_compress_pdf_button")
                    ) {
                        Icon(Icons.Default.Compress, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compress & Share", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompressionPresetOption(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .border(1.dp, if (isSelected) DocPrimaryCyan else DocBorderDark, RoundedCornerShape(10.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFF0C2444) else DocSurfaceCardDark
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
                colors = RadioButtonDefaults.colors(selectedColor = DocPrimaryCyan, unselectedColor = Color(0xFF64748B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(description, color = Color(0xFF94A3B8), fontSize = 10.sp, lineHeight = 13.sp)
            }
        }
    }
}
