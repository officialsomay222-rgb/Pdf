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
  primary = DocPrimaryCyanLight,
  onPrimary = DocNavyDark,
  primaryContainer = Color(0xFF0C4A6E),
  onPrimaryContainer = Color(0xFFBAE6FD),
  secondary = DocSecondaryIndigo,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFF312E81),
  onSecondaryContainer = Color(0xFFE0E7FF),
  tertiary = DocAccentAmber,
  background = DocNavyDark,
  onBackground = DocTextPrimaryDark,
  surface = DocSurfaceDark,
  onSurface = DocTextPrimaryDark,
  surfaceVariant = DocSurfaceCardDark,
  onSurfaceVariant = DocTextSecondaryDark,
  outline = DocBorderDark
)

private val LightColorScheme = lightColorScheme(
  primary = DocPrimaryCyan,
  onPrimary = Color.White,
  primaryContainer = Color(0xFFE0F2FE),
  onPrimaryContainer = Color(0xFF0369A1),
  secondary = DocSecondaryIndigo,
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFEEF2FF),
  onSecondaryContainer = Color(0xFF3730A3),
  tertiary = DocAccentAmber,
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
  darkTheme: Boolean = true, // Default to sleek high-tech engineering dark theme
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
