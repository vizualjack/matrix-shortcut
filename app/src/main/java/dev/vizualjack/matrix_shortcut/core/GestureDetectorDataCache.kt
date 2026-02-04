package dev.vizualjack.matrix_shortcut.core

import dev.vizualjack.matrix_shortcut.core.data.StorageData

object GestureDetectorDataCache {
    @Volatile
    var data: StorageData? = null
}