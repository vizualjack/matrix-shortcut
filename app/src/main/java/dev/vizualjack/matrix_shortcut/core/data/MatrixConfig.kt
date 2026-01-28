package dev.vizualjack.matrix_shortcut.core.data

import kotlinx.serialization.Serializable

@Serializable
data class MatrixConfig(
    var serverDomain: String? = null,
    var userName: String? = null,
    var accessToken: String? = null,
    var refreshToken: String? = null,
    var targetRoom: String? = null,
)
