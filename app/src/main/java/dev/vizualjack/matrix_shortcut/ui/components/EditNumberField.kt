package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import dev.vizualjack.matrix_shortcut.R

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
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        colors = TextFieldDefaults.textFieldColors(
            textColor = colorResource(R.color.text),
            focusedLabelColor = colorResource(R.color.text_accent),
            unfocusedLabelColor = colorResource(R.color.text),
            disabledLabelColor = colorResource(R.color.text),
            errorLabelColor = colorResource(R.color.text),
            containerColor = colorResource(R.color.text_inputs),
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = ShapeDefaults.Small,
    )
}