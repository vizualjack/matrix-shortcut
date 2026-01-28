package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
data class GestureEntry(
    var keyCode: Int,
    var minDuration: Int
)