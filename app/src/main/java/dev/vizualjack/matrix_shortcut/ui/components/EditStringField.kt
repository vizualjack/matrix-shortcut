package dev.vizualjack.matrix_shortcut.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import dev.vizualjack.matrix_shortcut.ui.theme.textSelectionColors
import dev.vizualjack.matrix_shortcut.ui.theme.spacing

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
        Text(placeholderText, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSecondary)
    }

    Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
        if (labelText != "") Text(labelText, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSecondary)



        CompositionLocalProvider(
            LocalTextSelectionColors provides textSelectionColors
        ) {
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
                keyboardOptions = KeyboardOptions(keyboardType = if (hidden) KeyboardType.Password else KeyboardType.Text),
                visualTransformation = if (hidden) PasswordVisualTransformation() else VisualTransformation.None,
                colors = TextFieldDefaults.textFieldColors(
                    textColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    disabledIndicatorColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = MaterialTheme.colorScheme.onSecondary,
                ),
                shape = MaterialTheme.shapes.medium,
                textStyle = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}