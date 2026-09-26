package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
  primary = CamScannerTealLight,
  onPrimary = DocNavyDark,
  primaryContainer = CamScannerTealDark,
  onPrimaryContainer = Color(0xFF99F6E4),
  secondary = DocSecondaryIndigo,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF1E293B),
  onSecondaryContainer = Color(0xFFE0E7FF),
  tertiary = CamScannerGold,
  background = DocNavyDark,
  onBackground = DocTextPrimaryDark,
  surface = DocSurfaceDark,
  onSurface = DocTextPrimaryDark,
  surfaceVariant = DocSurfaceCardDark,
  onSurfaceVariant = DocTextSecondaryDark,
  outline = DocBorderDark
)

private val LightColorScheme = lightColorScheme(
  primary = CamScannerTeal,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFCCFBF1),
  onPrimaryContainer = Color(0xFF0F766E),
  secondary = DocSecondaryIndigo,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF1F5F9),
  onSecondaryContainer = Color(0xFF334155),
  tertiary = CamScannerGold,
  background = DocBgLight,
  onBackground = DocTextPrimaryLight,
  surface = DocSurfaceLight,
  onSurface = DocTextPrimaryLight,
  surfaceVariant = DocSurfaceCardLight,
  onSurfaceVariant = DocTextSecondaryLight,
  outline = DocBorderLight
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val colorScheme = when {
    dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
      val context = LocalContext.current
      if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    }
    darkTheme -> DarkColorScheme
    else -> LightColorScheme
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

