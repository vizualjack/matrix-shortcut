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
import dev.vizualjack.matrix_shortcut.core.data.Storage
import java.lang.Exception

class AccessibilityService : AccessibilityService() {
    var screenOnTime: Long = 0
    var checkForGesture: Boolean = true
    var isTracking: Boolean = false
    val START_TIMEOUT = 2000
    val KEYUP_TO_KEYDOWN_TIMEOUT = 400
    val MATCH_CHECK_INTERVAL = 100
    var currentKeyCode: Int = 0
    var keyDownTime: Long = 0
    var keyUpTime: Long = 0
    var lastKeyAction = KeyEvent.ACTION_UP
    var vibrator: Vibrator? = null
    var logSaver: LogSaver? = null
    var gestureDetector: GestureDetector? = null


    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter()
        filter.addAction("android.intent.action.SCREEN_OFF")
        filter.addAction("android.intent.action.SCREEN_ON")
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                try {
                    if(intent.action.toString() == "android.intent.action.SCREEN_ON") {
                        screenOnTime = System.currentTimeMillis()
                        Log.i(javaClass.name, "screenOnTime: ${screenOnTime}")
                        reset()
                        vibrate(250L,VibrationEffect.EFFECT_DOUBLE_CLICK)
                    }
                } catch (ex: Exception) {
                    val logLine = createExceptionLine("error in BroadcastReceiver.onReceive: ", ex)
                    Log.e(javaClass.name, logLine)
                    logSaver?.save(logLine)
                }
            }
        }
        registerReceiver(receiver, filter)
        logSaver = LogSaver(applicationContext)
        Log.i(javaClass.name, "service connected")
    }

    override fun onUnbind(intent: Intent?): Boolean {
        Log.i(javaClass.name, "service disconnected")
        return super.onUnbind(intent)
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}

    override fun onInterrupt() {
        Log.i(javaClass.name, "interrupted")
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        try {
            if(event == null || !checkForGesture && !isTracking) return super.onKeyEvent(event)
            if(!isTracking) checkForStartTracking(event)
            if(!isTracking) return super.onKeyEvent(event)
            if(event.action == KeyEvent.ACTION_DOWN) onKeyDown(event.keyCode)
            else onKeyUp()
            lastKeyAction = event.action
            return super.onKeyEvent(event)
        } catch (ex: Exception) {
            val logLine = createExceptionLine("exception in onKeyEvent: ", ex)
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
            return super.onKeyEvent(event)
        }
    }

    private fun reset() {
        gestureDetector = null
        checkForGesture = true
        keyUpTime = 0
    }

    private fun reloadGestures() {
        val storageData = Storage(applicationContext).loadData()
        if(storageData == null) {
            val logLine = "no storage data available while reloading gestures!"
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
            return
        } else if(storageData.gestures == null) {
            val logLine = "no gestures availabe while reloading gestures!"
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
            return
        }
        gestureDetector = GestureDetector(ArrayList(storageData.gestures!!))
        Log.i(javaClass.name, "gestures reloaded")
    }

    private fun checkForStartTracking(event: KeyEvent) {
        checkForGesture = false
        if(event.action == KeyEvent.ACTION_UP) {
            isTracking = false
            return
        }
        var timeBetween = System.currentTimeMillis() - screenOnTime
        Log.i(javaClass.name, "timeBetween: $timeBetween")
        isTracking = timeBetween <= START_TIMEOUT
        if(isTracking) {
            Log.i(javaClass.name, "tracking started...")
            reloadGestures()
            startCheckDoneThread()
        }
    }

    private fun checkForTimeout() {
        if(keyUpTime <= 0) return
        val keyUpToKeyDownTime = keyDownTime - keyUpTime
        Log.i(javaClass.name, "keyUpToKeyDownTime: $keyUpToKeyDownTime")
        if (keyUpToKeyDownTime <= KEYUP_TO_KEYDOWN_TIMEOUT) return
        Log.i(javaClass.name, "keyup to keydown timeout reached!")
        isTracking = false
    }

    private fun onKeyDown(keyCode: Int) {
        keyDownTime = System.currentTimeMillis()
        currentKeyCode = keyCode
        checkForTimeout()
    }

    private fun onKeyUp() {
        if(gestureDetector == null) {
            val logLine = "no gesture detector available!"
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
            return
        }
        keyUpTime = System.currentTimeMillis()
        val duration = keyUpTime - keyDownTime
        Log.i(javaClass.name, "key $currentKeyCode pressed for $duration")
        gestureDetector!!.addGestureInput(currentKeyCode, duration.toInt())
        if(!gestureDetector!!.hasUniqueMatch()) return
        val actionName = gestureDetector!!.getUniqueMatchActionName() ?: return
        sendActionName(actionName)
        Log.i(javaClass.name, "found match: $actionName")
        isTracking = false
    }

    private fun startCheckDoneThread() {
        try {
            val checkThread = Thread(Runnable {
                try {
                    Log.i(javaClass.name, "checkThread started...")
                    if(gestureDetector == null) {
                        val logLine = "no gesture detector available in checkThread!"
                        Log.e(javaClass.name, logLine)
                        logSaver?.save(logLine)
                        return@Runnable
                    }
                    while(true) {
                        Thread.sleep(MATCH_CHECK_INTERVAL.toLong())
                        if(!isTracking) {
                            Log.i(javaClass.name, "tracking already done")
                            return@Runnable
                        }
                        Log.i(javaClass.name, "check if done")
                        val timeSinceLastKeyUp = System.currentTimeMillis() - keyUpTime
                        if (timeSinceLastKeyUp > KEYUP_TO_KEYDOWN_TIMEOUT && lastKeyAction == KeyEvent.ACTION_UP) {
                            isTracking = false
                            Log.i(javaClass.name, "done! check for match...")
                            val actionName = gestureDetector!!.getUniqueMatchActionName()
                            if (actionName != null) sendActionName(actionName)
                            return@Runnable
                        }
                    }
                } catch (ex: Exception) {
                    val logLine = createExceptionLine("error in checkThread: ", ex)
                    Log.e(javaClass.name, logLine)
                    logSaver?.save(logLine)
                }
            })
            checkThread.start()
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error in startCheckDoneThread: ", ex)
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
        }
    }

    private fun sendActionName(actionName: String) {
        Log.i(javaClass.name, "sending action: $actionName")
        try {
            vibrate(250L,VibrationEffect.DEFAULT_AMPLITUDE)
            val storageData = Storage(applicationContext).loadData()
            if (storageData == null) {
                val logLine = "no storage data available while sending action!"
                Log.e(javaClass.name, logLine)
                logSaver?.save(logLine)
                return
            }
            val settings = storageData.matrixConfig ?: return
//            sendToMatrixServer(settings, actionName, context = applicationContext)
            Log.i(javaClass.name, "sent action: $actionName")
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error while sending action: ", ex)
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
        }
    }

    private fun initVibratorManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibrator = vibratorManager.defaultVibrator
        }
        else vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private fun vibrate(durationMillis: Long, vibrationEffect: Int) {
        try {
            if (vibrator == null) initVibratorManager()
            if(vibrator != null) vibrator!!.vibrate(VibrationEffect.createOneShot(durationMillis,vibrationEffect))
        } catch (ex: Exception) {
            val logLine = createExceptionLine("exception in vibrate: ", ex)
            Log.e(javaClass.name, logLine)
            logSaver?.save(logLine)
        }
    }
}