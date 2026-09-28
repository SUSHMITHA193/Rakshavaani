package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TacticalColorScheme = darkColorScheme(
  primary = TacticalOlive,
  onPrimary = TacticalTextPrimary,
  primaryContainer = TacticalNavyCardLight,
  onPrimaryContainer = TacticalTextPrimary,
  secondary = TacticalAmber,
  onSecondary = TacticalNavyDark,
  secondaryContainer = TacticalBorder,
  onSecondaryContainer = TacticalTextPrimary,
  tertiary = TacticalGreen,
  onTertiary = TacticalNavyDark,
  background = TacticalNavyBg,
  onBackground = TacticalTextPrimary,
  surface = TacticalNavyCard,
  onSurface = TacticalTextPrimary,
  surfaceVariant = TacticalNavyCardLight,
  onSurfaceVariant = TacticalTextSecondary,
  outline = TacticalBorder,
  outlineVariant = TacticalBorderLight,
  error = TacticalRed,
  onError = TacticalTextPrimary
)

@Composable
fun MyApplicationTheme(
  darkTheme: Boolean = true,
  dynamicColor: Boolean = false,
  content: @Composable () -> Unit,
) {
  MaterialTheme(
    colorScheme = TacticalColorScheme,
    typography = Typography,
    content = content
  )
}

