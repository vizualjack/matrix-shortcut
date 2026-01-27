package dev.vizualjack.matrix_shortcut.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestureList(activity:AppActivity, gestures: List<Gesture>, addGesture:() -> Unit, openGesture:(Int) -> Unit, onSettingsClick:() -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
//            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            IconButton(onClick = {}, modifier = Modifier.menuAnchor()) {
                Icon(imageVector = Icons.Default.List, contentDescription = "Menu", Modifier.size(30.dp))
            }
            ExposedDropdownMenu(
                modifier = Modifier.width(100.dp),
                expanded = expanded,
                onDismissRequest = {expanded = false}
            ) {
                DropdownMenuItem(
                    text = { Text(text = "Import") },
                    onClick = { activity.importRequest() }
                )
                DropdownMenuItem(
                    text = { Text(text = "Export") },
                    onClick = { activity.exportRequest() }
                )
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        IconButton(onClick = {onSettingsClick()}) {
            Icon(imageVector = Icons.Default.Settings, contentDescription = "Settings", Modifier.size(30.dp))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.height(300.dp)) {
            LazyColumn {
                items(gestures.size) {index ->
                    GestureListEntry(gesture = gestures[index], onClick = { openGesture(index) })
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))
        SmallFloatingActionButton(
            onClick = { addGesture() },
        ) {
            Icon(Icons.Filled.Add, "Add gesture element")
        }
    }
}

@Composable
fun GestureListEntry(gesture: Gesture, onClick: () -> Unit) {
    Box(modifier = Modifier.height(40.dp)
        .fillMaxWidth()
        .padding(2.dp)
        .background(MaterialTheme.colorScheme.onPrimaryContainer, RoundedCornerShape(10.dp))
        .clickable {
            onClick()
        },
        contentAlignment = Alignment.Center) {
        Text(text = AnnotatedString(gesture.actionName), color = Color.White )
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
                activity = AppActivity(),
                gestures = listOf(
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                    Gesture(arrayListOf(),"aaa"),
                ),
                addGesture = {},
                openGesture = {},
                onSettingsClick = {}
            )
        }
    }
}