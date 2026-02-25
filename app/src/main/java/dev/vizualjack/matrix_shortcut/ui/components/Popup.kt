package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.vizualjack.matrix_shortcut.R


@Composable
fun Popup(onDismissRequest: () -> Unit, alignment: Alignment = Alignment.Center, modifier: Modifier = Modifier.padding(20.dp, 0.dp), content: @Composable () -> Unit) {
    Box(Modifier.background(Color(0f,0f,0f,0.5f)).clickable { onDismissRequest() },
        contentAlignment = alignment
    ) {
        Box(
            modifier = modifier.background(color = colorResource(R.color.popup), shape = MaterialTheme.shapes.medium),
        ) {
            content()
        }
    }
}
