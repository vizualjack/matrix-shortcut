package dev.vizualjack.matrix_shortcut.core.matrix

import kotlinx.serialization.Serializable

@Serializable
data class SyncResponse(
    val rooms: Rooms?
)

@Serializable
data class RoomNameResponse(
    val name: String?
)

@Serializable
data class JoinedRoomsResponse(
    val joined_rooms: Array<String>
)

@Serializable
data class JoinedMembersResponse(
    val joined: Map<String, MemberInfo>?
) {
    fun getMembers(): ArrayList<Member> {
        val members = arrayListOf<Member>()
        if(joined == null) return members
        for(entry in joined) {
            val fullUserId = entry.key
            val display_name = entry.value.display_name
            val username = fullUserId.replace("@", "").split(":")[0]
            members.add(Member(username, display_name))
        }
        return members
    }
}

@Serializable
data class SuccessfulLoginResponse(
    val access_token: String?,
    val refresh_token: String?,
)

@Serializable
data class SuccessfulRefreshTokenResponse(
    val access_token: String?,
    val refresh_token: String?,
)