package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = gray,
    primaryContainer = gray,
    onPrimaryContainer = gray,
    secondary = gray,
    onSecondary = gray,
    secondaryContainer = gray,
    onSecondaryContainer = gray,
    tertiary = gray,
    onTertiary = gray,
    tertiaryContainer = gray,
    onTertiaryContainer = gray,
//    background = gray,
//    onBackground = gray,
//    surface = gray,
//    onSurface = gray,
//    surfaceVariant = gray,
//    onSurfaceVariant = gray,
//    surfaceTint = gray,
//    inverseSurface = gray,
//    inverseOnSurface = gray,
    error = gray,
    onError = gray,
    errorContainer = gray,
    onErrorContainer = gray,
    outline = gray,
    outlineVariant = gray,
    scrim = gray,
//    primary = Purple80,
//    secondary = PurpleGrey80,
//    tertiary = Pink80
)

//private val LightColorScheme = lightColorScheme(
//    primary = Purple40,
//    secondary = PurpleGrey40,
//    tertiary = Pink40
//
//    /* Other default colors to override
//    background = Color(0xFFFFFBFE),
//    surface = Color(0xFFFFFBFE),
//    onPrimary = Color.White,
//    onSecondary = Color.White,
//    onTertiary = Color.White,
//    onBackground = Color(0xFF1C1B1F),
//    onSurface = Color(0xFF1C1B1F),
//    */
//)

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
//    val colorScheme = when {
//        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
//            val context = LocalContext.current
//            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
//        }
//
//        darkTheme -> DarkColorScheme
//        else -> LightColorScheme
//    }

//    val colorScheme = DarkColorScheme
//    val view = LocalView.current
//    if (!view.isInEditMode) {
//        SideEffect {
//            val window = (view.context as Activity).window
//            window.statusBarColor = colorScheme.primary.toArgb()
//            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
//        }
//    }

    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}