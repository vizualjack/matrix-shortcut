package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.ui.theme.spacing
import dev.vizualjack.matrix_shortcut.ui.theme.strokeWidth


@Composable
fun Section(header: String? = null, padding: Dp = MaterialTheme.spacing.md, modifier: Modifier = Modifier,content: @Composable () -> Unit) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
            .border(MaterialTheme.strokeWidth.normal, color = MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            .padding(padding),
    ) {
        Column (verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            if(header != null) Text(header, color = MaterialTheme.colorScheme.onSecondary)
            Box(modifier = Modifier.padding(MaterialTheme.spacing.xs)) { content() }
        }
    }
}

