package dev.vizualjack.matrix_shortcut.ui

import android.view.KeyEvent

enum class KeyCode(val value:Int, val text: String) {
    UNKNOWN(KeyEvent.KEYCODE_UNKNOWN, ""),
    VOLUME_UP(KeyEvent.KEYCODE_VOLUME_UP, "Volume Up"),
    VOLUME_DOWN(KeyEvent.KEYCODE_VOLUME_DOWN, "Volume Down");

    override fun toString(): String {
        return text
    }
}