package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.vizualjack.matrix_shortcut.R


@Composable
fun Popup(onDismissRequest: () -> Unit, alignment: Alignment = Alignment.Center, modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Dialog (
        onDismissRequest = { onDismissRequest() }
    ) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = alignment
        ) {
            Surface(
                shape = MaterialTheme.shapes.medium,
                tonalElevation = 10.dp,
                color = colorResource(R.color.popup)
            ) {
                content()
            }
        }
    }

}
