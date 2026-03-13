package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
data class VibrationConfigEntry (
    val durationMillis: Long,
    val amplitude: Int
)

@Serializable
data class VibrationConfig (
    var onWakeUp: VibrationConfigEntry? = null,
    var onGestureDetected: VibrationConfigEntry? = null
)
