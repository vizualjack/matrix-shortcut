package dev.vizualjack.matrix_shortcut.ui.components

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.DialogProperties
import androidx.compose.ui.window.DialogWindowProvider
import dev.vizualjack.matrix_shortcut.ui.theme.spacing
import dev.vizualjack.matrix_shortcut.ui.theme.strokeWidth


@Composable
fun Popup(onDismissRequest: () -> Unit, alignment: Alignment = Alignment.Center, modifier: Modifier = Modifier, header: (@Composable () -> Unit)? = null, content: @Composable () -> Unit) {
    Box(
        Modifier.background(Color(0,0,0,0x7F))
            .fillMaxSize()
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { onDismissRequest() },
        contentAlignment = alignment
    ) {
        Box(Modifier
            .padding(MaterialTheme.spacing.md)
            .clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) {}
        ) {
            Box(
                modifier
                    .background(MaterialTheme.colorScheme.background, MaterialTheme.shapes.medium)
                    .border(MaterialTheme.strokeWidth.normal, MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
            ) {
                Column(
                    Modifier.padding(MaterialTheme.spacing.md),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if(header != null) header()
                    content()
                }
            }
        }
    }
}