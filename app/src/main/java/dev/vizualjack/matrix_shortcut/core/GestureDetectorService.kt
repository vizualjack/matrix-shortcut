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
import kotlinx.coroutines.launch


class GestureDetectorService : AccessibilityService() {
    private val START_TIMEOUT = 3000

    private var screenOnTime: Long = 0
    private var keyDownKey: Int = 0
    private var keyDownTime: Long = 0
    private var keyUpTime: Long = 0

    private var vibrator: Vibrator? = null

    private var gestureDetector: GestureDetector? = null

    private val screenOnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            try {
                if(intent.action == Intent.ACTION_SCREEN_ON) {
                    onScreenOn()
                }
            } catch (_: Exception) {}
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val filter = IntentFilter()
        filter.addAction(Intent.ACTION_SCREEN_ON)
        registerReceiver(screenOnReceiver, filter)
        initVibratorManager()
    }

    override fun onDestroy() {
        unregisterReceiver(screenOnReceiver)
    }

    private fun onScreenOn() {
        startService(Intent(this, GestureDetectorDataLoader::class.java))
        screenOnTime = System.currentTimeMillis()
        vibrate(30L, 1)
    }

    override fun onKeyEvent(event: KeyEvent?): Boolean {
        if(screenOnTime != 0L && gestureDetector == null && GestureDetectorDataCache.data != null && GestureDetectorDataCache.data!!.gestures != null) {
            gestureDetector = GestureDetector(ArrayList(GestureDetectorDataCache.data!!.gestures!!))
        }
        if(event == null || gestureDetector == null) return false
        val eventTime = System.currentTimeMillis()
        if(isFirstCapturedKeyEvent() && !isFirstKeyInTime(eventTime)) {
            onDoneDetectingGesture()
            return false
        }
        if(event.action == KeyEvent.ACTION_DOWN) onKeyDown(event, eventTime)
        else if(event.action == KeyEvent.ACTION_UP) onKeyUp(event, eventTime)
        return false
    }

    private fun onKeyDown(event: KeyEvent, eventTime: Long) {
        keyDownTime = eventTime
        keyDownKey = event.keyCode
    }

    private fun onKeyUp(event: KeyEvent, eventTime: Long) {
        if(keyDownTime == 0L || event.keyCode != keyDownKey) return
        val keyDownDuration = eventTime - keyDownTime
        gestureDetector!!.addGestureInput(keyDownKey, keyDownDuration.toInt())
        if(gestureDetector!!.hasMatch() || !gestureDetector!!.canStillMatch()) onDoneDetectingGesture()
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
            val intent = Intent(this, GestureDetectorMessageSender::class.java)
            intent.putExtra(GestureDetectorMessageSender.INTENT_MESSAGE_KEY, gestureDetector!!.getMessageOfMatch())
            startService(intent)
        }
        gestureDetector = null
        keyUpTime = 0
        keyDownTime = 0
        screenOnTime = 0
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

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() {}
}