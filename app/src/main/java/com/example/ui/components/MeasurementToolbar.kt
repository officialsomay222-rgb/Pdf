package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import com.example.model.MeasurementItem
import com.example.model.MeasurementType
import com.example.model.ScaleCalibration
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark

@Composable
fun MeasurementToolbar(
    activeScale: ScaleCalibration,
    selectedMeasurementType: MeasurementType,
    onMeasurementTypeSelect: (MeasurementType) -> Unit,
    onOpenCalibration: () -> Unit,
    onOpenTakeoffSummary: () -> Unit,
    measurementsCount: Int,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(DocSurfaceCardDark)
            .border(width = 0.5.dp, color = DocBorderDark)
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .horizontalScroll(scrollState),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Active Scale Badge with Calibrate Action
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, DocPrimaryCyan.copy(alpha = 0.6f), RoundedCornerShape(6.dp))
                .clickable(onClick = onOpenCalibration)
                .padding(horizontal = 8.dp, vertical = 5.dp)
                .testTag("scale_calibration_badge"),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Straighten,
                contentDescription = "Calibrate Scale",
                tint = DocPrimaryCyan,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column {
                Text(
                    text = "SCALE: 100px = ${activeScale.realDistance.toInt()} ${activeScale.unit}",
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Tap to Calibrate",
                    color = DocPrimaryCyan,
                    fontSize = 8.sp
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Tool: Distance Line
        MeasureTypeButton(
            label = "Distance",
            icon = Icons.Default.ShowChart,
            isSelected = selectedMeasurementType == MeasurementType.DISTANCE_LINE,
            onClick = { onMeasurementTypeSelect(MeasurementType.DISTANCE_LINE) }
        )

        // Tool: Area Polygon
        MeasureTypeButton(
            label = "Area (Sq Ft)",
            icon = Icons.Default.SquareFoot,
            isSelected = selectedMeasurementType == MeasurementType.AREA_POLYGON,
            onClick = { onMeasurementTypeSelect(MeasurementType.AREA_POLYGON) }
        )

        // Tool: Perimeter Path
        MeasureTypeButton(
            label = "Perimeter",
            icon = Icons.Default.Timeline,
            isSelected = selectedMeasurementType == MeasurementType.PERIMETER_PATH,
            onClick = { onMeasurementTypeSelect(MeasurementType.PERIMETER_PATH) }
        )

        // Tool: Angle Arc
        MeasureTypeButton(
            label = "Angle",
            icon = Icons.Default.ChangeHistory,
            isSelected = selectedMeasurementType == MeasurementType.ANGLE_ARC,
            onClick = { onMeasurementTypeSelect(MeasurementType.ANGLE_ARC) }
        )

        Spacer(modifier = Modifier.width(6.dp))

        // Takeoff Summary Drawer Button
        Button(
            onClick = onOpenTakeoffSummary,
            modifier = Modifier
                .height(30.dp)
                .testTag("open_takeoff_summary_button"),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ListAlt,
                contentDescription = null,
                modifier = Modifier.size(13.dp),
                tint = Color.White
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Takeoffs ($measurementsCount)",
                fontSize = 11.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun MeasureTypeButton(
    label: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val bg = if (isSelected) Color(0xFFF59E0B).copy(alpha = 0.25f) else Color.Transparent
    val border = if (isSelected) Color(0xFFF59E0B) else DocBorderDark

    Row(
        modifier = Modifier
            .padding(end = 4.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(bg)
            .border(1.dp, border, RoundedCornerShape(6.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 5.dp)
            .testTag("measure_type_${label.lowercase().replace(" ", "_")}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (isSelected) Color(0xFFF59E0B) else Color(0xFF94A3B8),
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
