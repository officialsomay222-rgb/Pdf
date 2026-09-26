package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DocumentCategory
import com.example.ui.theme.DocBorderDark
import com.example.ui.theme.DocPrimaryCyan
import com.example.ui.theme.DocSurfaceCardDark
import com.example.ui.theme.DocSurfaceDark

@Composable
fun CadMiniMap(
    category: DocumentCategory,
    zoomLevel: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    canvasViewportWidth: Float,
    canvasViewportHeight: Float,
    docWidth: Float,
    docHeight: Float,
    onNavigate: (newPanX: Float, newPanY: Float) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val miniMapWidth = 140.dp
    val miniMapHeight = 100.dp

    Box(
        modifier = modifier
            .width(miniMapWidth)
            .height(miniMapHeight)
            .shadow(8.dp, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
            .background(DocSurfaceDark.copy(alpha = 0.92f))
            .border(1.dp, DocBorderDark, RoundedCornerShape(8.dp))
            .testTag("cad_minimap_navigator")
    ) {
        // MiniMap Canvas
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(docWidth, docHeight, zoomLevel) {
                    detectTapGestures { tapOffset ->
                        val scaleRatioX = docWidth / size.width
                        val scaleRatioY = docHeight / size.height
                        val targetDocX = tapOffset.x * scaleRatioX
                        val targetDocY = tapOffset.y * scaleRatioY

                        val newPanX = (size.width / 2f) - (targetDocX * zoomLevel)
                        val newPanY = (size.height / 2f) - (targetDocY * zoomLevel)
                        onNavigate(newPanX, newPanY)
                    }
                }
                .pointerInput(docWidth, docHeight, zoomLevel) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val scaleRatioX = docWidth / size.width
                        val scaleRatioY = docHeight / size.height
                        val newPanX = panOffsetX - (dragAmount.x * scaleRatioX * zoomLevel * 0.8f)
                        val newPanY = panOffsetY - (dragAmount.y * scaleRatioY * zoomLevel * 0.8f)
                        onNavigate(newPanX, newPanY)
                    }
                }
        ) {
            val mapW = size.width
            val mapH = size.height

            // 1. Draw Document Outline
            drawRect(
                color = Color(0xFF1E293B),
                size = Size(mapW, mapH)
            )

            // 2. Technical Floorplan / Document Wireframe Silhouette
            val wireframeColor = when (category) {
                DocumentCategory.BLUEPRINT -> Color(0xFF0284C7).copy(alpha = 0.4f)
                DocumentCategory.CONTRACT -> Color(0xFF94A3B8).copy(alpha = 0.3f)
                else -> Color(0xFF10B981).copy(alpha = 0.35f)
            }

            // Blueprint rooms silhouette
            drawRect(
                color = wireframeColor,
                topLeft = Offset(mapW * 0.15f, mapH * 0.15f),
                size = Size(mapW * 0.7f, mapH * 0.7f),
                style = Stroke(width = 1.5f)
            )
            drawLine(
                color = wireframeColor,
                start = Offset(mapW * 0.45f, mapH * 0.15f),
                end = Offset(mapW * 0.45f, mapH * 0.85f),
                strokeWidth = 1f
            )
            drawLine(
                color = wireframeColor,
                start = Offset(mapW * 0.15f, mapH * 0.5f),
                end = Offset(mapW * 0.85f, mapH * 0.5f),
                strokeWidth = 1f
            )

            // 3. Viewport Bounding Box (represents what the user currently sees)
            val vpWOnDoc = (canvasViewportWidth / zoomLevel).coerceAtMost(docWidth)
            val vpHOnDoc = (canvasViewportHeight / zoomLevel).coerceAtMost(docHeight)

            val docLeftInView = (-panOffsetX / zoomLevel).coerceIn(0f, docWidth)
            val docTopInView = (-panOffsetY / zoomLevel).coerceIn(0f, docHeight)

            val vpLeftOnMap = (docLeftInView / docWidth) * mapW
            val vpTopOnMap = (docTopInView / docHeight) * mapH
            val vpWidthOnMap = ((vpWOnDoc / docWidth) * mapW).coerceIn(12f, mapW)
            val vpHeightOnMap = ((vpHOnDoc / docHeight) * mapH).coerceIn(10f, mapH)

            // Fill
            drawRect(
                color = DocPrimaryCyan.copy(alpha = 0.25f),
                topLeft = Offset(vpLeftOnMap, vpTopOnMap),
                size = Size(vpWidthOnMap, vpHeightOnMap)
            )
            // Stroke
            drawRect(
                color = Color(0xFF38BDF8),
                topLeft = Offset(vpLeftOnMap, vpTopOnMap),
                size = Size(vpWidthOnMap, vpHeightOnMap),
                style = Stroke(width = 2f)
            )
        }

        // Header overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DocSurfaceDark.copy(alpha = 0.8f))
                .padding(horizontal = 6.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Layers,
                    contentDescription = null,
                    tint = DocPrimaryCyan,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                    text = "NAVIGATOR",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
            IconButton(
                onClick = onClose,
                modifier = Modifier.size(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Mini-map",
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}
