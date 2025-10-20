package io.github.hziqzmin.wildguardai.ui.theme

import androidx.compose.ui.graphics.Color

// --- YOUR PROVIDED COLORS ---
val DarkGreen = Color(0xFF2D3A3A)     // Your dark background color
val Cream = Color(0xFFF5F5F0)            // Your off-white text color
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

// --- MATERIAL 3 SEMANTIC COLORS (Dark Theme) ---
// These names are used by MaterialTheme in Theme.kt to automatically style your components.
// We are using the 'Cream' color as the primary accent color for this minimalist theme.

val md_theme_dark_primary = Cream                 // Main accent color for interactive elements
val md_theme_dark_onPrimary = DarkGreen           // Color for text/icons on top of the primary color

val md_theme_dark_background = Cream          // Main screen background color
val md_theme_dark_onBackground = DarkGreen            // Main text color on the background

val md_theme_dark_surface = DarkGreen             // Color for surfaces like cards, dialogs, etc.
val md_theme_dark_onSurface = Cream               // Text color on top of surfaces

// --- Other Material 3 colors, using your palette for a consistent look ---
val md_theme_dark_primaryContainer = Cream
val md_theme_dark_onPrimaryContainer = DarkGreen
val md_theme_dark_secondary = Cream
val md_theme_dark_onSecondary = DarkGreen
val md_theme_dark_secondaryContainer = DarkGreen
val md_theme_dark_onSecondaryContainer = Cream
val md_theme_dark_tertiary = Cream
val md_theme_dark_onTertiary = DarkGreen
val md_theme_dark_tertiaryContainer = DarkGreen
val md_theme_dark_onTertiaryContainer = Cream
val md_theme_dark_error = White                   // Using high-contrast White for errors
val md_theme_dark_errorContainer = DarkGreen
val md_theme_dark_onError = DarkGreen
val md_theme_dark_onErrorContainer = White
val md_theme_dark_surfaceVariant = DarkGreen
val md_theme_dark_onSurfaceVariant = Cream
val md_theme_dark_outline = Cream                 // Outlines will use the light text color
val md_theme_dark_inverseOnSurface = DarkGreen
val md_theme_dark_inverseSurface = Cream
val md_theme_dark_inversePrimary = DarkGreen
val md_theme_dark_surfaceTint = Cream
val md_theme_dark_outlineVariant = DarkGreen
val md_theme_dark_scrim = Black

