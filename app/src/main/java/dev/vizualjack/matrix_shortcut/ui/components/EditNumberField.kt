package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import dev.vizualjack.matrix_shortcut.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNumberField(
    text: String? = null,
    value: Int,
    onValueChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Left,
    transparentBackground: Boolean = false,
) {
    var label: @Composable (() -> Unit)? = null
    if (text != null && text != "") label = @Composable {
        Text(text)
    }
    TextField(
        value = value.toString(),
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
            containerColor = if(transparentBackground) Color.Transparent else colorResource(R.color.text_input),
            disabledIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        shape = ShapeDefaults.Small,
        textStyle = TextStyle(
            textAlign = textAlign
        )
    )
}