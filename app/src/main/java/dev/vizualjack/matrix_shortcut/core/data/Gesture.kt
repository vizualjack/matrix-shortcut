package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
class Gesture(
    var gestureEntries: ArrayList<GestureEntry>,
    var actionName: String
)
