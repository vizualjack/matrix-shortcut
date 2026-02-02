package dev.vizualjack.matrix_shortcut.core.matrix

import kotlinx.serialization.Serializable


@Serializable
data class MemberInfo(
    val display_name: String
)

@Serializable
data class Member(
    val userName: String,
    val display_name: String
)

@Serializable
data class Rooms(
    val invite: Map<String, Unit>? = null,
    val join: Map<String, Unit>? = null
)

@Serializable
data class Identifier(
    val type: String = "m.id.user",
    val user: String
)