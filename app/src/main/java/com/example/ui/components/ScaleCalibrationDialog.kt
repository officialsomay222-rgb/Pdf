package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Straighten
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
import com.example.model.ScaleCalibration
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun ScaleCalibrationDialog(
    currentScale: ScaleCalibration,
    onDismiss: () -> Unit,
    onSaveScale: (ScaleCalibration) -> Unit
) {
    var realDistText by remember { mutableStateOf(currentScale.realDistance.toString()) }
    var selectedUnit by remember { mutableStateOf(currentScale.unit) }

    val presets = listOf(
        Triple("1/4\" = 1'-0\" (Architectural)", 10f, "ft"),
        Triple("1/8\" = 1'-0\" (Structural / MEP)", 20f, "ft"),
        Triple("1:100 Metric Plan", 10f, "m"),
        Triple("1:50 Detail Section", 5f, "m"),
        Triple("1\" = 30'-0\" (Civil / Site)", 30f, "ft")
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DocSurfaceDark,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Straighten,
                    contentDescription = null,
                    tint = DocPrimaryCyan,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Architectural Scale Calibration",
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Standard Industry Scale Presets (100 px reference):",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                presets.forEach { (name, distance, unit) ->
                    val isSelected = realDistText == distance.toString() && selectedUnit == unit
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isSelected) DocPrimaryCyan.copy(alpha = 0.2f) else DocSurfaceCardDark)
                            .border(1.dp, if (isSelected) DocPrimaryCyan else DocBorderDark, RoundedCornerShape(6.dp))
                            .clickable {
                                realDistText = distance.toString()
                                selectedUnit = unit
                            }
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = name,
                            color = if (isSelected) Color.White else Color(0xFFCBD5E1),
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                        )
                        Text(
                            text = "$distance $unit",
                            color = DocPrimaryCyan,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Custom Calibration Value:",
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    modifier = Modifier.padding(bottom = 4.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = realDistText,
                        onValueChange = { realDistText = it },
                        label = { Text("Length (for 100px)", fontSize = 11.sp) },
                        modifier = Modifier.weight(1f).testTag("custom_scale_value_input"),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedBorderColor = DocPrimaryCyan,
                            unfocusedBorderColor = DocBorderDark
                        )
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Row {
                        listOf("ft", "m", "in").forEach { unit ->
                            val isUnitSelected = selectedUnit == unit
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isUnitSelected) DocPrimaryCyan else DocSurfaceCardDark)
                                    .clickable { selectedUnit = unit }
                                    .padding(horizontal = 8.dp, vertical = 12.dp)
                            ) {
                                Text(
                                    text = unit,
                                    color = if (isUnitSelected) Color.White else Color(0xFF94A3B8),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dist = realDistText.toFloatOrNull() ?: 10f
                    onSaveScale(
                        ScaleCalibration(
                            pixelDistance = 100f,
                            realDistance = dist,
                            unit = selectedUnit
                        )
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = DocPrimaryCyan),
                modifier = Modifier.testTag("apply_scale_button")
            ) {
                Text("Apply Scale", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Color(0xFF94A3B8))
            }
        }
    )
}
