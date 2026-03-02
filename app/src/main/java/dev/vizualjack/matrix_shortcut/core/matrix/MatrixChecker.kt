package dev.vizualjack.matrix_shortcut.core.matrix

import android.content.Context
import kotlinx.serialization.json.JsonDecoder

class MatrixChecker(val context: Context) {
    enum class CheckResult {
        OK,
        UNREACHABLE,
        NO_MATRIX_INSTANCE
    }

    fun checkInstance(serverDomain: String): CheckResult {
        val response = RESTClient(context).get("https://$serverDomain/_matrix/client/versions")
        if(response == null) return CheckResult.UNREACHABLE
        if(!response.text.contains("versions")) return CheckResult.NO_MATRIX_INSTANCE
        return CheckResult.OK
    }
}