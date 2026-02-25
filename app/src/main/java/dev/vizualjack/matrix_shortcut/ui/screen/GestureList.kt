package dev.vizualjack.matrix_shortcut.ui.screen

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat.startActivity
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.core.GestureDetectorService
import dev.vizualjack.matrix_shortcut.core.isAccessibilityServiceEnabled
import dev.vizualjack.matrix_shortcut.ui.components.Button
import dev.vizualjack.matrix_shortcut.ui.components.IconButton
import dev.vizualjack.matrix_shortcut.ui.components.Popup
import dev.vizualjack.matrix_shortcut.ui.components.Text
import dev.vizualjack.matrix_shortcut.ui.components.TextButton
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureList(activity:AppActivity?, gestures: List<Gesture>, newGesture:() -> Unit, openGesture:(Gesture) -> Unit, onSettingsClick:() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = Modifier.systemBarsPadding()) {
        Box(
            modifier = Modifier.padding(16.dp)
        ) {
            Box(
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = {expanded = true}) {
                    Icon(imageVector = Icons.Default.List, contentDescription = "Menu", Modifier.size(30.dp))
                }
            }

            Text("My shortcuts", color = colorResource(R.color.text), size = 4f, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                IconButton(onClick = {onSettingsClick()}) {
                    Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", Modifier.size(30.dp))
                }
            }
        }

        Column(
            modifier = Modifier.weight(1f).padding(10.dp, 0.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(gestures.size) {index ->
                    val gesture = gestures[index]
                    ListEntry(gesture.name, onClick = { openGesture(gesture) })
                }
            }
            Spacer(Modifier.height(10.dp))
            ListEntry("Add new shortcut", onClick = { newGesture() }, true)
        }

        val serviceEnabled = activity != null && isAccessibilityServiceEnabled(activity.applicationContext, GestureDetectorService::class.java)
        Box(modifier = Modifier.fillMaxWidth().padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            Button(
                color = colorResource(R.color.button),
                onClick = {
                    if(serviceEnabled) return@Button
                    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    startActivity(activity!!.applicationContext, intent, null)
                }) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(Modifier.width(8.dp).height(8.dp).background(if(serviceEnabled) colorResource(R.color.success) else colorResource(R.color.error), CircleShape))
                    Text(if(serviceEnabled) "Service is active" else "Please activate the service")
                }
            }
        }
    }

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
    val roundedCornerShape = RoundedCornerShape(10.dp)
    val buttonColor = colorResource(R.color.button)
    var modifier = Modifier.height(75.dp)
                .fillMaxWidth()
                .padding(5.dp)
                .clickable {
                    onClick()
                }
    if(addStyle) {
        modifier = modifier.drawBehind {
            drawRoundRect(
                color = buttonColor,
                size = size,
                cornerRadius = CornerRadius(10.dp.toPx(), 10.dp.toPx()),
                style = Stroke(
                    width = 3.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(
                        floatArrayOf(10f, 10f),
                        0f
                    )
                )
            )
        }
    }
    else modifier = modifier.background(buttonColor, roundedCornerShape)
    Box(modifier = modifier) {
        Box(Modifier.padding(15.dp, 0.dp).fillMaxSize()) {
            Row(Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if(addStyle) {
                    Box(Modifier.background(colorResource(R.color.accent_button), roundedCornerShape).width(30.dp).height(30.dp), Alignment.Center) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = colorResource(R.color.text_accent))
                    }
                }
                Text(text, color = colorResource(if(addStyle) R.color.text_accent else R.color.text), size = 3.5f, modifier = Modifier.weight(1f).align(Alignment.CenterVertically))
                if(!addStyle) {
                    Icon(imageVector = Icons.Default.KeyboardArrowRight, contentDescription = "Arrow right", Modifier.size(30.dp))
                }
            }

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportExportPopup(onImport: () -> Unit, onExport: () -> Unit, onClose: () -> Unit) {
    Popup({ onClose() }, Alignment.BottomCenter, Modifier.offset(0.dp, 20.dp)) {
        Column(
            modifier = Modifier.fillMaxWidth().height(200.dp).padding(10.dp, 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextButton("Import configuration", {onImport()}, modifier = Modifier.fillMaxWidth())
            TextButton("Export configuration", {onExport()}, modifier = Modifier.fillMaxWidth())
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
                ),
                newGesture = {},
                openGesture = {},
                onSettingsClick = {}
            )
        }
    }
}