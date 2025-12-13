package io.github.hziqzmin.wildguardai.ui.theme

import androidx.compose.ui.graphics.Color

// --- YOUR 4 CORE COLORS ---
val DarkGreen = Color(0xFF2D3A3A)     // For Text and UI elements
val Cream = Color(0xFFF5F5F0)            // For Backgrounds
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)
val AccentTurquoise = Color(0xFF26A69A)  // Your selected teal accent

// --- MATERIAL 3 LIGHT THEME MAPPING ---
// We map your colors to the required Material 3 roles for a light theme.

// Primary = Main interactive color (buttons, selected tabs)
val md_theme_light_primary = AccentTurquoise // <-- NOW USES YOUR NEW ACCENT
// OnPrimary = Text/icons on top of the primary color
val md_theme_light_onPrimary = White // <-- Text on buttons should be White for contrast

// Background = The main screen background color
val md_theme_light_background = Cream
// OnBackground = The main text color on the background
val md_theme_light_onBackground = DarkGreen

// Surface = Color of components like Cards, TextFields, TopAppBar
val md_theme_light_surface = Cream
// OnSurface = Text/icons on top of surface components
val md_theme_light_onSurface = DarkGreen

// --- We fill the rest with your colors to ensure a complete, minimal theme ---

val md_theme_light_primaryContainer = AccentTurquoise
val md_theme_light_onPrimaryContainer = White
val md_theme_light_secondary = DarkGreen
val md_theme_light_onSecondary = Cream
val md_theme_light_secondaryContainer = Cream
val md_theme_light_onSecondaryContainer = DarkGreen
val md_theme_light_tertiary = DarkGreen
val md_theme_light_onTertiary = Cream
val md_theme_light_tertiaryContainer = Cream
val md_theme_light_onTertiaryContainer = DarkGreen
val md_theme_light_error = Color(0xFFB00020) // It's good to keep a standard red for errors
val md_theme_light_onError = White
val md_theme_light_errorContainer = Color(0xFFFCD8DF)
val md_theme_light_onErrorContainer = Black
val md_theme_light_surfaceVariant = Cream
val md_theme_light_onSurfaceVariant = DarkGreen
val md_theme_light_outline = DarkGreen
val md_theme_light_inverseOnSurface = Cream
val md_theme_light_inverseSurface = DarkGreen
val md_theme_light_inversePrimary = Cream
val md_theme_light_surfaceTint = DarkGreen
val md_theme_light_outlineVariant = Cream
val md_theme_light_scrim = Black