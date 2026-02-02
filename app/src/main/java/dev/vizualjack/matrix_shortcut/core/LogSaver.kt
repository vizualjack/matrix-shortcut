package dev.vizualjack.matrix_shortcut.core

import android.content.Context
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class LogSaver(private val context: Context)  {

    private val FILE_NAME = "log_%DATE%.txt"

    fun save(text: String) {
        val filesDir = context.getExternalFilesDir(null) ?: return
        val file = File(filesDir, FILE_NAME.replace("%DATE%", getDay()))
        file.appendText("${getTimestamp()}: $text\n")
    }

    private fun getTimestamp(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        return sdf.format(Date(currentTimeMillis))
    }

    private fun getDay(): String {
        val currentTimeMillis = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return sdf.format(Date(currentTimeMillis))
    }
}