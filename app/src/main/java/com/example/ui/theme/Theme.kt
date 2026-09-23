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

private val TacticalColorScheme =
  darkColorScheme(
    primary = TacticalCyan,
    onPrimary = TacticalBlackBg,
    primaryContainer = TacticalSlateSurfaceVariant,
    onPrimaryContainer = TacticalCyan,
    secondary = TacticalGreenNeon,
    onSecondary = TacticalBlackBg,
    secondaryContainer = Color(0xFF052E16),
    onSecondaryContainer = Color(0xFF86EFAC),
    surface = TacticalSlateSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = TacticalSlateSurfaceVariant,
    onSurfaceVariant = Color(0xFF94A3B8),
    background = TacticalBlackBg,
    onBackground = Color(0xFFF8FAFC),
    outline = TacticalBorder,
    outlineVariant = Color(0xFF1E293B)
  )

private val DarkColorScheme =
  darkColorScheme(
    primary = ExcelGreenLight,
    onPrimary = Color.Black,
    primaryContainer = ExcelContainerDark,
    onPrimaryContainer = Color(0xFFD1E7DD),
    secondary = ForestSecondary,
    surface = EmeraldSurfaceDark,
    background = EmeraldSurfaceDark
  )

private val LightColorScheme =
  lightColorScheme(
    primary = ExcelGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = ExcelContainer,
    onPrimaryContainer = ExcelGreenDark,
    secondary = ForestSecondary,
    secondaryContainer = ForestSecondaryContainer,
    surface = EmeraldSurfaceLight,
    background = EmeraldSurfaceLight
  )

@Composable
fun MyApplicationTheme(
  isTactical: Boolean = false,
  darkTheme: Boolean = isSystemInDarkTheme(),
  // Dynamic color is available on Android 12+
  dynamicColor: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme =
    when {
      isTactical -> TacticalColorScheme
      dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
        val context = LocalContext.current
        if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
      }

      darkTheme -> DarkColorScheme
      else -> LightColorScheme
    }

  MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
