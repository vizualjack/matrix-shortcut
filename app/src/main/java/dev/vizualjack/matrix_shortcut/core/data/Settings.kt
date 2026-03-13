package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    var matrixConfig: MatrixConfig,
    var vibrationConfig: VibrationConfig
)
