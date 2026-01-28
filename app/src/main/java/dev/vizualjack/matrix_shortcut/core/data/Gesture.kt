package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
data class Gesture(
    var name: String,
    var message: String,
    var gestureEntries: ArrayList<GestureEntry>
)
