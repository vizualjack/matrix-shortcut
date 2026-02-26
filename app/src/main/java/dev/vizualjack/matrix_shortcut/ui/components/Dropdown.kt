package dev.vizualjack.matrix_shortcut.ui.components

import android.view.KeyEvent
import android.view.Surface
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.MenuItemColors
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.GestureEntry
import dev.vizualjack.matrix_shortcut.ui.screen.GestureEdit
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> Dropdown(
    value: T,
    values: Array<T>,
    onChange: (T) -> Unit,
    modifier: Modifier = Modifier,
    transparentBackground: Boolean = false,
) {
    var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            },
            modifier = Modifier
                .background(Color.Transparent, RoundedCornerShape(15.dp))
                .padding(5.dp, 0.dp),
        ) {
            Row(modifier.menuAnchor().background(Color.Transparent),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(value.toString())
                Icon(Icons.Default.KeyboardArrowDown, "Open dropdown", tint = colorResource(R.color.text))
            }
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.Transparent, RoundedCornerShape(15.dp)),
            ) {
                Surface(
                    color = Color.Red,
                ) {
                    values.forEach {
                        DropdownMenuItem(
                            text = { Text(it.toString()) },
                            onClick = {
                                onChange(it)
                                expanded = false
                            },
                            modifier = Modifier.background(Color.Transparent),
                        )
                    }
                }

            }
        }
//    }
}


@Preview
@Composable
fun DropdownPreview() {
    Dropdown(1, arrayOf(1, 2, 3), {})
}