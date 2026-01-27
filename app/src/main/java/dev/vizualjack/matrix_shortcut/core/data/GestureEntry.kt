package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
class GestureEntry(
    var keyCode:Int,
    var minDuration:Int
)