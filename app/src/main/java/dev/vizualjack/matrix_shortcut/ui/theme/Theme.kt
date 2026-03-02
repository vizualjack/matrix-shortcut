package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf


val LocalSpacing = staticCompositionLocalOf { Spacing() }
val LocalStrokeWidth = staticCompositionLocalOf { StrokeWidth() }
val LocalIconSize = staticCompositionLocalOf { IconSize() }

val MaterialTheme.spacing: Spacing
    @Composable
    get() = LocalSpacing.current

val MaterialTheme.strokeWidth: StrokeWidth
    @Composable
    get() = LocalStrokeWidth.current

val MaterialTheme.iconSize: IconSize
    @Composable
    get() = LocalIconSize.current

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val spacing = Spacing()
    val strokeWidth = StrokeWidth()
    val iconSize = IconSize()

    CompositionLocalProvider(
        LocalSpacing provides spacing,
        LocalStrokeWidth provides strokeWidth,
        LocalIconSize provides iconSize,
    ) {
        MaterialTheme(
            shapes = shapes,
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}