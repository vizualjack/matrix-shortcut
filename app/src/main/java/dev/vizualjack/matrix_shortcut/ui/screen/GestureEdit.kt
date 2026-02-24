package dev.vizualjack.matrix_shortcut.ui.screen

import android.view.KeyEvent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.GestureEntry
import dev.vizualjack.matrix_shortcut.ui.KeyCode
import dev.vizualjack.matrix_shortcut.ui.components.TextButton
import dev.vizualjack.matrix_shortcut.ui.components.Dropdown
import dev.vizualjack.matrix_shortcut.ui.components.EditNumberField
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun GestureEdit(editGesture: Gesture?, onSave: (gesture: Gesture) -> Unit, onBack: () -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(if(editGesture != null) editGesture.name else "") }
    var message by remember { mutableStateOf(if(editGesture != null) editGesture.message else "") }
    var gestureEntries by remember { mutableStateOf(if(editGesture != null) editGesture.gestureEntries else arrayListOf()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        TextButton("Back", {
            onBack()
        })
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Row {
            TextButton("Delete",
                enabled = editGesture != null,
                onClick = {
                    onDelete()
                    onBack()
                },
                color = colorResource(R.color.error_container)
            )
            Spacer(modifier = Modifier.width(5.dp))
            TextButton("Save", {
                    var gesture = Gesture("","", arrayListOf())
                    if (editGesture != null) gesture = editGesture
                    gesture.name = name
                    gesture.message = message
                    gesture.gestureEntries = gestureEntries
                    onSave(gesture)
                    onBack()
                },
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Spacer(modifier = Modifier.width(100.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        EditStringField(
            text = "name",
            value = name,
            onValueChanged = {
                name = it
            }
        )
        Spacer(modifier = Modifier.height(10.dp))
        EditStringField(
            text = "message",
            value = message,
            onValueChanged = {
                message = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxHeight(0.5f)) {
            LazyColumn {
                items(gestureEntries.size) { index ->
                    GestureEditEntry(
                        gestureElement = gestureEntries[index],
                        deleteGestureElement = {
                            gestureEntries = gestureEntries.toMutableList().apply { remove(gestureEntries[index]) } as ArrayList<GestureEntry>
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        SmallFloatingActionButton(
            onClick = {
                gestureEntries = (gestureEntries + GestureEntry(KeyCode.VOLUME_UP.value, 0)) as ArrayList<GestureEntry>
            },
            containerColor = colorResource(R.color.button),
            contentColor = colorResource(R.color.text)
        ) {
            Icon(Icons.Filled.Add, "Add gesture element")
        }
    }
}

@Composable
fun GestureEditEntry(gestureElement: GestureEntry, deleteGestureElement:() -> Unit) {
    var keyCodeValue = KeyCode.UNKNOWN
    if(gestureElement.keyCode == KeyCode.VOLUME_UP.value) keyCodeValue = KeyCode.VOLUME_UP
    if(gestureElement.keyCode == KeyCode.VOLUME_DOWN.value) keyCodeValue = KeyCode.VOLUME_DOWN

    var keyCode by remember { mutableStateOf(keyCodeValue) }
    var minDuration by remember { mutableStateOf(gestureElement.minDuration) }

    Row(modifier = Modifier.padding(3.dp)) {
        Dropdown(keyCode, KeyCode.values().filter { it != KeyCode.UNKNOWN }.toTypedArray(), {
                keyCode = it
                gestureElement.keyCode = keyCode.value
            },
            modifier = Modifier.width(161.dp))
        Spacer(modifier = Modifier.width(3.dp))
        EditNumberField(
            text = "min. dur (ms)",
            value = minDuration,
            onValueChanged = {
                minDuration = it
                gestureElement.minDuration = minDuration
            },
            modifier = Modifier.width(140.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        IconButton(
            onClick = { deleteGestureElement() },
            modifier = Modifier.align(Alignment.CenterVertically),
        ) {
            Icon(Icons.Filled.Delete,"Delete entry")
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
                editGesture = Gesture("Name goes here", "may day",arrayListOf(
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
