package dev.vizualjack.matrix_shortcut.core.data

import dev.vizualjack.matrix_shortcut.ui.screen.Settings
import kotlinx.serialization.Serializable

@Serializable
data class StorageData(
    var settings: Settings? = null,
    var gestures: List<Gesture>? = null
)