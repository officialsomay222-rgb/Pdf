package com.example.ui.canvas

import android.view.HapticFeedbackConstants
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import com.example.model.*
import kotlin.math.*

@Composable
fun DocumentCanvas(
    category: DocumentCategory,
    pageIndex: Int,
    pageCount: Int,
    documentTitle: String,
    watermarkText: String,
    zoomLevel: Float,
    panOffsetX: Float,
    panOffsetY: Float,
    onTransform: (zoomChange: Float, panChangeX: Float, panChangeY: Float) -> Unit,
    toolMode: WorkspaceToolMode,
    markupType: MarkupType,
    activeColorHex: Long,
    strokeWidth: Float,
    selectedStamp: StampType,
    annotations: List<AnnotationItem>,
    onAddAnnotation: (AnnotationItem) -> Unit,
    onDeleteAnnotation: (String) -> Unit,
    measurements: List<MeasurementItem>,
    measurementType: MeasurementType,
    activeScale: ScaleCalibration,
    onAddMeasurement: (MeasurementItem) -> Unit,
    formFields: List<FormFieldItem>,
    onFormFieldClick: (FormFieldItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val view = LocalView.current

    // Active in-progress drawing points
    var currentDrawPoints by remember { mutableStateOf<List<Point2D>>(emptyList()) }
    var laserPointerPos by remember { mutableStateOf<Offset?>(null) }
    val laserTrail = remember { mutableStateListOf<Offset>() }

    // Page virtual dimensions in canvas coordinates
    val docPageWidth = 720f
    val docPageHeight = 980f

    // Transform state for pinch-to-zoom
    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        if (toolMode == WorkspaceToolMode.VIEW_NAVIGATE) {
            onTransform(zoomChange, panChange.x, panChange.y)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0A0F1D))
            .transformable(state = transformState)
            .pointerInput(toolMode, markupType, measurementType, zoomLevel, panOffsetX, panOffsetY) {
                if (toolMode == WorkspaceToolMode.VIEW_NAVIGATE) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        onTransform(1f, dragAmount.x, dragAmount.y)
                    }
                } else if (toolMode == WorkspaceToolMode.MARKUP_DRAW) {
                    if (markupType == MarkupType.LASER_POINTER) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                laserPointerPos = offset
                                laserTrail.add(offset)
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                laserPointerPos = change.position
                                laserTrail.add(change.position)
                                if (laserTrail.size > 20) laserTrail.removeAt(0)
                            },
                            onDragEnd = {
                                laserPointerPos = null
                                laserTrail.clear()
                            }
                        )
                    } else if (markupType == MarkupType.MAGIC_ERASER) {
                        detectTapGestures { tapOffset ->
                            val docX = (tapOffset.x - panOffsetX) / zoomLevel
                            val docY = (tapOffset.y - panOffsetY) / zoomLevel
                            // Find nearest annotation to erase
                            val target = annotations.find { ann ->
                                ann.points.any { pt ->
                                    hypot((pt.x - docX).toDouble(), (pt.y - docY).toDouble()) < 30.0
                                }
                            }
                            if (target != null) {
                                view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                                onDeleteAnnotation(target.id)
                            }
                        }
                    } else {
                        // Drawing Pen, Highlighter, Clouds, Shapes, Stamps
                        detectDragGestures(
                            onDragStart = { offset ->
                                val docX = (offset.x - panOffsetX) / zoomLevel
                                val docY = (offset.y - panOffsetY) / zoomLevel

                                if (markupType == MarkupType.STAMP) {
                                    // Instant stamp drop
                                    view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                    onAddAnnotation(
                                        AnnotationItem(
                                            pageIndex = pageIndex,
                                            type = MarkupType.STAMP,
                                            points = listOf(Point2D(docX, docY)),
                                            stampType = selectedStamp,
                                            colorHex = selectedStamp.colorHex
                                        )
                                    )
                                } else {
                                    currentDrawPoints = listOf(Point2D(docX, docY))
                                }
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val docX = (change.position.x - panOffsetX) / zoomLevel
                                val docY = (change.position.y - panOffsetY) / zoomLevel
                                currentDrawPoints = currentDrawPoints + Point2D(docX, docY)
                            },
                            onDragEnd = {
                                if (currentDrawPoints.size >= 2) {
                                    view.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
                                    val newAnn = AnnotationItem(
                                        pageIndex = pageIndex,
                                        type = markupType,
                                        points = currentDrawPoints,
                                        colorHex = activeColorHex,
                                        strokeWidth = strokeWidth,
                                        opacity = if (markupType == MarkupType.HIGHLIGHTER) 0.45f else 1.0f,
                                        text = if (markupType == MarkupType.CALLOUT) "REVISION NOTE: Verify Section C" else ""
                                    )
                                    onAddAnnotation(newAnn)
                                }
                                currentDrawPoints = emptyList()
                            }
                        )
                    }
                } else if (toolMode == WorkspaceToolMode.CAD_MEASURE) {
                    // CAD Measurement Line & Area Polygon
                    detectDragGestures(
                        onDragStart = { offset ->
                            val docX = (offset.x - panOffsetX) / zoomLevel
                            val docY = (offset.y - panOffsetY) / zoomLevel
                            currentDrawPoints = listOf(Point2D(docX, docY))
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            val docX = (change.position.x - panOffsetX) / zoomLevel
                            val docY = (change.position.y - panOffsetY) / zoomLevel
                            currentDrawPoints = currentDrawPoints + Point2D(docX, docY)
                        },
                        onDragEnd = {
                            if (currentDrawPoints.size >= 2) {
                                view.performHapticFeedback(HapticFeedbackConstants.CONFIRM)
                                val endpoints = if (measurementType == MeasurementType.DISTANCE_LINE) {
                                    listOf(currentDrawPoints.first(), currentDrawPoints.last())
                                } else {
                                    // Closed area polygon corners
                                    val start = currentDrawPoints.first()
                                    val end = currentDrawPoints.last()
                                    listOf(
                                        start,
                                        Point2D(end.x, start.y),
                                        end,
                                        Point2D(start.x, end.y)
                                    )
                                }

                                val label = if (measurementType == MeasurementType.DISTANCE_LINE) "Measured Span" else "Area Takeoff"
                                onAddMeasurement(
                                    MeasurementItem(
                                        pageIndex = pageIndex,
                                        type = measurementType,
                                        points = endpoints,
                                        colorHex = 0xFFF59E0B,
                                        strokeWidth = 2.5f,
                                        label = label,
                                        scale = activeScale
                                    )
                                )
                            }
                            currentDrawPoints = emptyList()
                        }
                    )
                } else if (toolMode == WorkspaceToolMode.FORMS_FILL) {
                    detectTapGestures { tapOffset ->
                        val docX = (tapOffset.x - panOffsetX) / zoomLevel
                        val docY = (tapOffset.y - panOffsetY) / zoomLevel

                        val clickedField = formFields.find { field ->
                            field.pageIndex == pageIndex &&
                            docX >= field.x && docX <= (field.x + field.width) &&
                            docY >= field.y && docY <= (field.y + field.height)
                        }
                        if (clickedField != null) {
                            view.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK)
                            onFormFieldClick(clickedField)
                        }
                    }
                } else if (toolMode == WorkspaceToolMode.TEXT_EDIT) {
                    detectTapGestures { tapOffset ->
                        val docX = (tapOffset.x - panOffsetX) / zoomLevel
                        val docY = (tapOffset.y - panOffsetY) / zoomLevel
                        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                        onAddAnnotation(
                            AnnotationItem(
                                pageIndex = pageIndex,
                                type = MarkupType.TEXT_NOTE,
                                points = listOf(Point2D(docX, docY)),
                                text = "Spec 2026: Approved for Installation",
                                colorHex = activeColorHex,
                                fontSize = 13f
                            )
                        )
                    }
                }
            }
            .testTag("document_workspace_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Apply Master Pan and Zoom Matrix
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Document sheet origin with pan & zoom
            val pageLeft = panOffsetX
            val pageTop = panOffsetY
            val scaledPageWidth = docPageWidth * zoomLevel
            val scaledPageHeight = docPageHeight * zoomLevel

            // 1. Draw Document Canvas Sheet Shadow & Base
            drawRoundRect(
                color = Color.Black.copy(alpha = 0.5f),
                topLeft = Offset(pageLeft + 4f, pageTop + 6f),
                size = Size(scaledPageWidth, scaledPageHeight),
                cornerRadius = CornerRadius(6f, 6f)
            )

            // Sheet Paper Background
            val paperColor = when (category) {
                DocumentCategory.BLUEPRINT -> Color(0xFF0F1E36) // Deep blueprint midnight navy
                DocumentCategory.CONTRACT -> Color(0xFFFAFAFA)  // Crisp executive bond paper
                DocumentCategory.INSPECTION_FORM -> Color(0xFFF8FAFC) // Municipal technical form
                else -> Color(0xFF1E293B)
            }
            drawRoundRect(
                color = paperColor,
                topLeft = Offset(pageLeft, pageTop),
                size = Size(scaledPageWidth, scaledPageHeight),
                cornerRadius = CornerRadius(4f, 4f)
            )

            // Document Border Line
            drawRoundRect(
                color = when (category) {
                    DocumentCategory.BLUEPRINT -> Color(0xFF0284C7)
                    else -> Color(0xFFCBD5E1)
                },
                topLeft = Offset(pageLeft, pageTop),
                size = Size(scaledPageWidth, scaledPageHeight),
                cornerRadius = CornerRadius(4f, 4f),
                style = Stroke(width = 1.5f)
            )

            // 2. Render Specialized Content based on Category
            if (category == DocumentCategory.BLUEPRINT) {
                renderArchitecturalBlueprint(
                    pageLeft = pageLeft,
                    pageTop = pageTop,
                    scale = zoomLevel,
                    pageWidth = docPageWidth,
                    pageHeight = docPageHeight,
                    pageIndex = pageIndex,
                    docTitle = documentTitle
                )
            } else if (category == DocumentCategory.CONTRACT) {
                renderExecutiveContract(
                    pageLeft = pageLeft,
                    pageTop = pageTop,
                    scale = zoomLevel,
                    pageWidth = docPageWidth,
                    pageHeight = docPageHeight,
                    pageIndex = pageIndex
                )
            } else {
                renderMunicipalInspectionForm(
                    pageLeft = pageLeft,
                    pageTop = pageTop,
                    scale = zoomLevel,
                    pageWidth = docPageWidth,
                    pageHeight = docPageHeight,
                    pageIndex = pageIndex
                )
            }

            // 3. Security Diagonal Watermark
            if (watermarkText.isNotBlank()) {
                val centerDocX = pageLeft + (scaledPageWidth / 2f)
                val centerDocY = pageTop + (scaledPageHeight / 2f)
                rotate(degrees = -32f, pivot = Offset(centerDocX, centerDocY)) {
                    drawContext.canvas.nativeCanvas.apply {
                        val p = android.graphics.Paint().apply {
                            color = android.graphics.Color.argb(55, 239, 68, 68)
                            textSize = 34f * zoomLevel
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(watermarkText, centerDocX, centerDocY, p)
                        drawText(watermarkText, centerDocX, centerDocY - 140f * zoomLevel, p)
                        drawText(watermarkText, centerDocX, centerDocY + 140f * zoomLevel, p)
                    }
                }
            }

            // 4. Form Fields
            val pageFields = formFields.filter { it.pageIndex == pageIndex }
            for (field in pageFields) {
                val fx = pageLeft + (field.x * zoomLevel)
                val fy = pageTop + (field.y * zoomLevel)
                val fw = field.width * zoomLevel
                val fh = field.height * zoomLevel

                when (field.type) {
                    FormFieldType.CHECKBOX -> {
                        drawRoundRect(
                            color = if (field.isChecked) Color(0xFF0284C7) else Color(0xFF1E293B),
                            topLeft = Offset(fx, fy),
                            size = Size(fw, fh),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(fx, fy),
                            size = Size(fw, fh),
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(width = 1.5f)
                        )
                        if (field.isChecked) {
                            // Checkmark
                            val path = Path().apply {
                                moveTo(fx + fw * 0.25f, fy + fh * 0.5f)
                                lineTo(fx + fw * 0.45f, fy + fh * 0.75f)
                                lineTo(fx + fw * 0.8f, fy + fh * 0.25f)
                            }
                            drawPath(path, Color.White, style = Stroke(width = 2.5f, cap = StrokeCap.Round))
                        }
                        // Label text
                        drawContext.canvas.nativeCanvas.apply {
                            val p = android.graphics.Paint().apply {
                                color = if (category == DocumentCategory.CONTRACT) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                textSize = 11f * zoomLevel
                                isAntiAlias = true
                            }
                            drawText(field.label, fx + fw + 8f * zoomLevel, fy + fh * 0.75f, p)
                        }
                    }
                    FormFieldType.SIGNATURE_BOX -> {
                        drawRoundRect(
                            color = Color(0xFF0284C7).copy(alpha = 0.15f),
                            topLeft = Offset(fx, fy),
                            size = Size(fw, fh),
                            cornerRadius = CornerRadius(6f, 6f)
                        )
                        drawRoundRect(
                            color = Color(0xFF38BDF8),
                            topLeft = Offset(fx, fy),
                            size = Size(fw, fh),
                            cornerRadius = CornerRadius(6f, 6f),
                            style = Stroke(width = 1.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)))
                        )
                        drawContext.canvas.nativeCanvas.apply {
                            val p = android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(56, 189, 248)
                                textSize = 11f * zoomLevel
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                isAntiAlias = true
                            }
                            val status = if (field.isSigned) "✓ CRYPTOGRAPHICALLY SIGNED" else "TAP TO SIGN (PAdES CERTIFIED)"
                            drawText("${field.label} — $status", fx + 8f * zoomLevel, fy + fh * 0.55f, p)
                        }
                    }
                    else -> {
                        // Text or Dropdown Field
                        drawRoundRect(
                            color = Color(0xFF334155).copy(alpha = 0.4f),
                            topLeft = Offset(fx, fy),
                            size = Size(fw, fh),
                            cornerRadius = CornerRadius(4f, 4f)
                        )
                        drawRoundRect(
                            color = Color(0xFF64748B),
                            topLeft = Offset(fx, fy),
                            size = Size(fw, fh),
                            cornerRadius = CornerRadius(4f, 4f),
                            style = Stroke(width = 1f)
                        )
                        drawContext.canvas.nativeCanvas.apply {
                            val labelPaint = android.graphics.Paint().apply {
                                color = android.graphics.Color.rgb(148, 163, 184)
                                textSize = 9f * zoomLevel
                                isAntiAlias = true
                            }
                            val valPaint = android.graphics.Paint().apply {
                                color = if (category == DocumentCategory.CONTRACT) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                                textSize = 11f * zoomLevel
                                typeface = android.graphics.Typeface.DEFAULT_BOLD
                                isAntiAlias = true
                            }
                            drawText(field.label.uppercase(), fx + 6f * zoomLevel, fy - 4f * zoomLevel, labelPaint)
                            drawText(field.value.ifBlank { "Enter value..." }, fx + 6f * zoomLevel, fy + fh * 0.65f, valPaint)
                        }
                    }
                }
            }

            // 5. Existing Saved Annotations
            val pageAnnotations = annotations.filter { it.pageIndex == pageIndex }
            for (ann in pageAnnotations) {
                renderAnnotation(
                    ann = ann,
                    pageLeft = pageLeft,
                    pageTop = pageTop,
                    zoom = zoomLevel
                )
            }

            // 6. Existing Saved CAD Measurements
            val pageMeasurements = measurements.filter { it.pageIndex == pageIndex }
            for (meas in pageMeasurements) {
                renderMeasurement(
                    meas = meas,
                    pageLeft = pageLeft,
                    pageTop = pageTop,
                    zoom = zoomLevel
                )
            }

            // 7. Active In-Progress Drawing Points (Live Feedback)
            if (currentDrawPoints.size > 1) {
                val livePath = Path()
                val p0 = currentDrawPoints[0]
                livePath.moveTo(pageLeft + p0.x * zoomLevel, pageTop + p0.y * zoomLevel)

                if (markupType == MarkupType.REVISION_CLOUD && currentDrawPoints.size >= 2) {
                    val pEnd = currentDrawPoints.last()
                    val left = minOf(p0.x, pEnd.x)
                    val top = minOf(p0.y, pEnd.y)
                    val right = maxOf(p0.x, pEnd.x)
                    val bottom = maxOf(p0.y, pEnd.y)

                    drawRoundRect(
                        color = Color(activeColorHex),
                        topLeft = Offset(pageLeft + left * zoomLevel, pageTop + top * zoomLevel),
                        size = Size((right - left) * zoomLevel, (bottom - top) * zoomLevel),
                        cornerRadius = CornerRadius(16f * zoomLevel, 16f * zoomLevel),
                        style = Stroke(width = strokeWidth * zoomLevel)
                    )
                } else if (markupType == MarkupType.RECTANGLE || markupType == MarkupType.REDACTION_BOX) {
                    val pEnd = currentDrawPoints.last()
                    val left = minOf(p0.x, pEnd.x)
                    val top = minOf(p0.y, pEnd.y)
                    val right = maxOf(p0.x, pEnd.x)
                    val bottom = maxOf(p0.y, pEnd.y)

                    val isRedaction = markupType == MarkupType.REDACTION_BOX
                    drawRect(
                        color = if (isRedaction) Color.Black else Color(activeColorHex),
                        topLeft = Offset(pageLeft + left * zoomLevel, pageTop + top * zoomLevel),
                        size = Size((right - left) * zoomLevel, (bottom - top) * zoomLevel),
                        style = if (isRedaction) Fill else Stroke(width = strokeWidth * zoomLevel)
                    )
                } else if (toolMode == WorkspaceToolMode.CAD_MEASURE && measurementType == MeasurementType.DISTANCE_LINE) {
                    val pEnd = currentDrawPoints.last()
                    val x1 = pageLeft + p0.x * zoomLevel
                    val y1 = pageTop + p0.y * zoomLevel
                    val x2 = pageLeft + pEnd.x * zoomLevel
                    val y2 = pageTop + pEnd.y * zoomLevel

                    drawLine(
                        color = Color(0xFFF59E0B),
                        start = Offset(x1, y1),
                        end = Offset(x2, y2),
                        strokeWidth = 3f * zoomLevel,
                        cap = StrokeCap.Round
                    )

                    val distDoc = hypot((pEnd.x - p0.x).toDouble(), (pEnd.y - p0.y).toDouble()).toFloat()
                    val liveLabel = activeScale.format(distDoc)
                    drawContext.canvas.nativeCanvas.apply {
                        val p = android.graphics.Paint().apply {
                            color = android.graphics.Color.rgb(245, 158, 11)
                            textSize = 12f * zoomLevel
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = android.graphics.Paint.Align.CENTER
                        }
                        drawText(liveLabel, (x1 + x2) / 2f, (y1 + y2) / 2f - 8f * zoomLevel, p)
                    }
                } else {
                    for (i in 1 until currentDrawPoints.size) {
                        val pt = currentDrawPoints[i]
                        livePath.lineTo(pageLeft + pt.x * zoomLevel, pageTop + pt.y * zoomLevel)
                    }
                    drawPath(
                        path = livePath,
                        color = Color(activeColorHex),
                        style = Stroke(
                            width = strokeWidth * zoomLevel,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }

            // 8. Laser Pointer Overlay (for Presentations & Live Collab)
            if (laserPointerPos != null) {
                // Glow trail
                for (i in laserTrail.indices) {
                    val alpha = (i.toFloat() / laserTrail.size.toFloat()) * 0.7f
                    drawCircle(
                        color = Color(0xFFEF4444).copy(alpha = alpha),
                        radius = (4f + i * 0.4f),
                        center = laserTrail[i]
                    )
                }
                // Hot laser center
                drawCircle(
                    color = Color(0xFFEF4444),
                    radius = 9f,
                    center = laserPointerPos!!
                )
                drawCircle(
                    color = Color.White,
                    radius = 4f,
                    center = laserPointerPos!!
                )
            }
        }
    }
}

// -------------------------------------------------------------
// Sub-renderers for high-fidelity technical vector documents
// -------------------------------------------------------------

private fun DrawScope.renderArchitecturalBlueprint(
    pageLeft: Float,
    pageTop: Float,
    scale: Float,
    pageWidth: Float,
    pageHeight: Float,
    pageIndex: Int,
    docTitle: String
) {
    val sw = pageWidth * scale
    val sh = pageHeight * scale

    // 1. Technical CAD Orthogonal Grid lines
    val gridSpacing = 30f * scale
    val gridPaintColor = Color(0xFF1E3A8A).copy(alpha = 0.25f)
    var x = pageLeft + 20f * scale
    while (x < pageLeft + sw - 20f * scale) {
        drawLine(gridPaintColor, Offset(x, pageTop + 30f * scale), Offset(x, pageTop + sh - 50f * scale), strokeWidth = 0.75f)
        x += gridSpacing
    }
    var y = pageTop + 30f * scale
    while (y < pageTop + sh - 50f * scale) {
        drawLine(gridPaintColor, Offset(pageLeft + 20f * scale, y), Offset(pageLeft + sw - 20f * scale, y), strokeWidth = 0.75f)
        y += gridSpacing
    }

    // 2. Blueprint Architectural Rooms & Walls
    val wallColor = Color(0xFF38BDF8)
    val wallFill = Color(0xFF0369A1).copy(alpha = 0.15f)

    // Room 1: Executive Boardroom 301
    val r1Left = pageLeft + 60f * scale
    val r1Top = pageTop + 80f * scale
    val r1W = 260f * scale
    val r1H = 200f * scale
    drawRect(wallFill, Offset(r1Left, r1Top), Size(r1W, r1H))
    drawRect(wallColor, Offset(r1Left, r1Top), Size(r1W, r1H), style = Stroke(width = 3.5f * scale))

    // Conference Table in Room 1
    drawRoundRect(
        color = Color(0xFF0EA5E9).copy(alpha = 0.3f),
        topLeft = Offset(r1Left + 50f * scale, r1Top + 50f * scale),
        size = Size(160f * scale, 100f * scale),
        cornerRadius = CornerRadius(20f * scale, 20f * scale),
        style = Stroke(width = 1.5f * scale)
    )

    // Room 2: Data Center 303 (with redundant server racks)
    val r2Left = pageLeft + 360f * scale
    val r2Top = pageTop + 80f * scale
    val r2W = 300f * scale
    val r2H = 200f * scale
    drawRect(wallFill, Offset(r2Left, r2Top), Size(r2W, r2H))
    drawRect(wallColor, Offset(r2Left, r2Top), Size(r2W, r2H), style = Stroke(width = 3.5f * scale))

    // Server Racks Rows
    for (rackIndex in 0..4) {
        val rackX = r2Left + (30f + rackIndex * 50f) * scale
        drawRect(
            color = Color(0xFF0284C7).copy(alpha = 0.45f),
            topLeft = Offset(rackX, r2Top + 30f * scale),
            size = Size(35f * scale, 140f * scale),
            style = Stroke(width = 1.2f * scale)
        )
    }

    // HVAC Mechanical Duct with cross-hatch through Corridor
    val ductY = pageTop + 320f * scale
    val ductH = 60f * scale
    drawRect(
        color = Color(0xFF38BDF8).copy(alpha = 0.1f),
        topLeft = Offset(pageLeft + 40f * scale, ductY),
        size = Size(sw - 80f * scale, ductH)
    )
    drawLine(Color(0xFF38BDF8), Offset(pageLeft + 40f * scale, ductY), Offset(pageLeft + sw - 40f * scale, ductY), strokeWidth = 2f * scale)
    drawLine(Color(0xFF38BDF8), Offset(pageLeft + 40f * scale, ductY + ductH), Offset(pageLeft + sw - 40f * scale, ductY + ductH), strokeWidth = 2f * scale)
    // Duct Centerline (dash)
    drawLine(
        color = Color(0xFF38BDF8),
        start = Offset(pageLeft + 40f * scale, ductY + ductH / 2f),
        end = Offset(pageLeft + sw - 40f * scale, ductY + ductH / 2f),
        strokeWidth = 1f * scale,
        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f * scale, 8f * scale))
    )

    // Structural Shear Wall Columns (filled squares)
    val colSize = 14f * scale
    val colPoints = listOf(
        Offset(r1Left, r1Top),
        Offset(r1Left + r1W, r1Top),
        Offset(r1Left, r1Top + r1H),
        Offset(r1Left + r1W, r1Top + r1H),
        Offset(r2Left, r2Top),
        Offset(r2Left + r2W, r2Top),
        Offset(r2Left, r2Top + r2H),
        Offset(r2Left + r2W, r2Top + r2H)
    )
    for (pt in colPoints) {
        drawRect(Color(0xFF38BDF8), Offset(pt.x - colSize / 2f, pt.y - colSize / 2f), Size(colSize, colSize))
    }

    // 3. Technical Blueprint Title Block (Bottom Right)
    val tbW = 280f * scale
    val tbH = 100f * scale
    val tbLeft = pageLeft + sw - tbW - 20f * scale
    val tbTop = pageTop + sh - tbH - 20f * scale
    drawRect(Color(0xFF0F172A), Offset(tbLeft, tbTop), Size(tbW, tbH))
    drawRect(Color(0xFF38BDF8), Offset(tbLeft, tbTop), Size(tbW, tbH), style = Stroke(width = 2f * scale))

    drawContext.canvas.nativeCanvas.apply {
        val pTitle = android.graphics.Paint().apply {
            color = android.graphics.Color.WHITE
            textSize = 10f * scale
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val pSub = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(148, 163, 184)
            textSize = 8f * scale
            isAntiAlias = true
        }
        drawText(docTitle.take(34), tbLeft + 8f * scale, tbTop + 20f * scale, pTitle)
        drawText("SHEET: S-301 • LEVEL 3 FRAMING & MEP", tbLeft + 8f * scale, tbTop + 38f * scale, pSub)
        drawText("SCALE: 1/4\" = 1'-0\" (CALIBRATED)", tbLeft + 8f * scale, tbTop + 54f * scale, pSub)
        drawText("ENGINEER: AUSTIN & PARTNERS STRUCTURAL", tbLeft + 8f * scale, tbTop + 70f * scale, pSub)
        drawText("STATUS: APPROVED FOR CONSTRUCTION", tbLeft + 8f * scale, tbTop + 86f * scale, pTitle)

        // Room Labels on blueprint
        drawText("RM 301: EXEC BOARDROOM", r1Left + 20f * scale, r1Top + 30f * scale, pTitle)
        drawText("768 SQ FT • OCCUPANCY: 28", r1Left + 20f * scale, r1Top + 44f * scale, pSub)

        drawText("RM 303: DATA CENTER", r2Left + 20f * scale, r2Top + 30f * scale, pTitle)
        drawText("N+1 CRAC REDUNDANT CHILL", r2Left + 20f * scale, r2Top + 44f * scale, pSub)

        drawText("MAIN RETURN AIR DUCT 48\"x24\"", pageLeft + 60f * scale, ductY + 36f * scale, pTitle)
    }
}

private fun DrawScope.renderExecutiveContract(
    pageLeft: Float,
    pageTop: Float,
    scale: Float,
    pageWidth: Float,
    pageHeight: Float,
    pageIndex: Int
) {
    val sw = pageWidth * scale
    val sh = pageHeight * scale

    drawContext.canvas.nativeCanvas.apply {
        val hPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 14f * scale
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.SERIF, android.graphics.Typeface.BOLD)
            isAntiAlias = true
        }
        val subPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(100, 116, 139)
            textSize = 9f * scale
            typeface = android.graphics.Typeface.SERIF
            isAntiAlias = true
        }
        val bodyPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(30, 41, 59)
            textSize = 10f * scale
            typeface = android.graphics.Typeface.SERIF
            isAntiAlias = true
        }

        val startX = pageLeft + 45f * scale
        var currY = pageTop + 60f * scale

        drawText("MASTER SERVICES AGREEMENT & SCOPE OF WORK", startX, currY, hPaint)
        currY += 18f * scale
        drawText("CONTRACT IDENTIFIER: APX-2026-9042 • DELAWARE JURISDICTION", startX, currY, subPaint)

        currY += 30f * scale
        drawText("SECTION 1. ENGAGEMENT AND RECITALS", startX, currY, hPaint)
        currY += 16f * scale
        drawText("This Master Services Agreement is entered into by and between Apex Digital Systems Inc.,", startX, currY, bodyPaint)
        currY += 14f * scale
        drawText("and Vortex Infrastructure Corp. Both parties agree to the binding technical specifications herein.", startX, currY, bodyPaint)

        currY += 28f * scale
        drawText("SECTION 2. CONFIDENTIALITY AND SENSITIVE DATA", startX, currY, hPaint)
        currY += 16f * scale
        drawText("Contractor agrees to hold all proprietary blueprints, database schemas, and cryptographic keys in strict trust.", startX, currY, bodyPaint)
        currY += 14f * scale
        drawText("Key Representative SSN: 982-45-7712 (CONFIDENTIAL CLASSIFIED RECORD).", startX, currY, bodyPaint)
        currY += 14f * scale
        drawText("Primary Contact Email: somay.director@apexenterprise.internal", startX, currY, bodyPaint)

        currY += 28f * scale
        drawText("SECTION 3. PAYMENT SCHEDULE & WIRE TRANSFERS", startX, currY, hPaint)
        currY += 16f * scale
        drawText("Total Project Sum: $1,450,000 USD payable across 4 progressive engineering deliverables.", startX, currY, bodyPaint)
        currY += 14f * scale
        drawText("Wire routing number #021000021, Account Number #98234-884-12. Card on file: 4532-8921-9042-3319.", startX, currY, bodyPaint)

        currY += 34f * scale
        drawText("SECTION 4. SIGNATURE ATTESTATION & AUDIT TRAIL", startX, currY, hPaint)
        currY += 16f * scale
        drawText("By executing below, each signatory warrants full corporate power to bind their respective entity.", startX, currY, bodyPaint)
    }
}

private fun DrawScope.renderMunicipalInspectionForm(
    pageLeft: Float,
    pageTop: Float,
    scale: Float,
    pageWidth: Float,
    pageHeight: Float,
    pageIndex: Int
) {
    val sw = pageWidth * scale

    drawContext.canvas.nativeCanvas.apply {
        val hPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.BLACK
            textSize = 13f * scale
            typeface = android.graphics.Typeface.DEFAULT_BOLD
            isAntiAlias = true
        }
        val subPaint = android.graphics.Paint().apply {
            color = android.graphics.Color.rgb(71, 85, 105)
            textSize = 9f * scale
            isAntiAlias = true
        }

        val startX = pageLeft + 35f * scale
        var currY = pageTop + 50f * scale

        drawText("DEPARTMENT OF BUILDING & SAFETY - COMPLIANCE DIVISION", startX, currY, hPaint)
        currY += 16f * scale
        drawText("OFFICIAL PERMIT CERTIFICATION & FIELD INSPECTION FORM", startX, currY, subPaint)
        currY += 14f * scale
        drawText("PERMIT #: CA-2026-88192-BLDG • SECTOR 9 CIVIC DISTRICT", startX, currY, subPaint)
    }

    // Top border separator
    drawLine(
        color = Color(0xFF64748B),
        start = Offset(pageLeft + 35f * scale, pageTop + 90f * scale),
        end = Offset(pageLeft + sw - 35f * scale, pageTop + 90f * scale),
        strokeWidth = 1.5f * scale
    )
}

private fun DrawScope.renderAnnotation(
    ann: AnnotationItem,
    pageLeft: Float,
    pageTop: Float,
    zoom: Float
) {
    val color = Color(ann.colorHex)

    when (ann.type) {
        MarkupType.PEN -> {
            if (ann.points.size > 1) {
                val path = Path()
                path.moveTo(pageLeft + ann.points[0].x * zoom, pageTop + ann.points[0].y * zoom)
                for (i in 1 until ann.points.size) {
                    path.lineTo(pageLeft + ann.points[i].x * zoom, pageTop + ann.points[i].y * zoom)
                }
                drawPath(
                    path = path,
                    color = color,
                    style = Stroke(
                        width = ann.strokeWidth * zoom,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )
            }
        }
        MarkupType.HIGHLIGHTER -> {
            if (ann.points.size >= 2) {
                val p1 = ann.points[0]
                val p2 = ann.points[1]
                val left = minOf(p1.x, p2.x)
                val top = minOf(p1.y, p2.y)
                val right = maxOf(p1.x, p2.x)
                val bottom = maxOf(p1.y, p2.y)

                drawRect(
                    color = color.copy(alpha = ann.opacity),
                    topLeft = Offset(pageLeft + left * zoom, pageTop + top * zoom),
                    size = Size((right - left) * zoom, (bottom - top) * zoom)
                )
            }
        }
        MarkupType.REVISION_CLOUD -> {
            if (ann.points.size >= 2) {
                val p1 = ann.points[0]
                val p2 = ann.points[1]
                val left = minOf(p1.x, p2.x)
                val top = minOf(p1.y, p2.y)
                val right = maxOf(p1.x, p2.x)
                val bottom = maxOf(p1.y, p2.y)

                // Scalloped cloud outline
                drawRoundRect(
                    color = color,
                    topLeft = Offset(pageLeft + left * zoom, pageTop + top * zoom),
                    size = Size((right - left) * zoom, (bottom - top) * zoom),
                    cornerRadius = CornerRadius(16f * zoom, 16f * zoom),
                    style = Stroke(width = ann.strokeWidth * zoom)
                )
            }
        }
        MarkupType.CALLOUT -> {
            if (ann.points.size >= 2) {
                val p1 = ann.points[0]
                val p2 = ann.points[1]
                val x1 = pageLeft + p1.x * zoom
                val y1 = pageTop + p1.y * zoom
                val x2 = pageLeft + p2.x * zoom
                val y2 = pageTop + p2.y * zoom

                // Leader line
                drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = 2f * zoom)
                // Arrow head
                drawCircle(color, radius = 4f * zoom, center = Offset(x1, y1))
                // Text badge
                if (ann.text.isNotBlank()) {
                    drawRoundRect(
                        color = Color(0xFF0F172A),
                        topLeft = Offset(x2, y2 - 24f * zoom),
                        size = Size(220f * zoom, 28f * zoom),
                        cornerRadius = CornerRadius(4f * zoom, 4f * zoom)
                    )
                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x2, y2 - 24f * zoom),
                        size = Size(220f * zoom, 28f * zoom),
                        cornerRadius = CornerRadius(4f * zoom, 4f * zoom),
                        style = Stroke(width = 1f * zoom)
                    )
                    drawContext.canvas.nativeCanvas.apply {
                        val p = android.graphics.Paint().apply {
                            this.color = android.graphics.Color.WHITE
                            textSize = 10f * zoom
                            isAntiAlias = true
                        }
                        drawText(ann.text, x2 + 6f * zoom, y2 - 8f * zoom, p)
                    }
                }
            }
        }
        MarkupType.RECTANGLE -> {
            if (ann.points.size >= 2) {
                val p1 = ann.points[0]
                val p2 = ann.points[1]
                val left = minOf(p1.x, p2.x)
                val top = minOf(p1.y, p2.y)
                val right = maxOf(p1.x, p2.x)
                val bottom = maxOf(p1.y, p2.y)

                drawRect(
                    color = color,
                    topLeft = Offset(pageLeft + left * zoom, pageTop + top * zoom),
                    size = Size((right - left) * zoom, (bottom - top) * zoom),
                    style = Stroke(width = ann.strokeWidth * zoom)
                )
            }
        }
        MarkupType.REDACTION_BOX -> {
            if (ann.points.size >= 2) {
                val p1 = ann.points[0]
                val p2 = ann.points[1]
                val left = minOf(p1.x, p2.x)
                val top = minOf(p1.y, p2.y)
                val right = maxOf(p1.x, p2.x)
                val bottom = maxOf(p1.y, p2.y)

                drawRect(
                    color = Color.Black,
                    topLeft = Offset(pageLeft + left * zoom, pageTop + top * zoom),
                    size = Size((right - left) * zoom, (bottom - top) * zoom)
                )
                // Redaction marker label
                drawContext.canvas.nativeCanvas.apply {
                    val p = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 8f * zoom
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    drawText("REDACTED", pageLeft + left * zoom + 4f * zoom, pageTop + top * zoom + 14f * zoom, p)
                }
            }
        }
        MarkupType.TEXT_NOTE -> {
            if (ann.points.isNotEmpty()) {
                val p = ann.points[0]
                val x = pageLeft + p.x * zoom
                val y = pageTop + p.y * zoom

                drawContext.canvas.nativeCanvas.apply {
                    val textP = android.graphics.Paint().apply {
                        this.color = ann.colorHex.toInt()
                        textSize = ann.fontSize * zoom
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        isAntiAlias = true
                    }
                    drawText(ann.text, x, y, textP)
                }
            }
        }
        MarkupType.STAMP -> {
            if (ann.points.isNotEmpty() && ann.stampType != null) {
                val p = ann.points[0]
                val sx = pageLeft + p.x * zoom
                val sy = pageTop + p.y * zoom
                val sw = 170f * zoom
                val sh = 44f * zoom
                val stampColor = Color(ann.stampType.colorHex)

                rotate(degrees = -6f, pivot = Offset(sx + sw / 2f, sy + sh / 2f)) {
                    drawRoundRect(
                        color = Color(0xFF0F172A).copy(alpha = 0.85f),
                        topLeft = Offset(sx, sy),
                        size = Size(sw, sh),
                        cornerRadius = CornerRadius(6f * zoom, 6f * zoom)
                    )
                    drawRoundRect(
                        color = stampColor,
                        topLeft = Offset(sx, sy),
                        size = Size(sw, sh),
                        cornerRadius = CornerRadius(6f * zoom, 6f * zoom),
                        style = Stroke(width = 2.5f * zoom)
                    )
                    drawContext.canvas.nativeCanvas.apply {
                        val textP = android.graphics.Paint().apply {
                            this.color = ann.stampType.colorHex.toInt()
                            textSize = 13f * zoom
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            textAlign = android.graphics.Paint.Align.CENTER
                            isAntiAlias = true
                        }
                        drawText(ann.stampType.label, sx + sw / 2f, sy + 28f * zoom, textP)
                    }
                }
            }
        }
        else -> {}
    }
}

private fun DrawScope.renderMeasurement(
    meas: MeasurementItem,
    pageLeft: Float,
    pageTop: Float,
    zoom: Float
) {
    val color = Color(meas.colorHex)

    when (meas.type) {
        MeasurementType.DISTANCE_LINE -> {
            if (meas.points.size >= 2) {
                val p1 = meas.points[0]
                val p2 = meas.points[1]
                val x1 = pageLeft + p1.x * zoom
                val y1 = pageTop + p1.y * zoom
                val x2 = pageLeft + p2.x * zoom
                val y2 = pageTop + p2.y * zoom

                // Dimension line
                drawLine(color, Offset(x1, y1), Offset(x2, y2), strokeWidth = meas.strokeWidth * zoom)

                // Architectural End Ticks
                val tickLen = 7f * zoom
                drawLine(color, Offset(x1 - tickLen, y1 - tickLen), Offset(x1 + tickLen, y1 + tickLen), strokeWidth = 2.5f * zoom)
                drawLine(color, Offset(x2 - tickLen, y2 - tickLen), Offset(x2 + tickLen, y2 + tickLen), strokeWidth = 2.5f * zoom)

                // Real calibrated dimension callout
                val distPx = hypot((p2.x - p1.x).toDouble(), (p2.y - p1.y).toDouble()).toFloat()
                val dimText = meas.scale.format(distPx)

                drawContext.canvas.nativeCanvas.apply {
                    val p = android.graphics.Paint().apply {
                        this.color = meas.colorHex.toInt()
                        textSize = 11f * zoom
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText(dimText, (x1 + x2) / 2f, (y1 + y2) / 2f - 6f * zoom, p)
                }
            }
        }
        MeasurementType.AREA_POLYGON -> {
            if (meas.points.size >= 3) {
                val path = Path()
                path.moveTo(pageLeft + meas.points[0].x * zoom, pageTop + meas.points[0].y * zoom)
                for (i in 1 until meas.points.size) {
                    path.lineTo(pageLeft + meas.points[i].x * zoom, pageTop + meas.points[i].y * zoom)
                }
                path.close()

                // Fill translucent hatch
                drawPath(path, color = color.copy(alpha = 0.2f), style = Fill)
                drawPath(path, color = color, style = Stroke(width = meas.strokeWidth * zoom))

                // Area text in centroid
                var area = 0f
                var cx = 0f
                var cy = 0f
                for (i in meas.points.indices) {
                    val j = (i + 1) % meas.points.size
                    area += meas.points[i].x * meas.points[j].y
                    area -= meas.points[j].x * meas.points[i].y
                    cx += meas.points[i].x
                    cy += meas.points[i].y
                }
                val pixelArea = abs(area) / 2.0f
                cx /= meas.points.size
                cy /= meas.points.size

                val areaText = meas.scale.formatArea(pixelArea)
                drawContext.canvas.nativeCanvas.apply {
                    val p = android.graphics.Paint().apply {
                        this.color = android.graphics.Color.WHITE
                        textSize = 11f * zoom
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        textAlign = android.graphics.Paint.Align.CENTER
                        isAntiAlias = true
                    }
                    drawText("${meas.label}: $areaText", pageLeft + cx * zoom, pageTop + cy * zoom, p)
                }
            }
        }
        else -> {}
    }
}
