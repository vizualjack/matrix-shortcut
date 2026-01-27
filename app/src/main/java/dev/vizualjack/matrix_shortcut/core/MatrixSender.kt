package dev.vizualjack.matrix_shortcut.core

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.ui.screen.Settings
import java.io.DataOutputStream
import java.net.HttpURLConnection
import java.net.URL

private fun doSendToMatrixServer(settings: Settings, message: String, activity: AppActivity? = null, context: Context? = null):Boolean {
    val url = URL("https://${settings.domain}/_matrix/client/r0/rooms/${settings.roomId}/send/m.room.message?access_token=${settings.accessToken}")
    val connection = url.openConnection() as HttpURLConnection
    try {
        connection.requestMethod = "POST"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        connection.doInput = true

        val outputStream = DataOutputStream(connection.outputStream)
        outputStream.writeBytes("{\"msgtype\": \"m.text\",\"body\": \"$message\"}")
        outputStream.flush()
        outputStream.close()
        Log.i("MatrixSender", "Request sent")
        val responseCode = connection.responseCode
        if(responseCode != HttpURLConnection.HTTP_OK) activity?.sendToastText("Matrix server responded with $responseCode")
        return responseCode == HttpURLConnection.HTTP_OK
    } catch (ex:Exception) {
        Log.i("MatrixSender", "Exception thrown while send to matrix server: $ex\n${ex.stackTraceToString()}")
        activity?.sendToastText("Exception thrown while send to matrix server")
        if(context != null) LogSaver(context).save(ex)
        return false
    } finally {
        connection.disconnect()
    }
}

fun sendToMatrixServer(settings: Settings, message:String, activity: AppActivity? = null, context: Context? = null) {
    val checkThread = Thread(Runnable {
        doSendToMatrixServer(settings, message, activity, context)
    })
    checkThread.start()
}