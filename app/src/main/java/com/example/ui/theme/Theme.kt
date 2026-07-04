package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * The app ships dark-only. This composition local is kept for source
 * compatibility with screens that still read `LocalThemeIsDark.current`,
 * and is always `true`.
 */
val LocalThemeIsDark = staticCompositionLocalOf { true }

private val DarkColorScheme =
  darkColorScheme(
    primary = Palette.Purple,
    secondary = Palette.Teal,
    tertiary = Palette.Teal,
    background = Palette.BaseTop,
    surface = Color(0xFF161B22),
    onPrimary = Color.Black,
    onSecondary = Color.Black,
    onBackground = Palette.TextPrimary,
    onSurface = Palette.TextPrimary,
    onSurfaceVariant = Color(0xFF94A3B8)
  )

/** Retained for callers that still pass it as [MyApplicationTheme]'s darkTheme arg. App is dark-only. */
fun isAutoDarkTheme(): Boolean = true

@Composable
fun MyApplicationTheme(
  mood: String = "khush",
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  // Dark-only scheme; the accent follows the user's current mood.
  val colorScheme = DarkColorScheme.copy(primary = Palette.mood(mood))

  CompositionLocalProvider(LocalThemeIsDark provides true) {
    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
  }
}
