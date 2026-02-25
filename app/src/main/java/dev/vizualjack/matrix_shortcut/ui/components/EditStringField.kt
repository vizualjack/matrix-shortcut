package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EditStringField(
    labelText: String,
    placeholderText: String = "",
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    autofillType: AutofillType? = null
) {
    val autofill = LocalAutofill.current
    val autofillNode = remember { if(autofillType != null) AutofillNode(listOf(autofillType), onFill = {onValueChanged(it)}) else null }
    var placeholder: @Composable (() -> Unit)? = null

    autofillNode?.let {
        LocalAutofillTree.current += it
    }

    if (placeholderText != "") placeholder = @Composable {
        Text(placeholderText)
    }

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (labelText != "") Text(labelText, color = colorResource(R.color.text_light))

        TextField(
            value = value,
            singleLine = true,
            modifier = modifier
                .onFocusChanged {
                    if (autofill == null || autofillNode == null) return@onFocusChanged
                    if (it.isFocused) autofill.requestAutofillForNode(autofillNode)
                    else autofill.cancelAutofillForNode(autofillNode)
                }
                .onGloballyPositioned {
                    if (autofillNode == null) return@onGloballyPositioned
                    autofillNode.boundingBox = it.boundsInWindow()
                },
            onValueChange = { onValueChanged(it) },
            placeholder = placeholder,
//        label = label,
            keyboardOptions = KeyboardOptions(keyboardType = if(hidden) KeyboardType.Password else KeyboardType.Text),
            visualTransformation = if(hidden) PasswordVisualTransformation() else VisualTransformation.None,
            colors = TextFieldDefaults.textFieldColors(
                textColor = colorResource(R.color.text),
                focusedLabelColor = colorResource(R.color.text_accent),
                unfocusedLabelColor = colorResource(R.color.text),
                disabledLabelColor = colorResource(R.color.text),
                errorLabelColor = colorResource(R.color.text),
                containerColor = colorResource(R.color.text_input),
                disabledIndicatorColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
            ),
            shape = ShapeDefaults.Small,
        )
    }
}

