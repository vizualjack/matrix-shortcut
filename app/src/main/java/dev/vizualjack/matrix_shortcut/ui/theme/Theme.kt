package dev.vizualjack.matrix_shortcut.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import dev.vizualjack.matrix_shortcut.R


@Composable
fun AppTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = darkColorScheme(
        background = colorResource(R.color.background),
        onBackground = colorResource(R.color.text),
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}