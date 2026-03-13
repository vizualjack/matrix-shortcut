package dev.vizualjack.matrix_shortcut.ui


import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.data.Settings
import dev.vizualjack.matrix_shortcut.core.data.VibrationConfig
import dev.vizualjack.matrix_shortcut.ui.screen.GestureEdit
import dev.vizualjack.matrix_shortcut.ui.screen.GestureList
import dev.vizualjack.matrix_shortcut.ui.screen.LoadErrorScreen
import dev.vizualjack.matrix_shortcut.ui.screen.LoadingScreen
import dev.vizualjack.matrix_shortcut.ui.screen.SettingsUI

enum class Location() {
    Gestures,
    Gesture,
    MatrixConfig,
    Loading,
    LoadError
}

val TRANSITION_TIME = 300

@Composable
fun AppUI(
    activity: AppActivity,
) {
    val navController: NavHostController = rememberNavController()

    var settings = remember { Settings(MatrixConfig(), VibrationConfig()) }
    var gestures = remember { emptyList<Gesture>() }
    var selectedGesture: Gesture? = null

    fun onDataLoaded() {
        if(activity.storageData == null) return
        if(activity.storageData!!.gestures != null) gestures = activity.storageData!!.gestures!!
        if(activity.storageData!!.settings != null) settings = activity.storageData!!.settings!!
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
            enterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(TRANSITION_TIME)
                )
            },
            exitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(TRANSITION_TIME)
                )
            },
            popEnterTransition = {
                slideIntoContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(TRANSITION_TIME)
                )
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(TRANSITION_TIME)
                )
            }
        ) {
            composable(route = Location.MatrixConfig.name) {
                SettingsUI(activity,
                    settings,
                    onSave = { newSettings: Settings ->
                        settings = newSettings
                        activity.storageData!!.settings = settings
                    },
                    onBack = {
                        navController.popBackStack()
                    }
                )
            }
            composable(route = Location.Gestures.name) {
                GestureList(
                    activity = activity,
                    gestures = gestures,
                    openGesture = { gesture ->
                        selectedGesture = gesture
                        navController.navigate(Location.Gesture.name)
                    },
                    newGesture = {
                        selectedGesture = null
                        navController.navigate(Location.Gesture.name)
                    },
                    onSettingsClick = {
                        navController.navigate(Location.MatrixConfig.name)
                    }
                )
            }
            composable(route = Location.Gesture.name) {
                GestureEdit(
                    editGesture = selectedGesture,
                    onBack = {
                        navController.popBackStack()
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