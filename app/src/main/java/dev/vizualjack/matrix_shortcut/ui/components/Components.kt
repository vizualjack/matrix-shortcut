package dev.vizualjack.matrix_shortcut.ui.components

import android.util.Log
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillNode
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalAutofill
import androidx.compose.ui.platform.LocalAutofillTree
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EditStringField(
    text: String,
    value: String,
    onValueChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
    hidden: Boolean = false,
    autofillType: AutofillType? = null
) {
    val autofill = LocalAutofill.current
    val autofillNode = remember { if(autofillType != null) AutofillNode(listOf(autofillType), onFill = {onValueChanged(it)}) else null }
    var label: @Composable (() -> Unit)? = null

    autofillNode?.let {
        LocalAutofillTree.current += it
    }

    if (text != "") label = @Composable {
        Text(text)
    }
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
        label = label,
        keyboardOptions = KeyboardOptions(keyboardType = if(hidden) KeyboardType.Password else KeyboardType.Text),
        visualTransformation = if(hidden) PasswordVisualTransformation() else VisualTransformation.None,
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