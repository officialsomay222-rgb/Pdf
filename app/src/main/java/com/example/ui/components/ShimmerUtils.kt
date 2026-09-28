package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.ui.theme.CamScannerTeal

/**
 * Creates a smooth, 60fps sweeping shimmer gradient brush for content-aware loading skeletons.
 */
@Composable
fun rememberShimmerBrush(showShimmer: Boolean = true): Brush {
    if (!showShimmer) {
        return Brush.linearGradient(
            colors = listOf(Color.Transparent, Color.Transparent)
        )
    }

    val isDark = isSystemInDarkTheme()
    val shimmerColors = if (isDark) {
        listOf(
            Color(0xFF1E293B),
            Color(0xFF334155),
            Color(0xFF1E293B)
        )
    } else {
        listOf(
            Color(0xFFE2E8F0),
            Color(0xFFF1F5F9),
            Color(0xFFE2E8F0)
        )
    }

    val transition = rememberInfiniteTransition(label = "shimmerTransition")
    val translateAnim by transition.animateFloat(
        initialValue = -400f,
        targetValue = 1400f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    return Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, translateAnim - 300f),
        end = Offset(translateAnim, translateAnim)
    )
}

/**
 * Content-aware PDF Page Skeleton simulating realistic text lines, architectural boxes,
 * and signature zones during page rendering or initialization.
 */
@Composable
fun DocumentPageSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 440.dp
) {
    val brush = rememberShimmerBrush()
    val isDark = isSystemInDarkTheme()
    val paperBg = if (isDark) Color(0xFF131B2E) else Color(0xFFFFFFFF)
    val borderColor = if (isDark) Color(0xFF1E293B) else Color(0xFFE2E8F0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(containerColor = paperBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header Bar Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Body Paragraph 1 (Staggered realistic line widths)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.68f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Technical Schematic / Diagram / Table Box Skeleton
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
                    .border(0.5.dp, CamScannerTeal.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Body Paragraph 2
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.88f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.52f)
                    .height(11.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Footer Signature & Verification Seal Skeleton
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Box(
                        modifier = Modifier
                            .width(120.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(8.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(brush)
                    )
                }
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(brush)
                )
            }
        }
    }
}

/**
 * Skeleton placeholder for Document List cards during library indexing or cold launches.
 */
@Composable
fun DocumentCardSkeleton(modifier: Modifier = Modifier) {
    val brush = rememberShimmerBrush()
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E293B) else Color.White
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Document thumbnail skeleton
            Box(
                modifier = Modifier
                    .size(width = 44.dp, height = 56.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(brush)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.7f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(brush)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(
                        modifier = Modifier
                            .width(55.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(brush)
                    )
                    Box(
                        modifier = Modifier
                            .width(40.dp)
                            .height(10.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(brush)
                    )
                }
            }

            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
        }
    }
}

/**
 * Content-aware loading skeleton for PDF Tool components (Compressor, Merger, Splitter, Signer, Word Opener)
 * during initialization and calculation of document properties.
 */
@Composable
fun PdfToolComponentSkeleton(
    modifier: Modifier = Modifier,
    height: Dp = 140.dp
) {
    val brush = rememberShimmerBrush()
    val isDark = isSystemInDarkTheme()
    val cardBg = if (isDark) Color(0xFF1E293B) else Color(0xFFF8FAFC)
    val borderColor = if (isDark) Color(0xFF334155) else Color(0xFFE2E8F0)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.55f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
                Box(
                    modifier = Modifier
                        .width(48.dp)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(brush)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(10.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(brush)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(6.dp)).background(brush))
                Box(modifier = Modifier.weight(1f).height(28.dp).clip(RoundedCornerShape(6.dp)).background(brush))
            }
        }
    }
}
