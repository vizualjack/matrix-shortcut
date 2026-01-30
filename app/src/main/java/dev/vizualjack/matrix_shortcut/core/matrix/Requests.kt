package dev.vizualjack.matrix_shortcut.core.matrix

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val type: String = "m.login.password",
    val identifier: Identifier,
    val password: String
)

@Serializable
data class RefreshTokenRequest(
    val refresh_token: String,
)

@Serializable
data class MessageRequest(
    val msgtype: String = "m.text",
    val body: String
)

enum class RoomVisibility(val text: String) {
    PRIVATE("private"),
    PUBLIC("public")
}

@Serializable
data class CreateRoomRequest(
    val name: String?,
    val is_direct: Boolean?,
    val visibility: String?,
    val invite: Array<String>? = null,
    val preset: String? = null,
)