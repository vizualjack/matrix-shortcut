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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.GestureEntry
import dev.vizualjack.matrix_shortcut.ui.KeyCode
import dev.vizualjack.matrix_shortcut.ui.components.EditNumberField
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme

@Composable
fun GestureEdit(gesture: Gesture, onBack:() -> Unit, onDelete:() -> Unit) {
    var gestureElements by remember { mutableStateOf(gesture.gestureEntries) }
    var actionName by remember { mutableStateOf(gesture.actionName) }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Button(onClick = {
            gesture.actionName = actionName
            gesture.gestureEntries = gestureElements
            onBack()
        }) {
            Text(text = "Back")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Button(onClick = { onDelete() }, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFBF0000))) {
            Text(text = "Delete")
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row {
            Spacer(modifier = Modifier.width(100.dp))
        }
        Spacer(modifier = Modifier.height(32.dp))
        EditStringField(
            text = "message",
            value = actionName,
            onValueChanged = {
                actionName = it
            }
        )
        Spacer(modifier = Modifier.height(20.dp))
        Box(modifier = Modifier.fillMaxHeight(0.5f)) {
            LazyColumn {
                items(gestureElements.size) {index ->
                    GestureEditEntry(
                        gestureElement = gestureElements[index],
                        deleteGestureElement = {
                            gestureElements = gestureElements.toMutableList().apply { remove(gestureElements[index]) } as ArrayList<GestureEntry>
                        }
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        SmallFloatingActionButton(
            onClick = {
                gestureElements = (gestureElements + GestureEntry(KeyCode.VOLUME_UP.value, 0)) as ArrayList<GestureEntry>
            },
        ) {
            Icon(Icons.Filled.Add, "Add gesture element")
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
                gesture = Gesture(arrayListOf(
                    GestureEntry(KeyEvent.KEYCODE_VOLUME_DOWN,100),
                    GestureEntry(KeyEvent.KEYCODE_VOLUME_UP,0)
                ),"TEST"),
                onBack = {},
                onDelete = {}
            )
        }
    }
}

@Composable
fun GestureEditEntry(gestureElement: GestureEntry, deleteGestureElement:() -> Unit) {
    var keyCodeVal = KeyCode.VOLUME_DOWN
    if(gestureElement.keyCode == KeyCode.VOLUME_UP.value) keyCodeVal = KeyCode.VOLUME_UP

    var keyCode by remember { mutableStateOf(keyCodeVal) }
    var minDuration by remember { mutableStateOf(gestureElement.minDuration) }

    Row(modifier = Modifier.padding(3.dp)) {
        KeyCodeDropdown(keyCode = keyCode,
            onKeyCodeChanged = {
                keyCode = it
                gestureElement.keyCode = keyCode.value
            },
            modifier = Modifier.width(161.dp)
        )
        Spacer(modifier = Modifier.width(3.dp))
        EditNumberField(
            text = "min dur. (ms)",
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalStdlibApi::class)
@Composable
fun KeyCodeDropdown(
    keyCode: KeyCode,
    onKeyCodeChanged: (KeyCode) -> Unit,
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
                value = keyCode.name,
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodySmall,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                KeyCode.values().forEach {
                    DropdownMenuItem(
                        text = { Text(text = it.name) },
                        onClick = {
                            onKeyCodeChanged(it)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}