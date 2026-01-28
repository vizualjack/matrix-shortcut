package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
data class StorageData(
    var matrixConfig: MatrixConfig? = null,
    var gestures: List<Gesture>? = null
)