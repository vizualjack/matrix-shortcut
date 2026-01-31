package dev.vizualjack.matrix_shortcut.ui


import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
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
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.data.StorageData
import dev.vizualjack.matrix_shortcut.ui.screen.GestureEdit
import dev.vizualjack.matrix_shortcut.ui.screen.GestureList
import dev.vizualjack.matrix_shortcut.ui.screen.LoadErrorScreen
import dev.vizualjack.matrix_shortcut.ui.screen.LoadingScreen
import dev.vizualjack.matrix_shortcut.ui.screen.MatrixConfigUI

enum class Location() {
    Gestures,
    Gesture,
    MatrixConfig,
    Loading,
    LoadError
}

@Composable
fun AppUI(
    activity: AppActivity,
) {
//    val viewModel: ViewModel = viewModel()
    val navController: NavHostController = rememberNavController()

    var gestures by remember { mutableStateOf(emptyList<Gesture>()) }
    var selectedGesture: Gesture? = null

//    val storageData = Storage(activity.applicationContext).loadData() ?: StorageData()
//    if(storageData.gestures != null) gestures = storageData.gestures!!

    val startLocation: String
    if(activity.loadingStatus == AppActivity.LoadingStatus.LOADING) startLocation = Location.Loading.name
    else if(activity.loadingStatus == AppActivity.LoadingStatus.ERROR) startLocation = Location.LoadError.name
    else startLocation = Location.Gestures.name

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startLocation,
        ) {
            composable(route = Location.MatrixConfig.name) {
                MatrixConfigUI(activity,
                    MatrixConfig(), // SettingsStorage(activity.applicationContext).loadSettings(),
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
                    openGesture = { index ->
                        selectedGesture = gestures[index]
                        navController.backQueue.clear()
                        navController.navigate(Location.Gesture.name)
                    },
                    newGesture = {
                        selectedGesture = null
                        navController.backQueue.clear()
                        navController.navigate(Location.Gesture.name)
                    },
                    onSettingsClick = {
                        navController.backQueue.clear()
                        navController.navigate(Location.MatrixConfig.name)
                    }
                )
            }
            composable(route = Location.Gesture.name) {
                GestureEdit(
                    editGesture = selectedGesture,
                    onBack = {
                        navController.backQueue.clear()
                        navController.navigate(Location.Gestures.name)
                    },
                    onSave = { gesture: Gesture ->
//                        if(!gestures.contains(gesture)) gestures.toMutableList().apply { add(gesture) }
//                        storageData.gestures = gestures
//                        Storage(activity.applicationContext).saveData(storageData)
                    },
                    onDelete = {
//                        gestures = gestures.toMutableList().apply { remove(selectedGesture) }
//                        navController.backQueue.clear()
//                        navController.navigate(Location.Gestures.name)
                    }
                )
            }
            composable(route = Location.Loading.name) { LoadingScreen() }
            composable(route = Location.LoadError.name) { LoadErrorScreen() }
        }
    }
}