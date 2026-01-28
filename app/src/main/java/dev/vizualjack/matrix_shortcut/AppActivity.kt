package dev.vizualjack.matrix_shortcut

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import dev.vizualjack.matrix_shortcut.core.data.Storage
import dev.vizualjack.matrix_shortcut.core.data.StorageData
import dev.vizualjack.matrix_shortcut.core.LogSaver
import dev.vizualjack.matrix_shortcut.ui.AppUI
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.lang.Exception


class AppActivity : ComponentActivity() {
    private val exportActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i("MainActivity","Exporting...")
                val data: Intent? = result.data
                val uri = data?.data ?:return@registerForActivityResult
                contentResolver.openOutputStream(uri)?.use { outputStream ->
                    val storageData = Storage(applicationContext).loadData()
                    val appDataAsString = Json.encodeToString(storageData)
                    outputStream.write(appDataAsString.toByteArray())
                }
                Log.i("MainActivity","Exporting successful!")
            }
        }

    private val importActivity =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                Log.i("MainActivity","Importing...")
                val data: Intent? = result.data
                val uri = data?.data ?:return@registerForActivityResult
                try {
                    contentResolver.openInputStream(uri)?.use { outputStream ->
                        val storageData = Json.decodeFromString<StorageData>(outputStream.readBytes().decodeToString())
                        if(storageData.gestures != null && storageData.matrixConfig != null) Storage(applicationContext).saveData(storageData)
                    }
                    Log.i("MainActivity","Importing successful!")
                    setContent {
                        AppTheme {
                            AppUI(this)
                        }
                    }
                } catch (ex:Exception) {
                    val logLine = "error while importing: $ex\n${ex.stackTraceToString()}"
                    Log.e(javaClass.name,logLine)
                    LogSaver(applicationContext).save(logLine)
                }
            }
        }

    fun exportRequest() {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        exportActivity.launch(intent)
    }

    fun importRequest() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "text/plain"
        importActivity.launch(intent)
    }

    fun sendToastText(text:String) {
        runOnUiThread(Runnable { Toast.makeText(applicationContext, text, Toast.LENGTH_SHORT).show() })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                AppUI(this)
            }
        }
    }
}