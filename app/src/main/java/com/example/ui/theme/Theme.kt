package com.example.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

import com.example.model.AppThemeMode

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

private val OledColorScheme = darkColorScheme(
  primary = Color(0xFF14B8A6),
  onPrimary = Color.Black,
  primaryContainer = Color(0xFF0F766E),
  onPrimaryContainer = Color(0xFF99F6E4),
  secondary = Color(0xFF38BDF8),
  onSecondary = Color.Black,
  secondaryContainer = Color(0xFF111827),
  onSecondaryContainer = Color(0xFFE0F2FE),
  tertiary = Color(0xFFFACC15),
  background = Color(0xFF000000),
  onBackground = Color(0xFFF8FAFC),
  surface = Color(0xFF0A0A0A),
  onSurface = Color(0xFFF8FAFC),
  surfaceVariant = Color(0xFF121212),
  onSurfaceVariant = Color(0xFF94A3B8),
  outline = Color(0xFF262626)
)

private val WarmSepiaColorScheme = lightColorScheme(
  primary = Color(0xFFD97706),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFEF3C7),
  onPrimaryContainer = Color(0xFF92400E),
  secondary = Color(0xFF8B5A2B),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFF7EFE2),
  onSecondaryContainer = Color(0xFF451A03),
  tertiary = Color(0xFF059669),
  background = Color(0xFFFAF5ED),
  onBackground = Color(0xFF292524),
  surface = Color(0xFFFFFDF8),
  onSurface = Color(0xFF292524),
  surfaceVariant = Color(0xFFF5EBDC),
  onSurfaceVariant = Color(0xFF78716C),
  outline = Color(0xFFE7D8C5)
)

private val NordicFrostColorScheme = darkColorScheme(
  primary = Color(0xFF88C0D0),
  onPrimary = Color(0xFF2E3440),
  primaryContainer = Color(0xFF4C566A),
  onPrimaryContainer = Color(0xFFECEFF4),
  secondary = Color(0xFF81A1C1),
  onSecondary = Color(0xFF2E3440),
  secondaryContainer = Color(0xFF3B4252),
  onSecondaryContainer = Color(0xFFE5E9F0),
  tertiary = Color(0xFFA3BE8C),
  background = Color(0xFF2E3440),
  onBackground = Color(0xFFECEFF4),
  surface = Color(0xFF3B4252),
  onSurface = Color(0xFFECEFF4),
  surfaceVariant = Color(0xFF434C5E),
  onSurfaceVariant = Color(0xFFD8DEE9),
  outline = Color(0xFF4C566A)
)

private val SunsetAmberColorScheme = lightColorScheme(
  primary = Color(0xFFEA580C),
  onPrimary = Color.White,
  primaryContainer = Color(0xFFFFEDD5),
  onPrimaryContainer = Color(0xFF9A3412),
  secondary = Color(0xFFD97706),
  onSecondary = Color.White,
  secondaryContainer = Color(0xFFFFF7ED),
  onSecondaryContainer = Color(0xFF7C2D12),
  tertiary = Color(0xFF10B981),
  background = Color(0xFFFFFBF5),
  onBackground = Color(0xFF1C1917),
  surface = Color(0xFFFFFFFF),
  onSurface = Color(0xFF1C1917),
  surfaceVariant = Color(0xFFFFEDD5),
  onSurfaceVariant = Color(0xFF78716C),
  outline = Color(0xFFFED7AA)
)

@Composable
fun MyApplicationTheme(
  themeMode: AppThemeMode = AppThemeMode.SYSTEM,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  val isSystemDark = isSystemInDarkTheme()
  val isDark = when (themeMode) {
    AppThemeMode.SYSTEM -> isSystemDark
    AppThemeMode.DARK, AppThemeMode.OLED_BLACK, AppThemeMode.NORDIC_FROST -> true
    AppThemeMode.LIGHT, AppThemeMode.WARM_SEPIA, AppThemeMode.SUNSET_AMBER -> false
  }

  val colorScheme = when (themeMode) {
    AppThemeMode.SYSTEM -> {
      if (dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val context = LocalContext.current
        if (isSystemDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      } else {
        if (isSystemDark) DarkColorScheme else LightColorScheme
      }
    }
    AppThemeMode.LIGHT -> LightColorScheme
    AppThemeMode.DARK -> DarkColorScheme
    AppThemeMode.OLED_BLACK -> OledColorScheme
    AppThemeMode.WARM_SEPIA -> WarmSepiaColorScheme
    AppThemeMode.NORDIC_FROST -> NordicFrostColorScheme
    AppThemeMode.SUNSET_AMBER -> SunsetAmberColorScheme
  }

  val view = LocalView.current
  if (!view.isInEditMode) {
    SideEffect {
      val window = (view.context as? Activity)?.window
      if (window != null) {
        val insetsController = WindowCompat.getInsetsController(window, view)
        insetsController.isAppearanceLightStatusBars = !isDark
        insetsController.isAppearanceLightNavigationBars = !isDark
      }
    }
  }

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}

