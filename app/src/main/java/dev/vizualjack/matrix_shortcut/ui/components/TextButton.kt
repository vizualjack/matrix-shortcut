package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import dev.vizualjack.matrix_shortcut.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun TextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = colorResource(R.color.button)
) {
    androidx.compose.material3.Button({ onClick() }, modifier = modifier, enabled = enabled, colors = ButtonDefaults.buttonColors(color)) {
        Text(text, color = colorResource(R.color.text))
    }
}

