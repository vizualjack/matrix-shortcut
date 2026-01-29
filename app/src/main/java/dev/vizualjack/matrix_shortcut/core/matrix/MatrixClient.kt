package dev.vizualjack.matrix_shortcut.core.matrix

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.LogSaver
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.util.UUID


class MatrixClient(val context: Context, val serverDomain: String?) {
    val baseUrl = "https://${serverDomain}/_matrix/client/v3"

    enum class Error {
        UNKNOWN,
        SERVER_UNREACHABLE,
        UNAUTHORIZED,
        UNEXPECTED_RESPONSE,
        TOO_MANY_REQUESTS,
    }

    data class SimpleResult(val success: Boolean, val error: Error? = null)
    data class Result<Type>(val success: Boolean, val error: Error? = null, val value: Type)

    var userName: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var targetRoom: String? = null

    constructor(context: Context, matrixConfig: MatrixConfig) : this(context, matrixConfig.serverDomain) {
        userName = matrixConfig.userName
        accessToken = matrixConfig.accessToken
        refreshToken = matrixConfig.refreshToken
    }

    fun getConfig(): MatrixConfig {
        return MatrixConfig(
            serverDomain,
            userName,
            accessToken,
            refreshToken,
            targetRoom
        )
    }

    fun login(userName: String, password: String): SimpleResult {
        return loginRequest(userName, password)
    }

    fun createRoom(name: String, private: Boolean, username: String): SimpleResult {
        val request = CreateRoomRequest(name, null, if(private) "private" else "public", arrayOf("@$username:$serverDomain"))
        return createRoomRequst(request)
    }

    fun createPrivateChat(username: String): SimpleResult {
        val request = CreateRoomRequest(null, true, "private", arrayOf("@$username:$serverDomain"))
        return createRoomRequst(request)
    }

    private fun createRoomRequst(request: CreateRoomRequest): SimpleResult {
        val url = "$baseUrl/createRoom"
        val restClient = RESTClient(context, accessToken)
        val firstResponse = restClient.post(url, request, CreateRoomRequest.serializer())
        if(firstResponse == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if(firstResponse.code == HttpURLConnection.HTTP_UNAUTHORIZED) {
            if (refreshToken == null) return SimpleResult(false, Error.UNAUTHORIZED)
            val result = refreshAccessTokenRequest(refreshToken!!)
            if(!result.success) return result
        } else if(firstResponse.code != HttpURLConnection.HTTP_OK) return checkAndLogUnexpectedResponse(firstResponse)
        val secondResponse = restClient.post(url, request, CreateRoomRequest.serializer())
        if(secondResponse == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if(secondResponse.code == HttpURLConnection.HTTP_UNAUTHORIZED) return SimpleResult(false, Error.UNAUTHORIZED)
        else if(secondResponse.code != HttpURLConnection.HTTP_OK) return checkAndLogUnexpectedResponse(firstResponse)
        return SimpleResult(true)
    }

    fun getJoinedRooms(): SimpleResult {
        val restClient = RESTClient(context, accessToken)
        val response = restClient.get("$baseUrl/joined_rooms")
        if(response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if(response.code != 200) checkAndLogUnexpectedResponse(response)
        val responseObject = Json.decodeFromString<JoinedRoomsResponse>(response.text)
        if(responseObject.joined_rooms.size <= 0) return SimpleResult(true)
        for (roomId in responseObject.joined_rooms) {
            var roomName: String? = null
            val response = restClient.get("$baseUrl/rooms/$roomId/state/m.room.name")
            if(response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
            if(response.code != 200 && response.code != 404) return checkAndLogUnexpectedResponse(response)
            if(response.code == 200) roomName = Json.decodeFromString(RoomNameResponse.serializer(), response.text).name
            if(roomName == null) {
                val response = restClient.get("$baseUrl/rooms/$roomId/joined_members")
                if(response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
                if(response.code != 200 && response.code != 404) return checkAndLogUnexpectedResponse(response)
                if(response.code != 200) return SimpleResult(false, Error.UNKNOWN)
                val responseObject = Json.decodeFromString<JoinedMembersResponse>(response.text)
                val members = responseObject.getMembers()
                var chatPartnerMember: Member? = null
                for (member in members) {
                    if(member.userName == userName) continue
                    chatPartnerMember = member
                    break
                }
                if (chatPartnerMember == null) return SimpleResult(false, Error.UNKNOWN)
                roomName = chatPartnerMember.display_name
            }
//            TODO: user room id and room name to add it to the joined rooms list
        }
        return SimpleResult(true)
    }

    fun acceptAllInvites(): SimpleResult {
        val client = RESTClient(context, accessToken)
        val response = client.get("$baseUrl/sync")
        if(response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if(response.code != 200) checkAndLogUnexpectedResponse(response)
        val responseObject = Json.decodeFromString<SyncResponse>(response.text)
        if(responseObject.rooms == null || responseObject.rooms.invite.isEmpty()) return SimpleResult(true)
        for (roomId in responseObject.rooms.invite.keys) {
            val response = client.post("$baseUrl/join/$roomId")
            if (response == null) return SimpleResult(false, Error.UNKNOWN)
            else if (response.code != 200) return SimpleResult(false, Error.UNKNOWN)
        }
        return SimpleResult(true)
    }

    fun sendMessage(message: String): SimpleResult {
        val messageId = UUID.randomUUID().toString()
        val firstResponse = sendMessageRequest(message, messageId)
        if (firstResponse == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if(firstResponse.code == 200) return SimpleResult(true)
        if(firstResponse.code != 401) {
            val logLine = "unknown response:\nCode:${firstResponse.code}\nText:${firstResponse.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return SimpleResult(false, Error.UNKNOWN)
        }

        if (refreshToken == null) {
            val logLine = "no refresh token for requesting another access token!"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return SimpleResult(false, Error.UNAUTHORIZED)
        }

        val previousAccessToken = accessToken
        refreshAccessTokenRequest(refreshToken!!)
        if (accessToken == previousAccessToken) {
            val logLine = "access token didn't change!"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return SimpleResult(false, Error.UNAUTHORIZED)
        }

        val secondResponse = sendMessageRequest(message, messageId)
        if (secondResponse == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if(secondResponse.code == 200) return SimpleResult(true)
        if(secondResponse.code != 401) {
            val logLine = "unknown response:\nCode:${firstResponse.code}\nText:${firstResponse.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return SimpleResult(false, Error.UNKNOWN)
        }

        val logLine = "new access token didn't work!"
        Log.e(javaClass.name, logLine)
        LogSaver(context).save(logLine)
        return SimpleResult(false, Error.UNAUTHORIZED)
    }

    private fun loginRequest(userName: String, password: String): SimpleResult {
        val url = "$baseUrl/login"
        val request = LoginRequest(
            identifier = Identifier(
                user = "@$userName:$serverDomain"
            ),
            password = password
        )
        val response = RESTClient(context, accessToken).post(url, request, LoginRequest.serializer())
        if (response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if (response.code == HttpURLConnection.HTTP_FORBIDDEN)
            return SimpleResult(false, Error.UNAUTHORIZED)
        if (response.code != 200) return checkAndLogUnexpectedResponse(response)
        val loginResponse = Json.decodeFromString<SuccessfulLoginResponse>(response.text)
        if(loginResponse.access_token == null || loginResponse.access_token == "") {
            return SimpleResult(false, Error.UNEXPECTED_RESPONSE)
        }
        accessToken = loginResponse.access_token
        refreshToken = loginResponse.refresh_token
        return SimpleResult(true)
    }

    private fun refreshAccessTokenRequest(refreshToken: String): SimpleResult {
        val url = "$baseUrl/refresh"
        val request = RefreshTokenRequest(
            refresh_token = refreshToken
        )
        val response = RESTClient(context).post(url, request, RefreshTokenRequest.serializer())
        if (response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        if (response.code != HttpURLConnection.HTTP_OK) return checkAndLogUnexpectedResponse(response)
        val refreshTokenResponse = Json.decodeFromString<SuccessfulRefreshTokenResponse>(response.text)
        if(refreshTokenResponse.access_token == null || refreshTokenResponse.access_token == "") {
            return SimpleResult(false, Error.UNEXPECTED_RESPONSE)
        }
        accessToken = refreshTokenResponse.access_token
        if(refreshTokenResponse.refresh_token != null) this.refreshToken = refreshTokenResponse.refresh_token
        return SimpleResult(true)
    }

    private fun sendMessageRequest(message: String, messageId: String): RESTClient.Response? {
        val url = "$baseUrl/rooms/${targetRoom}/send/m.room.message/${messageId}"
        val request = MessageRequest(body = message)
        return RESTClient(context, accessToken).put(url, request, MessageRequest.serializer(),)
    }

    private fun checkAndLogUnexpectedResponse(response: RESTClient.Response): SimpleResult {
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED || response.code == HttpURLConnection.HTTP_FORBIDDEN) {
            return SimpleResult(false, Error.UNAUTHORIZED)
        } else if (response.code == 429) {
            return SimpleResult(false, Error.TOO_MANY_REQUESTS)
        }
        val logLine = "Unexpected matrix server response:\nCode: ${response.code}\nText: ${response.text}"
        Log.e(javaClass.name, logLine)
        LogSaver(context).save(logLine)
        return SimpleResult(false, Error.UNEXPECTED_RESPONSE)
    }
}