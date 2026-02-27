package dev.vizualjack.matrix_shortcut.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.core.content.ContextCompat.startActivity
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.core.GestureDetectorService
import dev.vizualjack.matrix_shortcut.core.isAccessibilityServiceEnabled
import dev.vizualjack.matrix_shortcut.ui.components.Screen
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme
import dev.vizualjack.matrix_shortcut.ui.theme.dashedBorder
import dev.vizualjack.matrix_shortcut.ui.theme.spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureList(activity:AppActivity?, gestures: List<Gesture>, newGesture:() -> Unit, openGesture:(Gesture) -> Unit, onSettingsClick:() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Screen(
        {
            FilledIconButton({expanded = true}, modifier = Modifier.align(Alignment.CenterStart)) {
                Icon(imageVector = Icons.Default.List, contentDescription = "Menu")
            }

            Text("My shortcuts", Modifier.align(Alignment.Center))

            FilledIconButton({onSettingsClick()}, modifier = Modifier.align(Alignment.CenterEnd)) {
                Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings")
            }
        },
        {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Column(Modifier.weight(1f).padding(0.dp, 0.dp, 0.dp, MaterialTheme.spacing.md)) {
                    LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                        items(gestures.size) {index ->
                            val gesture = gestures[index]
                            ListEntry(gesture.name, onClick = { openGesture(gesture) })
                        }
                    }
                    Spacer(Modifier.height(MaterialTheme.spacing.md))
                    ListEntry("Add new shortcut", onClick = { newGesture() }, true)
                }

                val serviceEnabled = activity != null && isAccessibilityServiceEnabled(activity.applicationContext, GestureDetectorService::class.java)
                Button(
                    onClick = {
                        if(serviceEnabled) return@Button
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        startActivity(activity!!.applicationContext, intent, null)
                    }) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                    ) {
                        Box(Modifier.width(8.dp).height(8.dp).background(if(serviceEnabled) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error, CircleShape))
                        Text(if(serviceEnabled) "Service is active" else "Please activate the service")
                    }
                }
            }
        }
    )

    if (expanded) {
        ImportExportPopup(
            {
                activity!!.importRequest()
                expanded = false
            },
            {
                activity!!.exportRequest()
                expanded = false
            },
            {
                expanded = false
            }
        )
    }
}

@Composable
fun ListEntry(text: String, onClick: () -> Unit, addStyle: Boolean = false) {
    val backgroundColor = MaterialTheme.colorScheme.primaryContainer
    val roundedCornerShape = MaterialTheme.shapes.medium
    var modifier = Modifier
                .height(75.dp)
                .fillMaxWidth()
                .clickable {
                    onClick()
                }
    modifier = if(addStyle) modifier.dashedBorder(backgroundColor) else modifier.background(backgroundColor, roundedCornerShape)
    Box(modifier = modifier) {
        Row(Modifier.padding(MaterialTheme.spacing.lg, MaterialTheme.spacing.xl).fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
        ) {
            if(addStyle) Icon(imageVector = Icons.Default.Add, contentDescription = "Add")
            Text(text, Modifier.weight(1f))
            if(!addStyle) Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Arrow right")
        }
    }
}

@Composable
fun ImportExportPopup(onImport: () -> Unit, onExport: () -> Unit, onClose: () -> Unit) {
    Popup(onDismissRequest = { onClose() }, alignment = Alignment.BottomCenter) {
        Column(
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(MaterialTheme.spacing.md, MaterialTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            Button({onImport()}, modifier = Modifier.fillMaxWidth()) { Text("Import configuration") }
            Button({onExport()}, modifier = Modifier.fillMaxWidth()) { Text("Export configuration") }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GestureListPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            GestureList(
                activity = null,
                gestures = listOf(
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                    Gesture("give me a name", "may day", arrayListOf()),
                ),
                newGesture = {},
                openGesture = {},
                onSettingsClick = {}
            )
        }
    }
}