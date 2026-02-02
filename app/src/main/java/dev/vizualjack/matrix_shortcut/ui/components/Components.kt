package dev.vizualjack.matrix_shortcut.ui.components

import android.R
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStringField(
    text: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidden: Boolean = false
) {
    var label: @Composable (() -> Unit)? = null
    if (text != "") label = @Composable {
        Text(text)
    }

    val hideInput = { newValue: AnnotatedString ->
        TransformedText(AnnotatedString("*".repeat(newValue.length)), OffsetMapping.Identity)
    }

    TextField(
        value = value,
        singleLine = true,
        modifier = modifier,
        onValueChange = { onValueChanged(it) },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
        visualTransformation = if(hidden) VisualTransformation(hideInput) else VisualTransformation.None
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNumberField(
    text: String,
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var label: @Composable (() -> Unit)? = null
    if (text != "") label = @Composable {
        Text(text)
    }
    var valueAsStr = ""
    if (value > 0) valueAsStr = value.toString()
    TextField(
        value = valueAsStr,
        singleLine = true,
        modifier = modifier,
        onValueChange = { onValueChanged(it.toIntOrNull() ?: 0) },
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
    )
}