package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.vizualjack.matrix_shortcut.R


@Composable
fun Popup(onDismissRequest: () -> Unit, content: @Composable () -> Unit) {
    Dialog (
        onDismissRequest = { onDismissRequest() }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 10.dp,
            color = colorResource(R.color.popup),
        ) {
            content()
        }
    }

}
