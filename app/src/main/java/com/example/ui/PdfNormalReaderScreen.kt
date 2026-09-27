package com.example.ui

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.model.DocumentEntity
import com.example.ui.theme.CamScannerTeal
import java.io.File

enum class ReaderThemeMode {
    LIGHT_WHITE,
    DARK_NIGHT,
    SEPIA_WARM
}

/**
 * Normal PDF Reader - Ultra fast, clean, distraction-free PDF viewer.
 * Stretches page naturally over the screen with zoom, pan, and extra white border framing.
 * Includes instant 1-tap jump to the All-in-One Studio Editor.
 */
@Composable
fun PdfNormalReaderScreen(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    onBack: () -> Unit,
    onOpenInStudio: (DocumentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activeDoc = uiState.activeDocument
    val activeTab = uiState.tabs.find { it.id == uiState.activeTabId }
    val currentPageIndex = activeTab?.activePageIndex ?: 0
    val totalPages = (activeDoc?.pageCount ?: 1).coerceAtLeast(1)

    val renderedBitmap by viewModel.renderedPageBitmap.collectAsStateWithLifecycle()

    // Touch zoom & pan state
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }
    var controlsVisible by remember { mutableStateOf(true) }

    // Display preferences: Screen Stretch & Extra White Border
    var isStretchToScreen by remember { mutableStateOf(true) }
    var hasExtraWhiteBorder by remember { mutableStateOf(true) }
    var readerTheme by remember { mutableStateOf(ReaderThemeMode.LIGHT_WHITE) }

    // Reset zoom when switching pages
    LaunchedEffect(currentPageIndex, activeDoc?.id) {
        zoomScale = 1f
        panOffset = Offset.Zero
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (zoomScale * zoomChange).coerceIn(1f, 5.0f)
        zoomScale = newScale
        if (newScale > 1f) {
            val maxPanX = (newScale - 1f) * 700f
            val maxPanY = (newScale - 1f) * 900f
            panOffset = Offset(
                x = (panOffset.x + offsetChange.x).coerceIn(-maxPanX, maxPanX),
                y = (panOffset.y + offsetChange.y).coerceIn(-maxPanY, maxPanY)
            )
        } else {
            panOffset = Offset.Zero
        }
    }

    val readerBgColor = when (readerTheme) {
        ReaderThemeMode.LIGHT_WHITE -> Color(0xFFF1F3F5)
        ReaderThemeMode.DARK_NIGHT -> Color(0xFF14171A)
        ReaderThemeMode.SEPIA_WARM -> Color(0xFFFBF0D9)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(readerBgColor)
            .testTag("pdf_normal_reader_screen")
    ) {
        // PDF Page Canvas Viewport - Stretches smoothly over screen
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (zoomScale > 1.1f) {
                                zoomScale = 1f
                                panOffset = Offset.Zero
                            } else {
                                zoomScale = 2.4f
                            }
                        },
                        onTap = {
                            controlsVisible = !controlsVisible
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            if (renderedBitmap != null && !renderedBitmap!!.isRecycled) {
                // PDF Page Container with Stretch and Extra Whiter Border
                val borderPadding = if (hasExtraWhiteBorder) 14.dp else 0.dp
                val pageShape = if (hasExtraWhiteBorder) RoundedCornerShape(6.dp) else RoundedCornerShape(0.dp)

                Box(
                    modifier = Modifier
                        .then(
                            if (isStretchToScreen) Modifier.fillMaxWidth() else Modifier.wrapContentSize()
                        )
                        .padding(horizontal = if (hasExtraWhiteBorder) 10.dp else 0.dp)
                        .graphicsLayer {
                            scaleX = zoomScale
                            scaleY = zoomScale
                            translationX = panOffset.x
                            translationY = panOffset.y
                        }
                        .then(
                            if (hasExtraWhiteBorder) {
                                Modifier
                                    .shadow(12.dp, pageShape)
                                    .background(Color.White, pageShape)
                                    .border(2.dp, Color.White, pageShape)
                                    .padding(borderPadding)
                            } else {
                                Modifier.shadow(4.dp)
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = renderedBitmap!!.asImageBitmap(),
                        contentDescription = "PDF Page ${currentPageIndex + 1}",
                        contentScale = if (isStretchToScreen) ContentScale.FillWidth else ContentScale.Fit,
                        modifier = if (isStretchToScreen) {
                            Modifier.fillMaxWidth()
                        } else {
                            Modifier.fillMaxHeight(0.85f).fillMaxWidth()
                        }
                    )
                }
            } else {
                // Clean fast loading indicator
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CircularProgressIndicator(
                        color = CamScannerTeal,
                        strokeWidth = 3.dp,
                        modifier = Modifier.size(38.dp)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Opening Page ${currentPageIndex + 1}...",
                        color = if (readerTheme == ReaderThemeMode.DARK_NIGHT) Color.White else Color(0xFF1E293B),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = activeDoc?.title ?: "Document",
                        color = if (readerTheme == ReaderThemeMode.DARK_NIGHT) Color.White.copy(alpha = 0.6f) else Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Top Header Bar (Auto-hiding / Floating)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding(),
                color = Color(0xEE111827),
                shadowElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back & Document Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to home",
                                tint = Color.White
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        Column {
                            Text(
                                text = activeDoc?.title ?: "PDF Reader",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = CamScannerTeal.copy(alpha = 0.25f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "NORMAL READER",
                                        color = Color(0xFF2DD4BF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Page ${currentPageIndex + 1} of $totalPages",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }

                    // Top Action Buttons
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Prominent "All-in-One Studio Editor" switch button
                        Button(
                            onClick = {
                                if (activeDoc != null) {
                                    onOpenInStudio(activeDoc)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "Studio Editor",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        // Share PDF button
                        IconButton(
                            onClick = {
                                if (activeDoc != null) {
                                    viewModel.exportAndShareDocument(activeDoc, context)
                                }
                            },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share PDF",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }
        }

        // Bottom Controls Bar (Page navigation, scrubber & view options)
        AnimatedVisibility(
            visible = controlsVisible,
            enter = fadeIn() + slideInVertically { it },
            exit = fadeOut() + slideOutVertically { it },
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                color = Color(0xEE111827),
                shape = RoundedCornerShape(22.dp),
                shadowElevation = 10.dp,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    // Secondary Toolbar: Stretch, White Border, Theme Toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Stretch to Screen Toggle
                        FilterChip(
                            selected = isStretchToScreen,
                            onClick = { isStretchToScreen = !isStretchToScreen },
                            label = { Text(if (isStretchToScreen) "Stretch On" else "Stretch Off", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.AspectRatio,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CamScannerTeal,
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(28.dp)
                        )

                        // Extra White Border Toggle
                        FilterChip(
                            selected = hasExtraWhiteBorder,
                            onClick = { hasExtraWhiteBorder = !hasExtraWhiteBorder },
                            label = { Text(if (hasExtraWhiteBorder) "White Border" else "Borderless", fontSize = 10.sp) },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.BorderOuter,
                                    contentDescription = null,
                                    modifier = Modifier.size(12.dp)
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0284C7),
                                selectedLabelColor = Color.White,
                                containerColor = Color.White.copy(alpha = 0.08f),
                                labelColor = Color.White.copy(alpha = 0.8f)
                            ),
                            border = null,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.height(28.dp)
                        )

                        // Reader Theme Switcher (Light / Dark / Sepia)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                                    .border(
                                        width = if (readerTheme == ReaderThemeMode.LIGHT_WHITE) 2.dp else 0.dp,
                                        color = CamScannerTeal,
                                        shape = CircleShape
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures { readerTheme = ReaderThemeMode.LIGHT_WHITE }
                                    }
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22262B))
                                    .border(
                                        width = if (readerTheme == ReaderThemeMode.DARK_NIGHT) 2.dp else 0.dp,
                                        color = CamScannerTeal,
                                        shape = CircleShape
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures { readerTheme = ReaderThemeMode.DARK_NIGHT }
                                    }
                            )
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFBF0D9))
                                    .border(
                                        width = if (readerTheme == ReaderThemeMode.SEPIA_WARM) 2.dp else 0.dp,
                                        color = CamScannerTeal,
                                        shape = CircleShape
                                    )
                                    .pointerInput(Unit) {
                                        detectTapGestures { readerTheme = ReaderThemeMode.SEPIA_WARM }
                                    }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Page scrubber slider (if multi-page)
                    if (totalPages > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "1",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                modifier = Modifier.width(18.dp)
                            )
                            Slider(
                                value = currentPageIndex.toFloat(),
                                onValueChange = { viewModel.setActivePage(it.toInt()) },
                                valueRange = 0f..(totalPages - 1).toFloat(),
                                steps = (totalPages - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = CamScannerTeal,
                                    activeTrackColor = CamScannerTeal,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(26.dp)
                            )
                            Text(
                                "$totalPages",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                textAlign = TextAlign.End,
                                modifier = Modifier.width(18.dp)
                            )
                        }
                    }

                    // Navigation Actions Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page
                        IconButton(
                            onClick = {
                                if (currentPageIndex > 0) {
                                    viewModel.setActivePage(currentPageIndex - 1)
                                }
                            },
                            enabled = currentPageIndex > 0,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous page",
                                tint = if (currentPageIndex > 0) Color.White else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Page Counter Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.padding(horizontal = 8.dp)
                        ) {
                            Text(
                                text = "Page ${currentPageIndex + 1} of $totalPages",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        // Next Page
                        IconButton(
                            onClick = {
                                if (currentPageIndex < totalPages - 1) {
                                    viewModel.setActivePage(currentPageIndex + 1)
                                }
                            },
                            enabled = currentPageIndex < totalPages - 1,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next page",
                                tint = if (currentPageIndex < totalPages - 1) Color.White else Color.White.copy(alpha = 0.25f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reset Zoom / Fit Screen
                        IconButton(
                            onClick = {
                                zoomScale = 1f
                                panOffset = Offset.Zero
                            },
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                Icons.Default.FitScreen,
                                contentDescription = "Fit to screen",
                                tint = if (zoomScale > 1.05f) CamScannerTeal else Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
