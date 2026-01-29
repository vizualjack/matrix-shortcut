package dev.vizualjack.matrix_shortcut.core.matrix

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.LogSaver
import dev.vizualjack.matrix_shortcut.core.createExceptionLine
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID


class MatrixClient(val context: Context, val serverDomain: String?) {
    val TIMEOUT = 5000

    data class ResultWithText(val result: Result, val text: String)

    enum class Result {
        SUCCESS,
        UNAUTHORIZED,
        UNREACHABLE,
        UNKNOWN
    }

    data class Response(val code: Int, val text: String)
    enum class RequestMethod(val value: String) {
        POST("POST"),
        GET("GET"),
        PUT("PUT")
    }

    val baseUrl = "https://${serverDomain}/_matrix/client/v3"

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

    fun login(userName: String, password: String): ResultWithText {
        return loginRequest(userName, password)
    }

    @Serializable
    data class CreateRoomRequest(
        val name: String?,
        val is_direct: Boolean?,
        val visibility: String?,
        val invite: Array<String>? = null,
        val preset: String? = null,
    )

    fun createRoom(name: String, private: Boolean, username: String): ResultWithText {
//        POST /_matrix/client/v3/createRoom
//        {
//            "name": "My Private Room",
//            "visibility": "private"
//            "invite": ["@bob:example.org"],
//        }
        val request = CreateRoomRequest(name, null, if(private) "private" else "public", arrayOf("@$username:$serverDomain"))
        val requestJson = Json.encodeToString(CreateRoomRequest.serializer(), request)
        val response = postRequest("$baseUrl/createRoom", true, requestJson)
        if(response == null) return ResultWithText(Result.UNREACHABLE, "Got no response")
        if(response.code != 200) return getErrorOfFailedResponse(response)
        return ResultWithText(Result.SUCCESS, "Successfully created room")
    }

    fun createPrivateChat(username: String): ResultWithText {
//       POST  /_matrix/client/v3/createRoom
//        {
//            "visibility": "private"
//            "invite": ["@bob:example.org"],
//            "is_direct": true
//        }
        val request = CreateRoomRequest(null, true, "private", arrayOf("@$username:$serverDomain"))
        val requestJson = Json.encodeToString(CreateRoomRequest.serializer(), request)
        val response = postRequest("$baseUrl/createRoom", true, requestJson)
        if(response == null) return ResultWithText(Result.UNREACHABLE, "Got no response")
        if(response.code != 200) return getErrorOfFailedResponse(response)
        return ResultWithText(Result.SUCCESS, "Successfully created private chat")
    }

    @Serializable
    data class JoinedRoomsResponse(
        val joined_rooms: Array<String>
    )

    @Serializable
    data class RoomNameResponse(
        val name: String
    )

    @Serializable
    data class MemberInfo(
        val display_name: String
    )

    data class Member(
        val userName: String,
        val display_name: String
    )

    @Serializable
    data class JoinedMembersResponse(
        val joined: Map<String, MemberInfo>
    ) {
        fun getMembers(): ArrayList<Member> {
            val members = arrayListOf<Member>()
            for(entry in joined) {
                val fullUserId = entry.key
                val display_name = entry.value.display_name
                val username = fullUserId.replace("@", "").split(":")[0]
                members.add(Member(username, display_name))
            }
            return members
        }
    }

    fun getJoinedRooms(): ResultWithText {
        val response = getRequest("$baseUrl/joined_rooms", true)
        if(response == null) return ResultWithText(Result.UNREACHABLE, "Got no response")
        if(response.code != 200) getErrorOfFailedResponse(response)
        val responseObject = Json.decodeFromString<JoinedRoomsResponse>(response.text)
        if(responseObject.joined_rooms.size <= 0) return ResultWithText(Result.SUCCESS, "No rooms")
        for (roomId in responseObject.joined_rooms) {
            var roomName: String? = null
            val response = getRequest("$baseUrl/rooms/$roomId/state/m.room.name", true)
            if(response == null) return ResultWithText(Result.UNREACHABLE, "Server unreachable")
            if(response.code != 200 && response.code != 404) return getErrorOfFailedResponse(response)
            if(response.code == 200) roomName = Json.decodeFromString(RoomNameResponse.serializer(), response.text).name
            if(roomName == null) {
                val response = getRequest("$baseUrl/rooms/$roomId/joined_members", true)
                if(response == null) return ResultWithText(Result.UNREACHABLE, "Server unreachable")
                if(response.code != 200 && response.code != 404) return getErrorOfFailedResponse(response)
                if(response.code != 200) return ResultWithText(Result.UNKNOWN, "Can't get joined member for room")
                val responseObject = Json.decodeFromString<JoinedMembersResponse>(response.text)
                val members = responseObject.getMembers()
                var chatPartnerMember: Member? = null
                for (member in members) {
                    if(member.userName == userName) continue
                    chatPartnerMember = member
                    break
                }
                if (chatPartnerMember == null) return ResultWithText(Result.UNKNOWN, "Can't get chat partner from joined members")
                roomName = chatPartnerMember.display_name
            }
//            TODO: user room id and room name to add it to the joined rooms list
        }
        return ResultWithText(Result.SUCCESS, "Got all joined rooms")
    }


    @Serializable
    data class Rooms(
        val invite: Map<String, Unit>,
        val join: Map<String, Unit>
    )

    @Serializable
    data class SyncResponse(
        val rooms: Rooms?
    )

    fun acceptAllInvites(): ResultWithText {
        val response = getRequest("$baseUrl/sync", true)
        if(response == null) return ResultWithText(Result.UNREACHABLE, "Got no response")
        if(response.code != 200) getErrorOfFailedResponse(response)
        val responseObject = Json.decodeFromString<SyncResponse>(response.text)
        if(responseObject.rooms == null || responseObject.rooms.invite.isEmpty()) return ResultWithText(Result.SUCCESS, "No invites")
        for (roomId in responseObject.rooms.invite.keys) {
            val response = postRequest("$baseUrl/join/$roomId", true)
            if (response == null) return ResultWithText(Result.UNKNOWN, "No response for join request")
            else if (response.code != 200) return ResultWithText(Result.UNKNOWN, "One join request wasn't successful")
        }
        return ResultWithText(Result.SUCCESS, "Accecpted all invites")
    }

    fun sendMessage(message: String): ResultWithText {
        val messageId = UUID.randomUUID().toString()
        val firstResponse = sendMessageRequest(message, messageId)
        if (firstResponse == null) return ResultWithText(Result.UNREACHABLE, "Server not reachable")
        if(firstResponse.code == 200) return ResultWithText(Result.SUCCESS, "Success")
        if(firstResponse.code != 401) {
            val logLine = "unknown response:\nCode:${firstResponse.code}\nText:${firstResponse.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNKNOWN, "Unknown error")
        }

        if (refreshToken == null) {
            val logLine = "no refresh token for requesting another access token!"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNAUTHORIZED, "Missing refresh token")
        }

        val previousAccessToken = accessToken
        refreshAccessTokenRequest(refreshToken!!)
        if (accessToken == previousAccessToken) {
            val logLine = "access token didn't change!"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNAUTHORIZED, "Access token didn't change")
        }

        val secondResponse = sendMessageRequest(message, messageId)
        if (secondResponse == null) return ResultWithText(Result.UNREACHABLE, "Server not reachable")
        if(secondResponse.code == 200) return ResultWithText(Result.SUCCESS, "Success")
        if(secondResponse.code != 401) {
            val logLine = "unknown response:\nCode:${firstResponse.code}\nText:${firstResponse.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNKNOWN, "Unknown error")
        }

        val logLine = "new access token didn't work!"
        Log.e(javaClass.name, logLine)
        LogSaver(context).save(logLine)
        return ResultWithText(Result.UNAUTHORIZED, "New access token didn't work!")
    }

    @Serializable
    data class Identifier(
        val type: String = "m.id.user",
        val user: String
    )

    @Serializable
    data class LoginRequest(
        val type: String = "m.login.password",
        val identifier: Identifier,
        val password: String
    )

    @Serializable
    data class SuccessfulLoginResponse(
        val access_token: String?,
        val refresh_token: String?,
    )

    @Serializable
    data class ErrorResponse(
        val error: String,
    )

    private fun loginRequest(userName: String, password: String): ResultWithText {
        val url = "$baseUrl/login"
        val loginRequest = LoginRequest(
            identifier = Identifier(
                user = "@$userName:$serverDomain"
            ),
            password = password
        )
        val requestBody = Json.encodeToString(LoginRequest.serializer(), loginRequest)
        val response = postRequest(url, false, requestBody)
        if (response == null) return ResultWithText(Result.UNREACHABLE, "Server not reachable")
        if (response.code == 200) {
            val loginResponse = Json.decodeFromString<SuccessfulLoginResponse>(response.text)
            if(loginResponse.access_token == null || loginResponse.access_token == "") {
                return ResultWithText(Result.UNKNOWN, "Got no access token")
            } else if (loginResponse.refresh_token == null || loginResponse.refresh_token == "") {
                return ResultWithText(Result.UNKNOWN, "Got no refresh token")
            }
            accessToken = loginResponse.access_token
            refreshToken = loginResponse.refresh_token
            return ResultWithText(Result.SUCCESS, "Success")
        }
        return getErrorOfFailedResponse(response)
    }

    @Serializable
    data class SuccessfulRefreshTokenResponse(
        val access_token: String?,
        val refresh_token: String?,
    )

    @Serializable
    data class RefreshTokenRequest(
        val refresh_token: String,
    )

    private fun refreshAccessTokenRequest(refreshToken: String): ResultWithText {
        val url = "$baseUrl/refresh"
        val refreshTokenRequest = RefreshTokenRequest(
            refresh_token = refreshToken
        )
        val requestBody = Json.encodeToString(RefreshTokenRequest.serializer(), refreshTokenRequest)
        val response = postRequest(url, false, requestBody)
        if (response == null) return ResultWithText(Result.UNREACHABLE, "Server not reachable")
        if (response.code == 200) {
            val refreshTokenResponse = Json.decodeFromString<SuccessfulRefreshTokenResponse>(response.text)
            if(refreshTokenResponse.access_token == null || refreshTokenResponse.access_token == "") {
                return ResultWithText(Result.UNKNOWN, "Got no access token")
            }
            accessToken = refreshTokenResponse.access_token
            if(refreshTokenResponse.refresh_token != null) this.refreshToken = refreshTokenResponse.refresh_token
            return ResultWithText(Result.SUCCESS, "Success")
        }
        return getErrorOfFailedResponse(response)
    }

    @Serializable
    data class MessageRequest(
        val msgtype: String = "m.text",
        val body: String
    )

    private fun sendMessageRequest(message: String, messageId: String): Response? {
        val url = "$baseUrl/rooms/${targetRoom}/send/m.room.message/${messageId}"
        val messageRequest = MessageRequest(body = message)
        val requestBody = Json.encodeToString(MessageRequest.serializer(), messageRequest)
        return putRequest(url, true, requestBody)
    }

    private fun getErrorOfFailedResponse(response: Response): ResultWithText {
        if (response.code == 400) {
            val logLine = "got 400:\n${response.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNKNOWN, "Got 400 from server")
        } else if (response.code == 401) {
            val logLine = "got 401:\n${response.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNKNOWN, "Got 401 from server")
        } else if (response.code == 403) {
            val errorResponse = Json.decodeFromString<ErrorResponse>(response.text)
            return ResultWithText(Result.UNAUTHORIZED, errorResponse.error)
        } else if (response.code == 429) {
            return ResultWithText(Result.UNKNOWN, "Too many requests!")
        } else if (response.code >= 500 && response.code < 600) {
            val logLine = "got 5xx response:\nCode:${response.code}\nText:${response.text}"
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return ResultWithText(Result.UNKNOWN, "Server error")
        }
        return ResultWithText(Result.UNKNOWN, "Unknown error")
    }

    private fun postRequest(url: String, needsAuthorization: Boolean, requestBody: String? = null): Response? {
        val connection = createConnection(url, RequestMethod.POST, needsAuthorization) ?: return null
        var response: Response? = null
        val needToReceive: Boolean
        if(requestBody != null) needToReceive = sendRequest(connection, requestBody)
        else needToReceive = true
        if(needToReceive) response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    private fun putRequest(url: String, needsAuthorization: Boolean, requestBody: String? = null): Response? {
        val connection = createConnection(url, RequestMethod.POST, needsAuthorization) ?: return null
        var response: Response? = null
        val needToReceive: Boolean
        if(requestBody != null) needToReceive = sendRequest(connection, requestBody)
        else needToReceive = true
        if(needToReceive) response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    private fun getRequest(url: String, needsAuthorization: Boolean): Response? {
        val connection = createConnection(url, RequestMethod.GET, needsAuthorization) ?: return null
        val response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    private fun sendRequest(connection: HttpURLConnection, requestBody: String): Boolean {
        try {
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(requestBody)
            outputStream.flush()
            outputStream.close()
            return true
        } catch (ex:Exception) {
            val logLine = createExceptionLine("error while sending request: ",ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return false
        }
    }

    private fun receiveResponse(connection: HttpURLConnection): Response? {
        var response: Response? = null
        try {
            response = Response(connection.responseCode, connection.responseMessage)
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while receiving response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
        }
        return response
    }

    private fun createConnection(url: String, method: RequestMethod, needsAuthorization: Boolean): HttpURLConnection? {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT
            connection.requestMethod = method.value
            if(method == RequestMethod.POST) connection.setRequestProperty("Content-Type", "application/json")
            if(needsAuthorization && accessToken != null) connection.setRequestProperty("Authorization", "Bearer $accessToken")
            connection.doOutput = true
            connection.doInput = true
            return connection
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while creating connection: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return null
        }
    }
}