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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runInterruptible
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import java.lang.Exception


class AppActivity : ComponentActivity() {
    enum class LoadingStatus {
        LOADING,
        LOADED,
        ERROR
    }

    var loadingStatus: LoadingStatus = LoadingStatus.LOADING
    private var storageData: StorageData = StorageData()
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
                    refreshContent()
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

    override fun onStop() {
        super.onStop()
        CoroutineScope(Dispatchers.IO).launch {
            saveData()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshContent()
        CoroutineScope(Dispatchers.IO).launch {
            loadData()
            withContext(Dispatchers.Main) {
                refreshContent()
            }
        }
    }

    private fun refreshContent() {
        setContent {
            AppTheme {
                AppUI(this)
            }
        }
    }

    private fun loadData() {
//        val loadedData = Storage(applicationContext).loadData()
//        if(loadedData == null) {
//            loadingStatus = LoadingStatus.ERROR
//            return
//        }
//        storageData = loadedData
        Thread.sleep(3000)
        loadingStatus = LoadingStatus.ERROR
    }

    private fun saveData() {
        Storage(applicationContext).saveData(storageData)
    }
}