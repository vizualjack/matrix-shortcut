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
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    var storageData: StorageData? = null
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
                sendToastText("Exported data successfully!")
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
                        storageData = StorageData()
                        val loadedStorageData = Json.decodeFromString<StorageData>(outputStream.readBytes().decodeToString())
                        if(loadedStorageData.gestures != null) storageData!!.gestures = loadedStorageData.gestures
                        if(loadedStorageData.matrixConfig != null) storageData!!.matrixConfig = loadedStorageData.matrixConfig
                        Storage(applicationContext).saveData(storageData!!)
                    }
                    refreshContent()
                    Log.i("MainActivity","Importing successful!")
                    sendToastText("Imported data successfully!")
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
        setContentToAppUI()
        CoroutineScope(Dispatchers.IO).launch {
            loadData()
            withContext(Dispatchers.Main) {
                refreshContent()
            }
        }
    }

    private fun refreshContent() { setContentToAppUI() }

    private fun setContentToAppUI() {
        setContent {
            AppTheme {
                AppUI(this)
            }
        }
    }

    private fun loadData() {
        storageData = Storage(applicationContext).loadData()
        if(storageData == null) {
            loadingStatus = LoadingStatus.ERROR
            return
        }
        loadingStatus = LoadingStatus.LOADED
    }

    private fun saveData() {
        if(storageData == null) return
        Storage(applicationContext).saveData(storageData!!)
    }
}