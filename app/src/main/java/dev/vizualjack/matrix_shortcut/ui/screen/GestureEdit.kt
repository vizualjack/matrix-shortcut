package dev.vizualjack.matrix_shortcut.ui.screen

import android.view.KeyEvent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.GestureEntry
import dev.vizualjack.matrix_shortcut.ui.KeyCode
import dev.vizualjack.matrix_shortcut.ui.components.Dropdown
import dev.vizualjack.matrix_shortcut.ui.components.EditNumberField
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.components.Screen
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme
import dev.vizualjack.matrix_shortcut.ui.theme.dashedBorder
import dev.vizualjack.matrix_shortcut.ui.theme.spacing


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GestureEdit(editGesture: Gesture?, onSave: (gesture: Gesture) -> Unit, onBack: () -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(if(editGesture != null) editGesture.name else "") }
    var message by remember { mutableStateOf(if(editGesture != null) editGesture.message else "") }
    var gestureEntries by remember { mutableStateOf(if(editGesture != null) editGesture.gestureEntries else arrayListOf()) }

    fun delete() {
        onDelete()
        onBack()
    }

    fun save() {
        var gesture = Gesture("","", arrayListOf())
        if (editGesture != null) gesture = editGesture
        gesture.name = name
        gesture.message = message
        gesture.gestureEntries = gestureEntries
        onSave(gesture)
        onBack()
    }

    Screen({
            IconButton(onClick = { onBack() }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    Modifier.align(Alignment.CenterStart)
                )
            }

            Text((if(editGesture != null) "Edit" else "Add") + " shortcut", Modifier.align(Alignment.Center))
    },{
        Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
            EditStringField(
                labelText = "Name",
                value = name,
                onValueChanged = {
                    name = it
                },
                modifier = Modifier.fillMaxWidth()
            )

            EditStringField(
                labelText = "Message to send",
                value = message,
                onValueChanged = {
                    message = it
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row {
                Text("Keystrokes", Modifier.weight(1f))

                Box(Modifier
                    .background(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.shapes.extraLarge)
                    .padding(MaterialTheme.spacing.sm, MaterialTheme.spacing.xs)
                ) {
                    Text(gestureEntries.size.toString() + " STEPS")
                }
            }

            Column(modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)
                ) {
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    items(gestureEntries.size) { index ->
                        GestureEditEntry(
                            gestureElement = gestureEntries[index],
                            deleteGestureElement = {
                                gestureEntries = gestureEntries.toMutableList().apply { remove(gestureEntries[index]) } as ArrayList<GestureEntry>
                            }
                        )
                    }
                }

                NewGestureEntry("Add keystroke", onClick = {
                    gestureEntries = (gestureEntries + GestureEntry(KeyCode.VOLUME_UP.value, 0)) as ArrayList<GestureEntry>
                })
            }

            Column(
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if(editGesture != null) Button({delete()}, Modifier.fillMaxWidth()) { Text("Delete shortcut") }
                Button({save()}, Modifier.fillMaxWidth()) { Text("Save shortcut") }
            }
        }
    })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureEditEntry(gestureElement: GestureEntry, deleteGestureElement:() -> Unit) {
    var keyCodeValue = KeyCode.UNKNOWN
    if(gestureElement.keyCode == KeyCode.VOLUME_UP.value) keyCodeValue = KeyCode.VOLUME_UP
    if(gestureElement.keyCode == KeyCode.VOLUME_DOWN.value) keyCodeValue = KeyCode.VOLUME_DOWN

    var keyCode by remember { mutableStateOf(keyCodeValue) }
    var minDuration by remember { mutableStateOf(gestureElement.minDuration) }

    Box(Modifier
        .border(1.dp, color = MaterialTheme.colorScheme.primary, MaterialTheme.shapes.medium)
        .background(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
        .fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(MaterialTheme.spacing.lg, MaterialTheme.spacing.sm).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Dropdown(keyCode, KeyCode.values().filter { it != KeyCode.UNKNOWN }.toTypedArray(), {
                keyCode = it
                gestureElement.keyCode = keyCode.value
            },
                modifier = Modifier.width(150.dp).height(TextFieldDefaults.MinHeight),
                transparentBackground = true
            )
            Spacer(modifier = Modifier.weight(1f))
            EditNumberField(
                value = minDuration,
                onValueChanged = {
                    minDuration = it
                    gestureElement.minDuration = minDuration
                },
                modifier = Modifier.width(100.dp),
                textAlign = TextAlign.Right,
                transparentBackground = true
            )
            Text("ms")
            Spacer(modifier = Modifier.width(MaterialTheme.spacing.lg))
            IconButton(
                onClick = { deleteGestureElement() },
                modifier = Modifier.align(Alignment.CenterVertically).width(30.dp),
            ) {
                Icon(Icons.Filled.Delete, "Delete entry")
            }
        }
    }
}

@Composable
fun NewGestureEntry(text: String, onClick: () -> Unit) {
    val modifier = Modifier.dashedBorder(MaterialTheme.colorScheme.primary)

    Box(modifier.fillMaxWidth().clickable { onClick() }) {
        Row(modifier = Modifier.padding(MaterialTheme.spacing.lg).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(Icons.Filled.AddCircle, "Add gesture element")
            Spacer(Modifier.width(MaterialTheme.spacing.md))
            Text(text)
        }
    }
}
@Preview(showBackground = true)
@Composable
fun GestureEditPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background
        ) {
            GestureEdit(
                editGesture = Gesture("a", "b",arrayListOf(
                    GestureEntry(KeyEvent.KEYCODE_VOLUME_DOWN,100),
                    GestureEntry(KeyEvent.KEYCODE_VOLUME_UP,0)
                )),
                onBack = {},
                onSave = {},
                onDelete = {}
            )
        }
    }
}
