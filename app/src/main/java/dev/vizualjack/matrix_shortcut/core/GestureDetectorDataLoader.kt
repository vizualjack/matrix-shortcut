package dev.vizualjack.matrix_shortcut.core

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import dev.vizualjack.matrix_shortcut.core.data.Storage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GestureDetectorDataLoader: Service() {
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        serviceScope.launch {
            try {
                val result = loadData()
                log("loaded data: $result")
            } catch (ex: Exception) {
                log("Couldn't load data. reason: $ex")
            }
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        serviceScope.cancel()
        super.onDestroy()
    }

    private fun loadData(): Boolean {
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
        GestureDetectorDataCache.data = storageData
        return true
    }

    override fun onBind(p0: Intent?): IBinder? = null

    private fun log(message: String) {
        Log.i(javaClass.name, message)
    }
}