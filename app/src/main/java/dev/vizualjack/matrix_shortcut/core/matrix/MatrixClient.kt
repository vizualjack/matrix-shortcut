package dev.vizualjack.matrix_shortcut.core.matrix

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.LogSaver
import dev.vizualjack.matrix_shortcut.core.createExceptionLine
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.util.UUID


class MatrixClient(val context: Context, val serverDomain: String?) {
    val baseUrl = "https://${serverDomain}/_matrix/client/v3"
    val json = Json{ignoreUnknownKeys = true}

    enum class Error {
        UNKNOWN,
        SERVER_UNREACHABLE,
        UNAUTHORIZED,
        FORBIDDEN,
        UNEXPECTED_RESPONSE,
        TOO_MANY_REQUESTS,
        NOT_FOUND
    }

    data class SimpleResult(val success: Boolean, val error: Error? = null) {
        fun <Type> toResult(): Result<Type> {
            return Result(success, error, null)
        }
    }
    data class Result<Type>(val success: Boolean, val error: Error? = null, val value: Type? = null)

    var userName: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null

    constructor(context: Context, serverDomain: String, userName: String, accessToken: String, refreshToken: String?) : this(context, serverDomain) {
        this.userName = userName
        this.accessToken = accessToken
        this.refreshToken = refreshToken
    }

    fun createRoom(name: String, inviteUserName: String): Result<String> {
        val response = createRoom(
            CreateRoomRequest(
            name,
            arrayOf(createFullUserId(inviteUserName))
        )
        )
        if(!response.success) return Result(false, response.error)
        return Result(true, null, response.value!!.room_id)
    }

    fun createPrivateChat(username: String): Result<String> {
        val response = createDirectMessageRoom(CreateDirectMessageRequest(
            arrayOf(createFullUserId(username))
        ))
        if(!response.success) return Result(false, response.error)
        return Result(true, null, response.value!!.room_id)
    }

    private fun createDirectMessageRoom(request: CreateDirectMessageRequest): Result<SuccessfulRoomCreationResponse> {
        var response = createDirectMessageRequst(request)
        val firstCheckResult = checkResponse(response)
        if(!firstCheckResult.success && firstCheckResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val refreshTokenResult = refreshAccessToken()
            if(!refreshTokenResult.success) return refreshTokenResult.toResult()
            response = createDirectMessageRequst(request)
            val secondCheckResult = checkResponse(response)
            if(!secondCheckResult.success) return secondCheckResult.toResult()
        }
        else if(!firstCheckResult.success) return firstCheckResult.toResult()
        try {
            return Result(true, null, json.decodeFromString<SuccessfulRoomCreationResponse>(response!!.text))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while decoding room created response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return Result(false, Error.UNEXPECTED_RESPONSE, null)
        }
    }

    private fun createRoom(request: CreateRoomRequest): Result<SuccessfulRoomCreationResponse> {
        var response = createRoomRequst(request)
        val firstCheckResult = checkResponse(response)
        if(!firstCheckResult.success && firstCheckResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val refreshTokenResult = refreshAccessToken()
            if(!refreshTokenResult.success) return refreshTokenResult.toResult()
            response = createRoomRequst(request)
            val secondCheckResult = checkResponse(response)
            if(!secondCheckResult.success) return secondCheckResult.toResult()
        }
        else if(!firstCheckResult.success) return firstCheckResult.toResult()
        try {
            return Result(true, null, json.decodeFromString<SuccessfulRoomCreationResponse>(response!!.text))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while decoding room created response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return Result(false, Error.UNEXPECTED_RESPONSE, null)
        }
    }

    fun getJoinedRooms(): Result<List<Room>> {
        val joinedRoomsResponse = joinedRoomsRequest()
        if(!joinedRoomsResponse.success) return Result(false, joinedRoomsResponse.error)
        val rooms = arrayListOf<Room>()
        for (roomId in joinedRoomsResponse.value!!.joined_rooms) {
            val joinedMembersResponse = joinedMembersRequest(roomId)
            if(!joinedMembersResponse.success) return Result(false, joinedMembersResponse.error)
            val members = joinedMembersResponse.value!!.getMembers()
            val roomNameResponse = roomNameRequest(roomId)
            if(!roomNameResponse.success) return Result(false, roomNameResponse.error)
            var roomName = roomNameResponse.value!!.name
            if(roomName == null) {
                var chatPartnerMember: Member? = null
                for (member in members) {
                    if(member.userName == userName) continue
                    chatPartnerMember = member
                    break
                }
                if (chatPartnerMember == null) roomName = roomId
                else roomName = chatPartnerMember.display_name
            }
            rooms.add(Room(roomId, roomName, members.size))
        }
        return Result(true, null, rooms)
    }

    private fun joinedRoomsRequest(): Result<JoinedRoomsResponse> {
        val url = "$baseUrl/joined_rooms"
        var response = createAuthorizedRESTClient().get(url)
        val checkResult = checkResponse(response)
        if(!checkResult.success && checkResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val result = refreshAccessToken()
            if(!result.success) return result.toResult()
            response = createAuthorizedRESTClient().get(url)
            val checkResult = checkResponse(response)
            if(!checkResult.success) return checkResult.toResult()
        } else if(!checkResult.success) return checkResult.toResult()
        try {
            return Result(true, null, json.decodeFromString<JoinedRoomsResponse>(response!!.text))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while decoding joined rooms response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return Result(false, Error.UNEXPECTED_RESPONSE, null)
        }
    }

    private fun joinedMembersRequest(roomId: String): Result<JoinedMembersResponse> {
        val url = "$baseUrl/rooms/$roomId/joined_members"
        var response = createAuthorizedRESTClient().get(url)
        val checkResult = checkResponse(response)
        if(!checkResult.success && checkResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val result = refreshAccessToken()
            if(!result.success) return result.toResult()
            response = createAuthorizedRESTClient().get(url)
            val checkResult = checkResponse(response)
            if(!checkResult.success) return checkResult.toResult()
        } else if (!checkResult.success && arrayListOf(Error.NOT_FOUND, Error.FORBIDDEN).indexOf(checkResult.error) == -1) return checkResult.toResult()
        try {
            if (arrayListOf(Error.NOT_FOUND, Error.FORBIDDEN).indexOf(checkResult.error) != -1) return Result(true, null, JoinedMembersResponse(null))
            return Result(true, null, json.decodeFromString<JoinedMembersResponse>(response!!.text))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while decoding joined members response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return Result(false, Error.UNEXPECTED_RESPONSE, null)
        }
    }

    private fun roomNameRequest(roomId: String): Result<RoomNameResponse> {
        val url = "$baseUrl/rooms/$roomId/state/m.room.name"
        var response = createAuthorizedRESTClient().get(url)
        val checkResult = checkResponse(response)
        if(!checkResult.success && checkResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val result = refreshAccessToken()
            if(!result.success) return result.toResult()
            response = createAuthorizedRESTClient().get(url)
            val checkResult = checkResponse(response)
            if(!checkResult.success) return checkResult.toResult()
        } else if(!checkResult.success && arrayListOf(Error.NOT_FOUND, Error.FORBIDDEN).indexOf(checkResult.error) == -1) return checkResult.toResult()
        try {
            if (arrayListOf(Error.NOT_FOUND, Error.FORBIDDEN).indexOf(checkResult.error) != -1) return Result(true, null, RoomNameResponse(null))
            return Result(true, null, json.decodeFromString<RoomNameResponse>(response!!.text))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while decoding room name response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return Result(false, Error.UNEXPECTED_RESPONSE, null)
        }
    }

    fun acceptAllInvites(): Result<Int> {
        val response = syncRequest()
        if(!response.success) return Result(false, response.error)
        if(response.value!!.rooms == null || response.value!!.rooms!!.invite == null  || response.value!!.rooms!!.invite!!.isEmpty()) return Result(true, null, 0)
        var acceptedInvites = 0
        for (roomId in response.value!!.rooms!!.invite!!.keys) {
            val joinResponse = joinRequest(roomId)
            if(!joinResponse.success) return Result(false, response.error)
            if(joinResponse.value!!) acceptedInvites += 1
        }
        return Result(true, null, acceptedInvites)
    }

    private fun syncRequest(): Result<SyncResponse> {
        val url = "$baseUrl/sync"
        var response = createAuthorizedRESTClient().get(url)
        val checkResult = checkResponse(response)
        if(!checkResult.success && checkResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val result = refreshAccessToken()
            if(!result.success) return result.toResult()
            response = createAuthorizedRESTClient().get(url)
            val checkResult = checkResponse(response)
            if(!checkResult.success) return checkResult.toResult()
        } else if(!checkResult.success) return checkResult.toResult()
        try {
            return Result(true, null, json.decodeFromString<SyncResponse>(response!!.text))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while decoding sync response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return Result(false, Error.UNEXPECTED_RESPONSE, null)
        }
    }

    private fun joinRequest(roomId: String): Result<Boolean> {
        val url = "$baseUrl/join/$roomId"
        var response = createAuthorizedRESTClient().post(url)
        val checkResult = checkResponse(response)
        if(!checkResult.success && checkResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val result = refreshAccessToken()
            if(!result.success) return result.toResult()
            response = createAuthorizedRESTClient().get(url)
            val checkResult = checkResponse(response)
            if(!checkResult.success) return checkResult.toResult()
        } else if(!checkResult.success && arrayListOf(Error.NOT_FOUND, Error.FORBIDDEN).indexOf(checkResult.error) == -1) return checkResult.toResult()
        if (arrayListOf(Error.NOT_FOUND, Error.FORBIDDEN).indexOf(checkResult.error) != -1) return Result(true, null,false)
        return Result(true, null,true)
    }

    fun sendMessage(roomId: String, message: String): SimpleResult {
        val messageId = UUID.randomUUID().toString()
        var response = sendMessageRequest(roomId, messageId, message)
        val checkResult = checkResponse(response)
        if(!checkResult.success && checkResult.error == Error.UNAUTHORIZED && refreshToken != null) {
            val result = refreshAccessToken()
            if(!result.success) return result
            response = sendMessageRequest(roomId, messageId, message)
            val checkResult = checkResponse(response)
            if(!checkResult.success) return checkResult
        } else if(!checkResult.success) return checkResult
        return SimpleResult(true)
    }

    fun login(userName: String, password: String): SimpleResult {
        val response = loginRequest(userName, password)
        val checkResult = checkResponse(response)
        if(!checkResult.success) return checkResult
        val loginResponse = Json{ignoreUnknownKeys = true}.decodeFromString<SuccessfulLoginResponse>(response!!.text)
        accessToken = loginResponse.access_token
        refreshToken = loginResponse.refresh_token
        return SimpleResult(true)
    }

    fun logout(): SimpleResult {
        return checkResponse(logoutRequest())
    }

    private fun refreshAccessToken(): SimpleResult {
        if(refreshToken == null) return SimpleResult(false, Error.UNAUTHORIZED)
        val response = refreshAccessTokenRequest(refreshToken!!)
        val checkResult = checkResponse(response)
        if (!checkResult.success) return checkResult
        val refreshTokenResponse = json.decodeFromString<SuccessfulRefreshTokenResponse>(response!!.text)
        accessToken = refreshTokenResponse.access_token
        if(refreshTokenResponse.refresh_token != null) this.refreshToken = refreshTokenResponse.refresh_token
        return SimpleResult(true)
    }

    private fun createDirectMessageRequst(request: CreateDirectMessageRequest): RESTClient.Response? {
        val url = "$baseUrl/createRoom"
        return createAuthorizedRESTClient().post(url, request, CreateDirectMessageRequest.serializer())
    }

    private fun createRoomRequst(request: CreateRoomRequest): RESTClient.Response? {
        val url = "$baseUrl/createRoom"
        return createAuthorizedRESTClient().post(url, request, CreateRoomRequest.serializer())
    }

    private fun sendMessageRequest(roomId: String, messageId: String,message: String, ): RESTClient.Response? {
        val url = "$baseUrl/rooms/${roomId}/send/m.room.message/${messageId}"
        val request = MessageRequest(body = message)
        return createAuthorizedRESTClient().put(url, request, MessageRequest.serializer())
    }

    private fun loginRequest(userName: String, password: String): RESTClient.Response? {
        val url = "$baseUrl/login"
        val request = LoginRequest(
            identifier = Identifier(
                user = "@$userName:$serverDomain"
            ),
            password = password,
        )
        return createUnauthorizedRESTClient().post(url, request, LoginRequest.serializer())
    }

    private fun logoutRequest(): RESTClient.Response? {
        val url = "$baseUrl/logout"
        return createAuthorizedRESTClient().post(url)
    }

    private fun refreshAccessTokenRequest(refreshToken: String): RESTClient.Response? {
        val url = "$baseUrl/refresh"
        val request = RefreshTokenRequest(
            refresh_token = refreshToken
        )
        return createUnauthorizedRESTClient().post(url, request, RefreshTokenRequest.serializer())
    }

    private fun checkResponse(response: RESTClient.Response?): SimpleResult {
        if(response == null) return SimpleResult(false, Error.SERVER_UNREACHABLE)
        else if(response.code == HttpURLConnection.HTTP_OK) return SimpleResult(true)
        else if(response.code == HttpURLConnection.HTTP_NOT_FOUND) return SimpleResult(false, Error.NOT_FOUND)
        else if(response.code == HttpURLConnection.HTTP_UNAUTHORIZED) return SimpleResult(false, Error.UNAUTHORIZED)
        else if(response.code == HttpURLConnection.HTTP_FORBIDDEN) return SimpleResult(false, Error.FORBIDDEN)
        else if(response.code == HttpURLConnection.HTTP_BAD_METHOD) return SimpleResult(false, Error.UNKNOWN)
        else if (response.code == 429) return SimpleResult(false, Error.TOO_MANY_REQUESTS)
        val logLine = "Unexpected matrix server response:\nCode: ${response.code}\nText: ${response.text}"
        Log.e(javaClass.name, logLine)
        LogSaver(context).save(logLine)
        return SimpleResult(false, Error.UNEXPECTED_RESPONSE)
    }

    private fun createUnauthorizedRESTClient(): RESTClient { return RESTClient(context) }
    private fun createAuthorizedRESTClient(): RESTClient { return RESTClient(context, accessToken) }

    private fun createFullUserId(username: String): String {
        return "@$username:$serverDomain"
    }
}