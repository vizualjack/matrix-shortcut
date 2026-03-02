package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.ui.theme.spacing
import dev.vizualjack.matrix_shortcut.ui.theme.strokeWidth


@Composable
fun TextButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, highlight: Boolean = false, enabled: Boolean = true, customColor: Color? = null, textColor: Color = MaterialTheme.colorScheme.onSurface) {
    var color = if(highlight) MaterialTheme.colorScheme.surfaceTint else MaterialTheme.colorScheme.surface
    if(customColor != null) color = customColor
    Button(
        { onClick() },
        modifier.height(50.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        enabled = enabled,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(text, color = textColor, style = MaterialTheme.typography.labelLarge)
    }
}

