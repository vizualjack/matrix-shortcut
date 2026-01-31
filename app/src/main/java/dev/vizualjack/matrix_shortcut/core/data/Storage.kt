package dev.vizualjack.matrix_shortcut.core.data

import android.content.Context
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.LogSaver
import dev.vizualjack.matrix_shortcut.core.createExceptionLine
import java.io.File
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


class Storage(private val context: Context) {
    private val FILE_NAME = "data.json"

    fun loadData(): StorageData? {
        Log.i(javaClass.name, "loading...")
        val file = File(context.filesDir, FILE_NAME)
        if(!file.exists()) {
            Log.i(javaClass.name, "${FILE_NAME} doesn't exist!")
            return StorageData(null, null)
        }
        try {
            val data = Json.decodeFromString<StorageData>(file.readText())
            Log.i(javaClass.name, "loaded successfully")
            return data
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error on loading: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
        }
        return null
    }


    fun saveData(data: StorageData) {
        Log.i(javaClass.name, "saving...")
        try {
            val file = File(context.filesDir, FILE_NAME)
            file.writeText(Json.encodeToString(data))
            Log.i(javaClass.name, "saved")
        } catch (ex: Exception) {
            val logLine = createExceptionLine("error on saving: ", ex)
            Log.e(javaClass.name, logLine)
            LogSaver(context).save(logLine)
        }
    }
}