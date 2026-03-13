package dev.vizualjack.matrix_shortcut.core

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import dev.vizualjack.matrix_shortcut.core.data.Gesture
import dev.vizualjack.matrix_shortcut.core.data.GestureInput


class VibrationManager(
    context : Context
) {
    private var vibrator: Vibrator? = null

    init {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibrator = vibratorManager.defaultVibrator
    }

    fun vibrate(durationMillis: Long?, vibrationAmplitude: Int?) {
        if (vibrator == null || durationMillis == null || vibrationAmplitude == null) return
        try { vibrator!!.vibrate(VibrationEffect.createOneShot(durationMillis, vibrationAmplitude)) }
        catch (_: Exception) {}
    }
}