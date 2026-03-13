package dev.vizualjack.matrix_shortcut.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.matrix.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GestureDetectorMessageSender: Service() {

    companion object {
        const val INTENT_MESSAGE_KEY = "message"
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var vibrationManager: VibrationManager? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null) return START_NOT_STICKY
        serviceScope.launch {
            try {
                vibrationManager = VibrationManager(applicationContext)
                val result = sendMessage(intent.getStringExtra(INTENT_MESSAGE_KEY)!!)
                vibrate()
                log("sent message: $result")
            } catch (ex: Exception) {
                log("Couldn't send message. reason: $ex")
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun sendMessage(message: String): Boolean {
        if(GestureDetectorDataCache.data == null ||
            GestureDetectorDataCache.data!!.settings == null) return false
        val matrixConfig = GestureDetectorDataCache.data?.settings!!.matrixConfig
        if(!isMatrixConfigComplete(matrixConfig)) return false
        val matrixClient = MatrixClient(applicationContext, matrixConfig.serverDomain!!, matrixConfig.userName!!, matrixConfig.accessToken!!, matrixConfig.refreshToken)
        matrixClient.sendMessage(matrixConfig.targetRoom!!, message)
        return true
    }

    private fun isMatrixConfigComplete(matrixConfig: MatrixConfig): Boolean {
        return  matrixConfig.serverDomain != null &&
                matrixConfig.userName != null &&
                matrixConfig.accessToken != null &&
                matrixConfig.targetRoom != null
    }

    private fun vibrate() {
        if(GestureDetectorDataCache.data == null) return
        if(GestureDetectorDataCache.data!!.settings == null) return
        if(GestureDetectorDataCache.data!!.settings!!.vibrationConfig.onGestureDetected == null) return
        val vibrationSettings = GestureDetectorDataCache.data!!.settings!!.vibrationConfig.onGestureDetected!!
        vibrationManager?.vibrate(vibrationSettings.durationMillis, vibrationSettings.amplitude)
    }

    private fun log(message: String) {
        Log.i(javaClass.name, message)
    }

    override fun onBind(p0: Intent?): IBinder? = null
}