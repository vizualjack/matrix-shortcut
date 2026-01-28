package dev.vizualjack.matrix_shortcut.ui.screen

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme


@Composable
fun MatrixConfigUI(activity: AppActivity, config: MatrixConfig, backAction:() -> Unit) {
    var serverDomain by rememberSaveable { mutableStateOf(config.serverDomain) }
    var userName by rememberSaveable { mutableStateOf(config.userName) }
    var accessToken by rememberSaveable { mutableStateOf(config.accessToken) }
    var targetRoom by rememberSaveable { mutableStateOf(config.targetRoom) }

    var showLoginPopup by remember { mutableStateOf(true) }

    if (showLoginPopup) {
        LoginPopup(
            serverDomain = "server.domain",
            onSuccessLogin = { loginData ->

            },
            onClose = {
                showLoginPopup = false
            }
        )
    }


    fun save() {
        Log.i("Settings", "Domain: $serverDomain")
        Log.i("Settings", "User: $userName")
        Log.i("Settings", "accessToken: $accessToken")
        Log.i("Settings", "targetRoom: $targetRoom")
        Log.i("Settings", "Saved")
//        SettingsStorage(activity.applicationContext).saveSettings(MatrixConfig(domain,user,accessToken, targetRoom))
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
        EditStringField(text = "server domain", value = serverDomain ?: "", onValueChanged = {serverDomain = it}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(5.dp))

        Text(if (userName != null) "Logged in as " + userName else "Please login")
        Button(onClick = {
            showLoginPopup = true
        }) {
            Text(if (userName != null) "Change" else "Login")
        }
        Spacer(modifier = Modifier.height(5.dp))


//        EditStringField(text = "access token", value = accessToken ?: "", onValueChanged = {accessToken = it}, modifier = Modifier.fillMaxWidth())
//        Spacer(modifier = Modifier.height(5.dp))
        EditStringField(text = "room id",
            value = targetRoom ?: "",
            onValueChanged = {targetRoom = it},
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = {
        }) {
            Text("Select room")
        }

        Spacer(modifier = Modifier.height(10.dp))
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
//            sendToMatrixServer(Settings(domain,user,accessToken,targetRoom), "That worked well!", activity, activity.applicationContext)
        }) {
            Text(text = "Send test message")
        }
    }
}

data class LoginData(
    val userName: String,
    val accessToken: String,
    val refreshToken: String,
)

enum class ClientStatus(val text: String) {
    LOGGING_IN("Logging in..."),
    FAILED("Username or password wasn't correct!"),
    SUCCESS("Successfully logged in!")
}

@Composable
fun LoginPopup(serverDomain: String, onClose: () -> Unit, onSuccessLogin: (loginData: LoginData) -> Unit) {
    var userName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var loggingIn by remember { mutableStateOf(false) }
    var clientStatus by rememberSaveable { mutableStateOf<ClientStatus?>(null) }


    Dialog (
        onDismissRequest = { onClose() }
    ) {
        Surface(
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 20.dp,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Spacer(Modifier.height(2.dp))
                Text (
                    text = "Login to " + serverDomain,
                    fontSize = TextUnit(15f, TextUnitType.Sp)
                )
                Spacer(Modifier.height(10.dp))

                var statusColor = Color.White
                if(clientStatus != null) {
                    if (clientStatus!! == ClientStatus.FAILED) statusColor = Color.Red
                    else if (clientStatus!! == ClientStatus.SUCCESS) statusColor = Color.Green
                }
                Text (
                    text = if(clientStatus != null) clientStatus!!.text else "",
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(10.dp))

                EditStringField(text = "username",
                    value = userName,
                    onValueChanged = {userName = it},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                EditStringField(text = "password",
                    value = password,
                    onValueChanged = {password = it},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Password will be never saved!",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(15.dp))
                Row {
                    Button(
                        enabled = !loggingIn,
                        onClick = {
                            onClose()
                        }
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                    Button(
                        enabled = !loggingIn,
                        onClick = {

                        }
                    ) {
                        Text("Login")
                    }
                }
            }
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
            MatrixConfigUI(AppActivity(), MatrixConfig(), backAction = {})
        }
    }
}