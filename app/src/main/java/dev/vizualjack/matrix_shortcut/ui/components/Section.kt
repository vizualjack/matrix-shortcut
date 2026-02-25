package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun Section(header: String, content: @Composable () -> Unit) {
    Box(
        modifier = Modifier.fillMaxWidth().background(colorResource(R.color.section), RoundedCornerShape(10.dp)).padding(13.dp),
    ) {
        Column (verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(header, color = colorResource(R.color.text_light))
            Box(modifier = Modifier.padding(5.dp)) { content() }
        }
    }
}
