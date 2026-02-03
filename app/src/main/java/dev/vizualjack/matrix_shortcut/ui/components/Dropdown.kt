package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.ui.KeyCode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    value: T,
    values: Array<T>,
    onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = if(value != null) value.toString() else "",
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor(),
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
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(colorResource(R.color.dropdown))
            ) {
                values.forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.toString(), color = colorResource(R.color.text)) },
                        onClick = {
                            onChange(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}