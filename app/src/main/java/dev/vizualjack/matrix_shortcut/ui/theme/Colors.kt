package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource

val TEXT_COLOR = Color(241,245,249)

val colorScheme = darkColorScheme(
    background = Color(16, 24, 34),
    primaryContainer = Color(30,41,59, (0.3*255).toInt()),
    onPrimaryContainer = TEXT_COLOR,
    primary = Color(30,41,59),
    onPrimary = Color(100,116,139),
    tertiary = Color(0,0x7F,0),
    secondaryContainer = Color(19,109,236,(0.2*255).toInt()),
    onSecondaryContainer = Color(19,109,236),
    onSecondary = Color(148, 163, 184),
    error = Color(0xEF, 0x13, 0x13, 0xFF),
    errorContainer = Color(0x81, 0x1A, 0x1A, 0xFF),
    onSurfaceVariant = TEXT_COLOR,
    surfaceVariant = Color(30,41,59, (0.5*255).toInt()),
    onSurface = TEXT_COLOR,
    surface = Color(30,41,59),
    surfaceTint = Color(19,109,236),
    onBackground = TEXT_COLOR,
)

val textSelectionColors = TextSelectionColors(
    handleColor = colorScheme.onSecondary,
    backgroundColor = colorScheme.onPrimary,
)