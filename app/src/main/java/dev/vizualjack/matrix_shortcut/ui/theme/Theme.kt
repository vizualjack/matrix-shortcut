package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R
import androidx.compose.runtime.staticCompositionLocalOf


val LocalSpacing = staticCompositionLocalOf { Spacing() }

val MaterialTheme.spacing: Spacing
    @Composable
    get() = LocalSpacing.current

@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val spacing = Spacing()

    CompositionLocalProvider(
        LocalSpacing provides spacing
    ) {
        MaterialTheme(
            shapes = shapes,
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}