package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import dev.vizualjack.matrix_shortcut.ui.theme.spacing


@Composable
fun Popup(onDismissRequest: () -> Unit, alignment: Alignment = Alignment.Center, header: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = { onDismissRequest() }) {
        Box(
            Modifier.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.medium),
            alignment
        ) {
            Column(modifier = Modifier.padding(MaterialTheme.spacing.md), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md), horizontalAlignment = Alignment.CenterHorizontally) {
                if(header != null) header()
                content()
            }
        }
    }
}