package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import dev.vizualjack.matrix_shortcut.ui.theme.textSelectionColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNumberField(
    text: String? = null,
    value: Int?,
    onValueChanged: (Int?) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Left,
    transparentBackground: Boolean = false,
) {
    var label: @Composable (() -> Unit)? = null
    if (text != null && text != "") label = @Composable {
        Text(text, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondary)
    }

    CompositionLocalProvider(
        LocalTextSelectionColors provides textSelectionColors
    ) {
        TextField(
            value = value?.toString() ?: "",
            singleLine = true,
            modifier = modifier,
            onValueChange = { onValueChanged(it.toIntOrNull()) },
            label = label,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            colors = TextFieldDefaults.textFieldColors(
                textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                containerColor = if(transparentBackground) Color.Transparent else Color.Unspecified,
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = MaterialTheme.colorScheme.onSecondary,
            ),
            shape = ShapeDefaults.Small,
            textStyle = MaterialTheme.typography.labelLarge.merge(TextStyle(
                textAlign = textAlign
            ))
        )
    }
}