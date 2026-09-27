package com.example.ui

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
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
import com.example.model.DocumentEntity
import com.example.ui.theme.CamScannerTeal
import kotlinx.coroutines.launch

enum class ReaderThemeMode {
    LIGHT_WHITE,
    DARK_NIGHT,
    SEPIA_WARM,
    OLED_BLACK
}

enum class ReaderViewMode {
    CONTINUOUS_SCROLL, // Constant seamless vertical page scroll
    SINGLE_PAGE        // Classic slide / page-by-page swiper
}

/**
 * Super Smooth, Professional PDF Normal Viewer.
 * Features:
 * - Constant continuous vertical scrolling (like Adobe Acrobat / Google Drive)
 * - Single-page swipe mode
 * - Fluid pinch-to-zoom & double-tap zoom
 * - Async caching page loader (zero UI stutter)
 * - Quick thumbnails sheet & instant page scrubber
 * - Reading themes (Light, Sepia, Dark Night, OLED Black)
 * - 1-tap jump to the Studio Editor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfNormalReaderScreen(
    uiState: DocumentUiState,
    viewModel: DocumentViewModel,
    onBack: () -> Unit,
    onOpenInStudio: (DocumentEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val activeDoc = uiState.activeDocument
    val activeTab = uiState.tabs.find { it.id == uiState.activeTabId }
    val totalPages = (activeDoc?.pageCount ?: 1).coerceAtLeast(1)

    // View Modes & Preferences
    var viewMode by remember { mutableStateOf(ReaderViewMode.CONTINUOUS_SCROLL) }
    var readerTheme by remember { mutableStateOf(ReaderThemeMode.LIGHT_WHITE) }
    var hasExtraWhiteBorder by remember { mutableStateOf(true) }
    var controlsVisible by remember { mutableStateOf(true) }
    var showThumbnailsSheet by remember { mutableStateOf(false) }

    // Touch Zoom & Pan State (Viewport level)
    var zoomScale by remember { mutableFloatStateOf(1f) }
    var panOffset by remember { mutableStateOf(Offset.Zero) }

    // LazyColumn scroll state for continuous mode
    val listState = rememberLazyListState()

    // Single page mode state
    var singlePageIndex by remember { mutableIntStateOf(activeTab?.activePageIndex ?: 0) }

    // Derived current page index in continuous scroll
    val visiblePageIndex by remember {
        derivedStateOf {
            if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) {
                listState.firstVisibleItemIndex.coerceIn(0, totalPages - 1)
            } else {
                singlePageIndex.coerceIn(0, totalPages - 1)
            }
        }
    }

    // Double tap zoom reset
    fun resetZoom() {
        zoomScale = 1f
        panOffset = Offset.Zero
    }

    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        val newScale = (zoomScale * zoomChange).coerceIn(1f, 4.5f)
        zoomScale = newScale
        if (newScale > 1f) {
            val maxPanX = (newScale - 1f) * 600f
            val maxPanY = (newScale - 1f) * 800f
            panOffset = Offset(
                x = (panOffset.x + offsetChange.x).coerceIn(-maxPanX, maxPanX),
                y = (panOffset.y + offsetChange.y).coerceIn(-maxPanY, maxPanY)
            )
        } else {
            panOffset = Offset.Zero
        }
    }

    val readerBgColor = when (readerTheme) {
        ReaderThemeMode.LIGHT_WHITE -> Color(0xFFF1F5F9)
        ReaderThemeMode.DARK_NIGHT -> Color(0xFF13171F)
        ReaderThemeMode.SEPIA_WARM -> Color(0xFFF8EFE1)
        ReaderThemeMode.OLED_BLACK -> Color(0xFF000000)
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(readerBgColor)
            .testTag("pdf_normal_reader_screen")
    ) {
        // Main Viewport Container with Pinch & Pan
        Box(
            modifier = Modifier
                .fillMaxSize()
                .transformable(state = transformState)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            if (zoomScale > 1.1f) {
                                resetZoom()
                            } else {
                                zoomScale = 2.2f
                            }
                        },
                        onTap = {
                            controlsVisible = !controlsVisible
                        }
                    )
                }
                .graphicsLayer {
                    scaleX = zoomScale
                    scaleY = zoomScale
                    translationX = panOffset.x
                    translationY = panOffset.y
                }
        ) {
            if (activeDoc != null) {
                when (viewMode) {
                    // MODE 1: Continuous Constant Vertical Page Stream
                    ReaderViewMode.CONTINUOUS_SCROLL -> {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("continuous_pdf_list"),
                            contentPadding = PaddingValues(
                                top = if (controlsVisible) 76.dp else 16.dp,
                                bottom = if (controlsVisible) 130.dp else 24.dp,
                                start = if (hasExtraWhiteBorder) 14.dp else 4.dp,
                                end = if (hasExtraWhiteBorder) 14.dp else 4.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            items(totalPages, key = { "page_$it" }) { pageIdx ->
                                ContinuousPdfPageCard(
                                    filePath = activeDoc.filePath,
                                    pageIndex = pageIdx,
                                    totalPages = totalPages,
                                    hasWhiteBorder = hasExtraWhiteBorder,
                                    readerTheme = readerTheme,
                                    viewModel = viewModel
                                )
                            }
                        }
                    }

                    // MODE 2: Single Page Swiper View
                    ReaderViewMode.SINGLE_PAGE -> {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(
                                    top = if (controlsVisible) 76.dp else 16.dp,
                                    bottom = if (controlsVisible) 130.dp else 24.dp,
                                    start = if (hasExtraWhiteBorder) 14.dp else 4.dp,
                                    end = if (hasExtraWhiteBorder) 14.dp else 4.dp
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            ContinuousPdfPageCard(
                                filePath = activeDoc.filePath,
                                pageIndex = singlePageIndex,
                                totalPages = totalPages,
                                hasWhiteBorder = hasExtraWhiteBorder,
                                readerTheme = readerTheme,
                                viewModel = viewModel
                            )
                        }
                    }
                }
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CamScannerTeal)
                }
            }
        }

        // Floating Fast Page Pill Indicator (Appears when scrolling)
        AnimatedVisibility(
            visible = true,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .statusBarsPadding()
                .padding(top = if (controlsVisible) 70.dp else 16.dp, end = 16.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Black.copy(alpha = 0.65f),
                shadowElevation = 4.dp,
                modifier = Modifier.clip(CircleShape)
            ) {
                Text(
                    text = "${visiblePageIndex + 1} / $totalPages",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        // -------------------------------------------------------------
        // TOP CONTROLS BAR (Auto-hiding / Floating Glassmorphic Header)
        // -------------------------------------------------------------
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
                color = Color(0xF20F172A),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Back Button & Document Info
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("reader_back_button")
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
                                        text = if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) "CONTINUOUS FLOW" else "SINGLE PAGE",
                                        color = Color(0xFF2DD4BF),
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Page ${visiblePageIndex + 1} of $totalPages",
                                    color = Color.White.copy(alpha = 0.75f),
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
                        // Thumbnails Grid Button
                        IconButton(
                            onClick = { showThumbnailsSheet = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.GridView,
                                contentDescription = "Page thumbnails",
                                tint = Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.size(19.dp)
                            )
                        }

                        // Prominent "Studio Editor" switch button
                        Button(
                            onClick = {
                                if (activeDoc != null) {
                                    onOpenInStudio(activeDoc)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CamScannerTeal),
                            shape = RoundedCornerShape(18.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(34.dp)
                                .testTag("reader_studio_editor_button")
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
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Share,
                                contentDescription = "Share PDF",
                                tint = Color.White,
                                modifier = Modifier.size(19.dp)
                            )
                        }
                    }
                }
            }
        }

        // -------------------------------------------------------------
        // BOTTOM FLOATING TOOLBAR (View Modes, Scrubber, Theme Toggles)
        // -------------------------------------------------------------
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
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                color = Color(0xF20F172A),
                shape = RoundedCornerShape(24.dp),
                shadowElevation = 12.dp,
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Secondary Toolbar Row: View Mode, Border Margin, Themes
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // View Mode Switcher (Continuous Vertical vs Single Page)
                        FilterChip(
                            selected = viewMode == ReaderViewMode.CONTINUOUS_SCROLL,
                            onClick = {
                                viewMode = if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) {
                                    ReaderViewMode.SINGLE_PAGE
                                } else {
                                    ReaderViewMode.CONTINUOUS_SCROLL
                                }
                            },
                            label = {
                                Text(
                                    if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) "Continuous Flow" else "Single Page",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) Icons.Default.VerticalDistribute else Icons.Default.ViewCarousel,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
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

                        // White Border Frame Toggle
                        FilterChip(
                            selected = hasExtraWhiteBorder,
                            onClick = { hasExtraWhiteBorder = !hasExtraWhiteBorder },
                            label = {
                                Text(
                                    if (hasExtraWhiteBorder) "White Border" else "Borderless",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Default.BorderOuter,
                                    contentDescription = null,
                                    modifier = Modifier.size(13.dp)
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

                        // Reading Theme Bubbles (Light, Sepia, Night, OLED)
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            ThemeColorDot(
                                color = Color(0xFFF1F5F9),
                                isSelected = readerTheme == ReaderThemeMode.LIGHT_WHITE,
                                onClick = { readerTheme = ReaderThemeMode.LIGHT_WHITE }
                            )
                            ThemeColorDot(
                                color = Color(0xFFF8EFE1),
                                isSelected = readerTheme == ReaderThemeMode.SEPIA_WARM,
                                onClick = { readerTheme = ReaderThemeMode.SEPIA_WARM }
                            )
                            ThemeColorDot(
                                color = Color(0xFF1E293B),
                                isSelected = readerTheme == ReaderThemeMode.DARK_NIGHT,
                                onClick = { readerTheme = ReaderThemeMode.DARK_NIGHT }
                            )
                            ThemeColorDot(
                                color = Color(0xFF000000),
                                isSelected = readerTheme == ReaderThemeMode.OLED_BLACK,
                                onClick = { readerTheme = ReaderThemeMode.OLED_BLACK }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Multi-page Scrubber Slider
                    if (totalPages > 1) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                "1",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 10.sp,
                                modifier = Modifier.width(16.dp)
                            )
                            Slider(
                                value = visiblePageIndex.toFloat(),
                                onValueChange = { targetPage ->
                                    val page = targetPage.toInt().coerceIn(0, totalPages - 1)
                                    singlePageIndex = page
                                    if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) {
                                        coroutineScope.launch {
                                            listState.scrollToItem(page)
                                        }
                                    }
                                },
                                valueRange = 0f..(totalPages - 1).toFloat(),
                                steps = (totalPages - 2).coerceAtLeast(0),
                                colors = SliderDefaults.colors(
                                    thumbColor = CamScannerTeal,
                                    activeTrackColor = CamScannerTeal,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(24.dp)
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

                    // Navigation Actions Row (Previous, Counter, Next, Fit)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Page
                        IconButton(
                            onClick = {
                                val prev = (visiblePageIndex - 1).coerceAtLeast(0)
                                singlePageIndex = prev
                                if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) {
                                    coroutineScope.launch { listState.animateScrollToItem(prev) }
                                }
                            },
                            enabled = visiblePageIndex > 0,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Previous page",
                                tint = if (visiblePageIndex > 0) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Page Counter Badge
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White.copy(alpha = 0.12f),
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(
                                text = "Page ${visiblePageIndex + 1} of $totalPages",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }

                        // Next Page
                        IconButton(
                            onClick = {
                                val next = (visiblePageIndex + 1).coerceAtMost(totalPages - 1)
                                singlePageIndex = next
                                if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) {
                                    coroutineScope.launch { listState.animateScrollToItem(next) }
                                }
                            },
                            enabled = visiblePageIndex < totalPages - 1,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Next page",
                                tint = if (visiblePageIndex < totalPages - 1) Color.White else Color.White.copy(alpha = 0.2f),
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Reset Zoom / Fit Screen
                        IconButton(
                            onClick = { resetZoom() },
                            modifier = Modifier.size(32.dp)
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

        // -------------------------------------------------------------
        // QUICK THUMBNAILS BOTTOM SHEET
        // -------------------------------------------------------------
        if (showThumbnailsSheet && activeDoc != null) {
            ModalBottomSheet(
                onDismissRequest = { showThumbnailsSheet = false },
                containerColor = Color(0xFF0F172A),
                scrimColor = Color.Black.copy(alpha = 0.6f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Document Pages ($totalPages)",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showThumbnailsSheet = false }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(totalPages) { pIdx ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(105.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .border(
                                        width = if (pIdx == visiblePageIndex) 2.5.dp else 1.dp,
                                        color = if (pIdx == visiblePageIndex) CamScannerTeal else Color.White.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable {
                                        singlePageIndex = pIdx
                                        if (viewMode == ReaderViewMode.CONTINUOUS_SCROLL) {
                                            coroutineScope.launch { listState.scrollToItem(pIdx) }
                                        }
                                        showThumbnailsSheet = false
                                    }
                            ) {
                                ContinuousPdfPageCard(
                                    filePath = activeDoc.filePath,
                                    pageIndex = pIdx,
                                    totalPages = totalPages,
                                    hasWhiteBorder = false,
                                    readerTheme = readerTheme,
                                    viewModel = viewModel,
                                    isThumbnail = true
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.75f))
                                        .padding(vertical = 2.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Page ${pIdx + 1}",
                                        color = Color.White,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
    }
}

/**
 * Individual PDF Page Composable that renders asynchronously with zero UI jank
 */
@Composable
private fun ContinuousPdfPageCard(
    filePath: String,
    pageIndex: Int,
    totalPages: Int,
    hasWhiteBorder: Boolean,
    readerTheme: ReaderThemeMode,
    viewModel: DocumentViewModel,
    isThumbnail: Boolean = false
) {
    var pageBitmap by remember(filePath, pageIndex) { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember(filePath, pageIndex) { mutableStateOf(true) }

    LaunchedEffect(filePath, pageIndex) {
        isLoading = true
        val bmp = viewModel.getPageBitmapSuspend(
            filePath = filePath,
            pageIndex = pageIndex,
            targetWidth = if (isThumbnail) 320 else 1080
        )
        pageBitmap = bmp
        isLoading = false
    }

    val pageShape = if (hasWhiteBorder) RoundedCornerShape(8.dp) else RoundedCornerShape(2.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (hasWhiteBorder && !isThumbnail) {
                    Modifier
                        .shadow(8.dp, pageShape)
                        .background(Color.White, pageShape)
                        .border(1.dp, Color.White, pageShape)
                        .padding(12.dp)
                } else if (!isThumbnail) {
                    Modifier
                        .shadow(4.dp, pageShape)
                        .background(Color.White, pageShape)
                } else {
                    Modifier.background(Color.White)
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (pageBitmap != null && !pageBitmap!!.isRecycled) {
            Image(
                bitmap = pageBitmap!!.asImageBitmap(),
                contentDescription = "PDF Page ${pageIndex + 1}",
                contentScale = if (isThumbnail) ContentScale.Crop else ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            // Elegant placeholder / loading shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isThumbnail) 140.dp else 450.dp)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(
                            color = CamScannerTeal,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(28.dp)
                        )
                        if (!isThumbnail) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Loading page ${pageIndex + 1}...",
                                color = Color(0xFF64748B),
                                fontSize = 12.sp
                            )
                        }
                    }
                } else {
                    Text(
                        text = "Page ${pageIndex + 1}",
                        color = Color(0xFF94A3B8),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeColorDot(
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(22.dp)
            .clip(CircleShape)
            .background(color)
            .border(
                width = if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) CamScannerTeal else Color.White.copy(alpha = 0.4f),
                shape = CircleShape
            )
            .clickable { onClick() }
    )
}
