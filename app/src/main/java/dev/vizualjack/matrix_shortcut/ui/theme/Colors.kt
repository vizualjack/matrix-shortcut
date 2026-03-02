package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

val TEXT_COLOR = Color(241,245,249)

val colorScheme = darkColorScheme(
    background = Color(16, 24, 34),
    primaryContainer = Color(30,41,59, (0.3*255).toInt()),
    onPrimaryContainer = TEXT_COLOR,
    primary = Color(30,41,59),   // BUTTON COLOR
    onPrimary = Color(100,116,139),  // BUTTON TEXT COLOR
    tertiary = Color(0,0x7F,0),
    secondaryContainer = Color(19,109,236,(0.2*255).toInt()),
    onSecondaryContainer = Color(19,109,236),
    onSecondary = Color(148, 163, 184),
    error = Color(0x9F,0x1F,0x1F),
    errorContainer = Color(0x81, 0x1A, 0x1A, 0xFF),

//    secondary = Color(0xFF,0,0),
//    tertiaryContainer = Color(0xFF,0,0),
//    onTertiaryContainer = Color(0xFF,0,0),
//    onTertiary = Color(0xFF,0,0),
//    onErrorContainer = Color(0xFF,0,0),
//    inverseOnSurface = Color(0xFF,0,0),
//    inversePrimary = Color(0xFF,0,0),
//    onSurfaceVariant = Color(0xFF,0,0),
//    onError = Color(0xFF,0,0),

    onSurfaceVariant = TEXT_COLOR,
    surfaceVariant = Color(30,41,59, (0.5*255).toInt()),
//    outline = Color(0xFF,0,0),
    onSurface = TEXT_COLOR,
    surface = Color(30,41,59),
    surfaceTint = Color(19,109,236),
//    inverseSurface = Color(0xFF,0,0),

//    errorContainer = Color(0xFF,0,0),
//    outlineVariant = Color(0xFF,0,0),
//    scrim = Color(0xFF,0,0),
    onBackground = TEXT_COLOR,
)