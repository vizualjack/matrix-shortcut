package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.ui.theme.spacing


@Composable
fun Screen(header: @Composable BoxScope.() -> Unit, content: @Composable BoxScope.() -> Unit) {
    Column(modifier = Modifier.systemBarsPadding().padding(MaterialTheme.spacing.md)) {
        Box(modifier = Modifier.fillMaxWidth().padding(0.dp, 0.dp, 0.dp, MaterialTheme.spacing.md)) {
            header()
        }

        Box(Modifier.fillMaxWidth().weight(1f)) {
            content()
        }
    }
}