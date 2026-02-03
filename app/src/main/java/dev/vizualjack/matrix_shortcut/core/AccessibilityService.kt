package dev.vizualjack.matrix_shortcut.core

import android.accessibilityservice.AccessibilityService
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.KeyEvent
import android.view.accessibility.AccessibilityEvent
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.data.Storage
import dev.vizualjack.matrix_shortcut.core.matrix.MatrixClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class AccessibilityService : AccessibilityService() {
    private val START_TIMEOUT = 3000
    private val workScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val logScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private var screenOnTime: Long = 0
    private var keyDownKey: Int = 0
    private var keyDownTime: Long = 0
    private var keyUpTime: Long = 0

    private var vibrator: Vibrator? = null
    private var logSaver: LogSaver? = null

    private var gestureDetector: GestureDetector? = null
    private var matrixConfig: MatrixConfig? = null

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if(intent.action == Intent.ACTION_SCREEN_ON) {
                    onScreenOn()
                }
            } catch (ex: Exception) {
                val logLine = createExceptionLine("error at screenOnReceiver.onReceive: ", ex)
                log(logLine)
            }
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        logSaver = LogSaver(applicationContext)
        log("onServiceConnected")
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenOnReceiver, filter)
        log("registered screen on receiver")
        initVibratorManager()
        log("vibrator manager successfully initialized: " + (vibrator != null).toString())
        log("onServiceConnected - done")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        log("onUnbind")
        return false
    }

    override fun onInterrupt() {
        log("onInterrupt")
    }

    override fun onDestroy() {
        log("onDestroy")
        unregisterReceiver(screenOnReceiver)
        log("unregistered screen on receiver")
    }

    private fun onScreenOn() {
        workScope.launch {
            if(reload()) log("loaded newest storage data")
            else log("couldn't load newest storage data")
            screenOnTime = System.currentTimeMillis()
            log("set screen on time: $screenOnTime")
            vibrate(100L, 1)
            log("screen on vibration fired")
        }
    }

    private fun reload(): Boolean {
        val storageData = Storage(applicationContext).loadData()
        if(storageData == null) {
            log("missing storage data")
            return false
        } else if (storageData.gestures == null) {
            log("missing gestures data")
            return false
        } else if (storageData.matrixConfig == null) {
            log("missing matrix config data")
            return false
        }
        gestureDetector = GestureDetector(ArrayList(storageData.gestures!!))
        matrixConfig = storageData.matrixConfig
        return true
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        workScope.launch {
            if(event == null || gestureDetector == null || matrixConfig == null) return@launch
            log("onKeyEvent started")
            val eventTime = System.currentTimeMillis()
            if(isFirstCapturedKeyEvent() && !isFirstKeyInTime(eventTime)) {
                onDoneDetectingGesture()
                log("onKeyEvent started to late, finished detecting")
                return@launch
            }
            if(event.action == KeyEvent.ACTION_DOWN) onKeyDown(event, eventTime)
            else if(event.action == KeyEvent.ACTION_UP) onKeyUp(event, eventTime)
            log("onKeyEvent finished")
        }
        return false
    }

    private fun onKeyDown(event: KeyEvent, eventTime: Long) {
        keyDownTime = eventTime
        keyDownKey = event.keyCode
        log("onKeyDown")
    }

    private fun onKeyUp(event: KeyEvent, eventTime: Long) {
        if(keyDownTime == 0L || event.keyCode != keyDownKey) return
        val keyDownDuration = eventTime - keyDownTime
        gestureDetector!!.addGestureInput(keyDownKey, keyDownDuration.toInt())
        if(gestureDetector!!.hasMatch() || !gestureDetector!!.canStillMatch()) onDoneDetectingGesture()
        log("onKeyUp")
    }

    private fun isFirstKeyInTime(eventTime: Long): Boolean {
        val neededTime = eventTime - screenOnTime
        return neededTime <= START_TIMEOUT
    }

    private fun isFirstCapturedKeyEvent(): Boolean {
        return keyDownTime == 0L && keyUpTime == 0L
    }

    private fun onDoneDetectingGesture() {
        if(gestureDetector!!.hasMatch()) {
            sendMessage(gestureDetector!!.getMessageOfMatch()!!)
            vibrate(250L, 1)
        }
        gestureDetector = null
        matrixConfig = null
        keyUpTime = 0
        keyDownTime = 0
        screenOnTime = 0
    }

    private fun sendMessage(message: String) {
        if(matrixConfig == null || !isMatrixConfigComplete(matrixConfig!!)) return
        val matrixClient = MatrixClient(applicationContext, matrixConfig!!.serverDomain!!, matrixConfig!!.userName!!, matrixConfig!!.accessToken!!, matrixConfig!!.refreshToken)
        val result = matrixClient.sendMessage(matrixConfig!!.targetRoom!!, message)
        if(result.success) log("sending message to matrix server was successful!")
        else log("sending message to matrix server was NOT successful! error: ${result.error}")
    }

    private fun isMatrixConfigComplete(matrixConfig: MatrixConfig): Boolean {
        if(matrixConfig.serverDomain == null) {
            log("server domain in matrix config is missing!")
            return false
        } else if(matrixConfig.userName == null) {
            log("user name in matrix config is missing!")
            return false
        } else if(matrixConfig.accessToken == null) {
            log("access token in matrix config is missing!")
            return false
        } else if(matrixConfig.targetRoom == null) {
            log("target room in matrix config is missing!")
            return false
        }
        return true
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
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error in vibrate: ", ex)
            log(logLine)
        }
    }

    private fun log(message: String) {
        logScope.launch {
            Log.i(javaClass.name, message)
            logSaver?.save(message)
        }
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
}