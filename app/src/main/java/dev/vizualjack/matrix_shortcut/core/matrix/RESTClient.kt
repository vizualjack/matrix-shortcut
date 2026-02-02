package dev.vizualjack.matrix_shortcut.core.matrix

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.LogSaver
import dev.vizualjack.matrix_shortcut.core.createExceptionLine
import kotlinx.serialization.SerializationStrategy
import kotlinx.serialization.json.Json
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

class RESTClient(val context: Context, val bearerToken: String? = null) {
    val TIMEOUT = 5000

    data class Response(val code: Int, val text: String)
    enum class RequestMethod(val value: String) {
        POST("POST"),
        GET("GET"),
        PUT("PUT")
    }

    fun post(url: String): Response? {
        val connection = createConnection(url, RequestMethod.POST) ?: return null
        var response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    fun <T> post(url: String, request: T, requestSerializer: SerializationStrategy<T>): Response? {
        val connection = createConnection(url, RequestMethod.POST) ?: return null
        var response: Response? = null
        val needToReceive: Boolean
        needToReceive = sendRequest(connection, request, requestSerializer)
        if(needToReceive) response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    fun <T> put(url: String, request: T? = null, requestSerializer: SerializationStrategy<T>?): Response? {
        val connection = createConnection(url, RequestMethod.PUT) ?: return null
        var response: Response? = null
        val needToReceive: Boolean
        if(request != null && requestSerializer != null) needToReceive = sendRequest<T>(connection, request, requestSerializer)
        else needToReceive = true
        if(needToReceive) response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    fun get(url: String): Response? {
        val connection = createConnection(url, RequestMethod.GET) ?: return null
        val response = receiveResponse(connection)
        connection.disconnect()
        return response
    }

    private fun <T> sendRequest(connection: HttpURLConnection, request: T, requestSerializer: SerializationStrategy<T>): Boolean {
        try {
            val json = Json {encodeDefaults = true}
            val requestBodyJson: String = json.encodeToString(requestSerializer, request)
            val outputStream = DataOutputStream(connection.outputStream)
            outputStream.writeBytes(requestBodyJson)
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
            val inputStream = if(connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val content = inputStream.readBytes()
            response = Response(connection.responseCode, content.decodeToString())
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while receiving response: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
        }
        return response
    }

    private fun createConnection(url: String, method: RequestMethod): HttpURLConnection? {
        try {
            val connection = URL(url).openConnection() as HttpURLConnection
            connection.connectTimeout = TIMEOUT
            connection.readTimeout = TIMEOUT
            if(method != RequestMethod.GET) connection.doOutput = true
            else connection.doInput = true
            connection.requestMethod = method.value
            if(method == RequestMethod.POST) connection.setRequestProperty("Content-Type", "application/json")
            if(bearerToken != null) connection.setRequestProperty("Authorization", "Bearer $bearerToken")
            return connection
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while creating connection: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
            return null
        }
    }
}