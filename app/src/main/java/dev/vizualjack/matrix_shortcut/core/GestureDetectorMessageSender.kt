package dev.vizualjack.matrix_shortcut.core

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.data.Storage
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
    private var vibrator: Vibrator? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if(intent == null) return START_NOT_STICKY
        serviceScope.launch {
            try {
                val result = sendMessage(intent.getStringExtra(INTENT_MESSAGE_KEY)!!)
                vibrate(100L, 1)
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
        val matrixConfig = GestureDetectorDataCache.data?.matrixConfig
        if(matrixConfig == null || !isMatrixConfigComplete(matrixConfig)) return false
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

    private fun initVibratorManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        }
        else vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun vibrate(durationMillis: Long, vibrationAmplitude: Int) {
        try {
            if (vibrator == null) initVibratorManager()
            if(vibrator != null) vibrator!!.vibrate(VibrationEffect.createOneShot(durationMillis, vibrationAmplitude))
        } catch (_: Exception) {}
    }

    private fun log(message: String) {
        Log.i(javaClass.name, message)
    }

    override fun onBind(p0: Intent?): IBinder? = null
}