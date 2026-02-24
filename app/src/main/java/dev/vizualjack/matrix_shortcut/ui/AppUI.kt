package dev.vizualjack.matrix_shortcut.ui


import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
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
    val navController: NavHostController = rememberNavController()

    var matrixConfig = remember { MatrixConfig() }
    var gestures = remember { emptyList<Gesture>() }
    var selectedGesture: Gesture? = null

    fun onDataLoaded() {
        if(activity.storageData == null) return
        if(activity.storageData!!.gestures != null) gestures = activity.storageData!!.gestures!!
        if(activity.storageData!!.matrixConfig != null) matrixConfig = activity.storageData!!.matrixConfig!!
    }

    val startLocation: String
    if(activity.loadingStatus == AppActivity.LoadingStatus.LOADING) startLocation = Location.Loading.name
    else if(activity.loadingStatus == AppActivity.LoadingStatus.ERROR) startLocation = Location.LoadError.name
    else {
        startLocation = Location.Gestures.name
        onDataLoaded()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        NavHost(
            navController = navController,
            startDestination = startLocation,
            modifier = Modifier.systemBarsPadding()
        ) {
            composable(route = Location.MatrixConfig.name) {
                MatrixConfigUI(activity,
                    matrixConfig,
                    onSave = { newConfig: MatrixConfig ->
                        matrixConfig = newConfig
                        activity.storageData!!.matrixConfig = matrixConfig
                    },
                    onBack = {
                        navController.backQueue.clear()
                        navController.navigate(Location.Gestures.name)
                    }
                )
            }
            composable(route = Location.Gestures.name) {
                GestureList(
                    activity = activity,
                    gestures = gestures,
                    openGesture = { gesture ->
                        selectedGesture = gesture
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
                        if(!gestures.contains(gesture)) gestures = gestures.toMutableList().apply { add(gesture) }
                        activity.storageData!!.gestures = gestures
                    },
                    onDelete = {
                        gestures = gestures.toMutableList().apply { remove(selectedGesture) }
                        activity.storageData!!.gestures = gestures
                    }
                )
            }
            composable(route = Location.Loading.name) { LoadingScreen() }
            composable(route = Location.LoadError.name) { LoadErrorScreen() }
        }
    }
}