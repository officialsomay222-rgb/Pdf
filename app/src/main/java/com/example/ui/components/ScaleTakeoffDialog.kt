package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Share
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
import com.example.model.MeasurementItem
import com.example.model.MeasurementType
import com.example.model.ScaleCalibration
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun ScaleTakeoffDialog(
    measurements: List<MeasurementItem>,
    activeScale: ScaleCalibration,
    onDeleteMeasurement: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // Calculate total linear distance and total polygon area
    var totalLinearFeet = 0f
    var totalAreaSqFt = 0f

    for (m in measurements) {
        if (m.type == MeasurementType.DISTANCE_LINE && m.points.size >= 2) {
            val p1 = m.points[0]
            val p2 = m.points[1]
            val px = Math.hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
            totalLinearFeet += m.scale.toReal(px)
        } else if (m.type == MeasurementType.AREA_POLYGON && m.points.size >= 3) {
            // Shoelace formula
            var area = 0f
            for (i in m.points.indices) {
                val j = (i + 1) % m.points.size
                area += m.points[i].x * m.points[j].y
                area -= m.points[j].x * m.points[i].y
            }
            val pixelArea = Math.abs(area) / 2.0f
            val ratio = m.scale.realDistance / m.scale.pixelDistance
            totalAreaSqFt += pixelArea * ratio * ratio
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DocSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ListAlt,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Measurement & Takeoff Schedule",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 420.dp)) {
                // Summary Cards
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("TOTAL AREA", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("%,.1f sq %s", totalAreaSqFt, activeScale.unit),
                                color = Color(0xFF10B981),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Card(
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = DocSurfaceCardDark)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("TOTAL LINEAL", color = Color(0xFF94A3B8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(
                                text = String.format("%,.1f %s", totalLinearFeet, activeScale.unit),
                                color = DocPrimaryCyan,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Drawing Items (${measurements.size}):",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (measurements.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No measurements drawn yet. Select Distance or Area tool.", color = Color.Gray, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f, fill = false),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(measurements) { item ->
                            val valueStr = when (item.type) {
                                MeasurementType.DISTANCE_LINE -> {
                                    if (item.points.size >= 2) {
                                        val px = Math.hypot((item.points[1].x - item.points[0].x).toDouble(), (item.points[1].y - item.points[0].y).toDouble()).toFloat()
                                        item.scale.format(px)
                                    } else "0"
                                }
                                MeasurementType.AREA_POLYGON -> {
                                    var area = 0f
                                    for (i in item.points.indices) {
                                        val j = (i + 1) % item.points.size
                                        area += item.points[i].x * item.points[j].y
                                        area -= item.points[j].x * item.points[i].y
                                    }
                                    val pixelArea = Math.abs(area) / 2.0f
                                    item.scale.formatArea(pixelArea)
                                }
                                else -> "Tracked"
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(DocSurfaceCardDark)
                                    .border(0.5.dp, DocBorderDark, RoundedCornerShape(6.dp))
                                    .padding(horizontal = 10.dp, vertical = 6.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.label, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = "${item.type.name.replace("_", " ")} • Page ${item.pageIndex + 1}",
                                        color = Color(0xFF94A3B8),
                                        fontSize = 10.sp
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = valueStr,
                                        color = Color(0xFFF59E0B),
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    IconButton(
                                        onClick = { onDeleteMeasurement(item.id) },
                                        modifier = Modifier.size(24.dp).testTag("delete_meas_${item.id}")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(16.dp)
                                        )
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
                Text("Close", color = Color.White)
            }
        }
    )
}
