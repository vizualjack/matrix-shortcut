package dev.vizualjack.matrix_shortcut.ui


import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.Storage
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.data.storage.SettingsStorage
import dev.vizualjack.matrix_shortcut.ui.screen.GestureEdit
import dev.vizualjack.matrix_shortcut.ui.screen.GestureList
import dev.vizualjack.matrix_shortcut.ui.screen.SettingsPage

enum class Location() {
    Gestures,
    Gesture,
    Settings
}

@Composable
fun AppUI(
    activity: AppActivity,
    viewModel: ViewModel = viewModel(),
    navController: NavHostController = rememberNavController()
) {
    var gestures by remember { mutableStateOf(Storage(activity.applicationContext).loadGestures()) }
    var selectedGesture: Gesture? = null

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = Location.Gestures.name,
        ) {
            composable(route = Location.Settings.name) {
                SettingsPage(activity,
                    SettingsStorage(activity.applicationContext).loadSettings(),
                    backAction = {
                        navController.backQueue.clear()
                        navController.navigate(Location.Gestures.name)
                    }
                )
            }
            composable(route = Location.Gestures.name) {
                GestureList(
                    activity = activity,
                    gestures = gestures,
                    openGesture = {index ->
                        selectedGesture = gestures[index]
                        navController.backQueue.clear()
                        navController.navigate(Location.Gesture.name)
                    },
                    addGesture = {
                        gestures = gestures + Gesture(arrayListOf(),"")
                    },
                    onSettingsClick = {
                        navController.backQueue.clear()
                        navController.navigate(Location.Settings.name)
                    }
                )
            }
            composable(route = Location.Gesture.name) {
                GestureEdit(
                    gesture = selectedGesture!!,
                    onBack = {
                        Log.i("Screen", "selectedGesture gestureElements...")
                        for(gestureElement in selectedGesture!!.gestureEntries) {
                            Log.i("Screen", "key ${gestureElement.keyCode} for ${gestureElement.minDuration}ms")
                        }
                        Log.i("Screen", "selectedGesture gestureElements...done")
                        navController.backQueue.clear()
                        navController.navigate(Location.Gestures.name)
                        Storage(activity.applicationContext).saveGestures(gestures)
                    },
                    onDelete = {
                        gestures = gestures.toMutableList().apply { remove(selectedGesture) }
                        navController.backQueue.clear()
                        navController.navigate(Location.Gestures.name)
                    }
                )
            }
        }
    }
}