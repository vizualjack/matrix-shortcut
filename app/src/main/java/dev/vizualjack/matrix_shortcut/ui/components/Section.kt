package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
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


@Composable
fun Section(header: String? = null, padding: Dp = MaterialTheme.spacing.md, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.medium)
            .padding(padding),
    ) {
        Column (verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
            if(header != null) Text(header)
            Box(modifier = Modifier.padding(MaterialTheme.spacing.xs)) { content() }
        }
    }
}

