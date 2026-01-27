package dev.vizualjack.matrix_shortcut.ui.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.data.storage.SettingsStorage
import dev.vizualjack.matrix_shortcut.core.sendToMatrixServer
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme
import kotlinx.serialization.Serializable

@Serializable
data class Settings (val domain:String, val user:String, val accessToken:String, val roomId:String)

@Composable
fun SettingsPage(activity:AppActivity, settings: Settings, backAction:() -> Unit) {
    var domain by rememberSaveable { mutableStateOf(settings.domain) }
    var user by rememberSaveable { mutableStateOf(settings.user) }
    var accessToken by rememberSaveable { mutableStateOf(settings.accessToken) }
    var roomId by rememberSaveable { mutableStateOf(settings.roomId) }

    fun save() {
        Log.i("Settings", "Domain: $domain")
        Log.i("Settings", "User: $user")
        Log.i("Settings", "accessToken: $accessToken")
        Log.i("Settings", "room id: $roomId")
        Log.i("Settings", "Saved")
        SettingsStorage(activity.applicationContext).saveSettings(Settings(domain,user,accessToken,roomId))
        backAction()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Button(onClick = { save() }) {
            Text(text = "Back")
        }
    }

    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EditStringField(text = "server domain", value = domain, onValueChanged = {domain = it}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(5.dp))
        EditStringField(text = "user", value = user, onValueChanged = {user = it}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(5.dp))
        EditStringField(text = "access token", value = accessToken, onValueChanged = {accessToken = it}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(5.dp))
        EditStringField(text = "room id", value = roomId, onValueChanged = {roomId = it}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(10.dp))
        Button(onClick = {
            sendToMatrixServer(Settings(domain,user,accessToken,roomId), "That worked well!", activity, activity.applicationContext)
        }) {
            Text(text = "Test")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    AppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            SettingsPage(AppActivity(), Settings("","","", ""), backAction = {})
        }
    }
}