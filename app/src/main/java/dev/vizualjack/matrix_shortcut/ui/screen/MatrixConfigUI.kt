package dev.vizualjack.matrix_shortcut.ui.screen

import android.content.Context
import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.matrix.MatrixClient
import dev.vizualjack.matrix_shortcut.core.matrix.Room
import dev.vizualjack.matrix_shortcut.core.matrix.RoomVisibility
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class ShownPopup {
    NONE,
    LOGIN,
    ROOM_CREATOR,
    ROOM_SELECTOR
}

@Composable
fun MatrixConfigUI(activity: AppActivity?, config: MatrixConfig, onSave:(config: MatrixConfig) -> Unit, onBack:() -> Unit) {
    var serverDomain by rememberSaveable { mutableStateOf(config.serverDomain) }
    var userName by rememberSaveable { mutableStateOf(config.userName) }
    var accessToken by rememberSaveable { mutableStateOf(config.accessToken) }
    var refreshToken by rememberSaveable { mutableStateOf(config.refreshToken) }
    var targetRoom by rememberSaveable { mutableStateOf(config.targetRoom) }

    var shownPopup by remember { mutableStateOf(ShownPopup.NONE) }

    if (shownPopup == ShownPopup.LOGIN) {
        if(serverDomain != null && serverDomain != "") {
            LoginPopup(
                if(activity != null) activity.applicationContext else null,
                serverDomain!!,
                onSuccessLogin = { loginData ->
                    userName = loginData.userName
                    accessToken = loginData.accessToken
                    refreshToken = loginData.refreshToken
                },
                onClose = { shownPopup = ShownPopup.NONE }
            )
        }
        else {
            activity?.sendToastText("Please provide a server domain first")
            shownPopup = ShownPopup.NONE
        }
    }

    if (shownPopup != ShownPopup.NONE && shownPopup != ShownPopup.LOGIN) {
        if(serverDomain == null || serverDomain == "" || accessToken == null || accessToken == "") {
            activity?.sendToastText("Please login first")
            shownPopup = ShownPopup.NONE
        }
        else if (shownPopup == ShownPopup.ROOM_CREATOR) {
            RoomCreatorPopup(
                if(activity != null) activity.applicationContext else null,
                serverDomain!!,
                userName!!,
                accessToken!!,
                refreshToken,
                onRoomCreated = { roomId ->
                    targetRoom = roomId
                },
                onClose = { shownPopup = ShownPopup.NONE }
            )
        } else if (shownPopup == ShownPopup.ROOM_SELECTOR) {
            RoomSelectorPopup(
                if(activity != null) activity.applicationContext else null,
                serverDomain!!,
                userName!!,
                accessToken!!,
                refreshToken,
                onRoomSelected = { roomId ->
                    targetRoom = roomId
                },
                onClose = { shownPopup = ShownPopup.NONE }
            )
        }
    }

    fun save() {
        onSave(MatrixConfig(serverDomain, userName, accessToken, refreshToken, targetRoom))
        onBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopStart
    ) {
        Button(onClick = { onBack() }) {
            Text(text = "Back")
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.TopEnd
    ) {
        Button(onClick = { save() }) {
            Text(text = "Save")
        }
    }


    Column(
        modifier = Modifier
            .padding(10.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        EditStringField(text = "server domain", value = serverDomain ?: "", onValueChanged = {serverDomain = it.trim()}, modifier = Modifier.fillMaxWidth())
        Spacer(modifier = Modifier.height(5.dp))

        Text(if (userName != null) "Logged in as " + userName else "Please login")
        Button(onClick = {
            shownPopup = ShownPopup.LOGIN
        }) {
            Text(if (userName != null) "Change" else "Login")
        }
        Spacer(modifier = Modifier.height(5.dp))


        EditStringField(text = "room id",
            value = targetRoom ?: "",
            onValueChanged = {targetRoom = it.trim()},
            modifier = Modifier.fillMaxWidth()
        )
        Row {
            Button(onClick = {
                shownPopup = ShownPopup.ROOM_CREATOR
            }) {
                Text("Create room")
            }
            Spacer(Modifier.width(15.dp))
            Button(onClick = {
                shownPopup = ShownPopup.ROOM_SELECTOR
            }) {
                Text("Select room")
            }
        }

        Spacer(Modifier.height(10.dp))
        Button(modifier = Modifier.fillMaxWidth(),
            onClick = {
                if(activity == null) return@Button
                if(serverDomain == null || serverDomain == "" || accessToken == null || accessToken == "") {
                    activity.sendToastText("Please login first")
                    return@Button
                }
                if(targetRoom == null || targetRoom == "") {
                    activity.sendToastText("Please select a room")
                    return@Button
                }
                CoroutineScope(Dispatchers.Default).launch {
                    val result = MatrixClient(activity.applicationContext, serverDomain!!, userName!!, accessToken!!, refreshToken).sendMessage(targetRoom!!, "That worked well!")
                    CoroutineScope(Dispatchers.Main).launch {
                        activity.sendToastText(if (result.success) "Successfully sent test message!" else "Error while sending a test message: " + result.error)
                    }
                }
        }) {
            Text(text = "Send test message")
        }
    }
}

data class LoginData(
    val userName: String,
    val accessToken: String,
    val refreshToken: String?,
)

enum class LoginStatus(val text: String) {
    LOGGING_IN("Logging in..."),
    FAILED("Username or password wasn't correct!"),
    SUCCESS("Successfully logged in!"),
    MISSING_DATA("Please provide username and password"),
    SERVER_UNREACHABLE("Server not reachable"),
    TOO_MANY_REQUESTS("Server got too many requests, try again later"),
}

@Composable
fun LoginPopup(context: Context?, serverDomain: String, onClose: () -> Unit, onSuccessLogin: (loginData: LoginData) -> Unit) {
    var userName by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    var loginStatus by rememberSaveable { mutableStateOf<LoginStatus?>(null) }

    fun login() {
        if(context == null) return
        if (userName == "" || password == "") {
            loginStatus = LoginStatus.MISSING_DATA
            return
        }
        loginStatus = LoginStatus.LOGGING_IN
        CoroutineScope(Dispatchers.Default).launch {
            val matrixClient = MatrixClient(context, serverDomain)
            val result = matrixClient.login(userName, password)
            CoroutineScope(Dispatchers.Main).launch {
                if(!result.success) {
                    if(result.error == MatrixClient.Error.SERVER_UNREACHABLE) loginStatus = LoginStatus.SERVER_UNREACHABLE
                    else if(result.error == MatrixClient.Error.TOO_MANY_REQUESTS) loginStatus = LoginStatus.TOO_MANY_REQUESTS
                    else loginStatus = LoginStatus.FAILED
                } else loginStatus = LoginStatus.SUCCESS
            }
            if(!result.success) return@launch
            Thread.sleep(2000)
            CoroutineScope(Dispatchers.Main).launch {
                onSuccessLogin(LoginData(
                    userName,
                    matrixClient.accessToken!!,
                    matrixClient.refreshToken
                ))
                onClose()
            }
        }
    }

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
                if(loginStatus != null) {
                    if (loginStatus!! == LoginStatus.FAILED) statusColor = Color.Red
                    else if (loginStatus!! == LoginStatus.SUCCESS) statusColor = Color.Green
                }
                Text (
                    text = if(loginStatus != null) loginStatus!!.text else "",
                    color = statusColor
                )
                Spacer(modifier = Modifier.height(10.dp))
                EditStringField(text = "username",
                    value = userName,
                    onValueChanged = {userName = it.trim()},
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                EditStringField(text = "password",
                    value = password,
                    onValueChanged = {password = it.trim()},
                    modifier = Modifier.fillMaxWidth(),
                    hidden = true
                )
                Spacer(modifier = Modifier.height(5.dp))
                Text(
                    text = "Password will be never saved!",
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(15.dp))
                Row {
                    Button(
                        enabled = loginStatus != LoginStatus.LOGGING_IN,
                        onClick = {
                            onClose()
                        }
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.fillMaxWidth(0.1f))
                    Button(
                        enabled = loginStatus != LoginStatus.LOGGING_IN,
                        onClick = {
                            login()
                        }
                    ) {
                        Text("Login")
                    }
                }
            }
        }
    }
}

enum class CreateStatus(val text: String) {
    CREATING("Creating room..."),
    UNAUTHORIZED("Access token invalid, please login again!"),
    SUCCESS("Successfully created room!"),
    MISSING_DATA("Please provide all data!"),
    SERVER_UNREACHABLE("Server not reachable"),
    TOO_MANY_REQUESTS("Server got too many requests, try again later"),
    UNKNOWN("Unknown error!"),
}

@Composable
fun RoomCreatorPopup(context: Context?, serverDomain: String, userName: String, accessToken: String, refreshToken: String?, onClose: () -> Unit, onRoomCreated: (roomId: String) -> Unit) {
    var roomName by rememberSaveable { mutableStateOf("") }
    var inviteUserName by rememberSaveable { mutableStateOf("") }
    var directMessageRoom by rememberSaveable { mutableStateOf(false) }

    var createStatus by rememberSaveable { mutableStateOf<CreateStatus?>(null) }

    fun create() {
        if(context == null) return
        roomName = roomName.trim()
        if (inviteUserName == "") {
            createStatus = CreateStatus.MISSING_DATA
            return
        }
        if (!directMessageRoom && roomName == "") {
            createStatus = CreateStatus.MISSING_DATA
            return
        }
        createStatus = CreateStatus.CREATING
        CoroutineScope(Dispatchers.Default).launch {
            val matrixClient = MatrixClient(context, serverDomain, userName, accessToken, refreshToken)
            val result = if(directMessageRoom) matrixClient.createPrivateChat(inviteUserName) else matrixClient.createRoom(roomName, inviteUserName)
            CoroutineScope(Dispatchers.Default).launch {
                if(!result.success) {
                    if(result.error == MatrixClient.Error.SERVER_UNREACHABLE) createStatus = CreateStatus.SERVER_UNREACHABLE
                    else if(result.error == MatrixClient.Error.TOO_MANY_REQUESTS) createStatus = CreateStatus.TOO_MANY_REQUESTS
                    else if(result.error == MatrixClient.Error.UNAUTHORIZED) createStatus = CreateStatus.UNAUTHORIZED
                    else createStatus = CreateStatus.UNKNOWN
                } else createStatus = CreateStatus.SUCCESS
            }
            Thread.sleep(2000)
            withContext(Dispatchers.Main) {
                onRoomCreated(result.value!!)
                onClose()
            }
        }
    }

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
                Text (
                    text = "Create private room / direct message", // + serverDomain,
                    fontSize = TextUnit(15f, TextUnitType.Sp)
                )
                var statusColor = Color.White
                if(createStatus != null && createStatus != CreateStatus.CREATING) {
                    if (createStatus!! != CreateStatus.SUCCESS) statusColor = Color.Red
                    else statusColor = Color.Green
                }
                Text (
                    text = if(createStatus != null) createStatus!!.text else "",
                    color = statusColor
                )
                Row {
                    Text("Private chat", Modifier.align(Alignment.CenterVertically).clickable { directMessageRoom = !directMessageRoom })
                    Checkbox(directMessageRoom, {directMessageRoom = it})
                }
                if(!directMessageRoom) {
                    EditStringField(text = "room name",
                        value = roomName,
                        onValueChanged = {roomName = it},
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(5.dp))
                EditStringField(text = "user to invite",
                    value = inviteUserName,
                    onValueChanged = {inviteUserName = it.trim()},
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Only username is enough,\nif the user is on the same server", textAlign = TextAlign.Center, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Spacer(Modifier.height(20.dp))
                Row {
                    Button({onClose()}) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button({create()}) {
                        Text("Create")
                    }
                }
            }
        }
    }
}

enum class SelectStatus(val text: String) {
    GETTING_DATA("Getting data..."),
    UNAUTHORIZED("Access token invalid, please login again!"),
    SERVER_UNREACHABLE("Server not reachable"),
    TOO_MANY_REQUESTS("Server got too many requests, try again later"),
    NEED_TO_SELECT_A_ROOM("Please select a room"),
    UNKNOWN("Unknown error!"),
}

@Composable
fun RoomSelectorPopup(context: Context?, serverDomain: String, userName: String, accessToken: String, refreshToken: String?, onClose: () -> Unit, onRoomSelected: (roomId: String) -> Unit) {
    var selectedRoom by rememberSaveable { mutableStateOf<Room?>(null) }
    var rooms by rememberSaveable { mutableStateOf(listOf<Room>()) }

    var selectStatus by rememberSaveable { mutableStateOf<SelectStatus?>(null) }

    fun loadRooms() {
        if(context == null) return
        selectStatus = SelectStatus.GETTING_DATA
        CoroutineScope(Dispatchers.Default).launch {
            val matrixClient = MatrixClient(context, serverDomain, userName, accessToken, refreshToken)
            val result = matrixClient.getJoinedRooms()
            CoroutineScope(Dispatchers.Main).launch {
                if(!result.success) {
                    if(result.error == MatrixClient.Error.SERVER_UNREACHABLE) selectStatus = SelectStatus.SERVER_UNREACHABLE
                    else if(result.error == MatrixClient.Error.TOO_MANY_REQUESTS) selectStatus = SelectStatus.TOO_MANY_REQUESTS
                    else if(result.error == MatrixClient.Error.UNAUTHORIZED) selectStatus = SelectStatus.UNAUTHORIZED
                    else selectStatus = SelectStatus.UNKNOWN
                } else {
                    selectStatus = null
                    rooms = result.value!!
                }
            }
        }
    }

    fun refresh() {
        if(context == null) return
        selectStatus = SelectStatus.GETTING_DATA
        CoroutineScope(Dispatchers.Default).launch {
            val matrixClient = MatrixClient(context, serverDomain, userName, accessToken, refreshToken)
            val result = matrixClient.acceptAllInvites()
            CoroutineScope(Dispatchers.Main).launch {
                if(!result.success) {
                    if(result.error == MatrixClient.Error.SERVER_UNREACHABLE) selectStatus = SelectStatus.SERVER_UNREACHABLE
                    else if(result.error == MatrixClient.Error.TOO_MANY_REQUESTS) selectStatus = SelectStatus.TOO_MANY_REQUESTS
                    else if(result.error == MatrixClient.Error.UNAUTHORIZED) selectStatus = SelectStatus.UNAUTHORIZED
                    else selectStatus = SelectStatus.UNKNOWN
                    return@launch
                }
                selectStatus = null
            }
            if(result.success) loadRooms()
        }
    }

    fun select() {
        if(selectedRoom == null) {
            selectStatus = SelectStatus.NEED_TO_SELECT_A_ROOM
            return
        }
        onRoomSelected(selectedRoom!!.roomId)
        onClose()
    }

    LaunchedEffect(Unit) {
        loadRooms()
    }

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
                Text (
                    text = "Room selector",
                    fontSize = TextUnit(15f, TextUnitType.Sp)
                )
//                Spacer(Modifier.height(5.dp))

                var statusColor = Color.White
                if(selectStatus != null && selectStatus != SelectStatus.GETTING_DATA) statusColor = Color.Red
                Text (
                    text = if(selectStatus != null) selectStatus!!.text else "",
                    color = statusColor
                )

                Row {
                    RoomDropDown(selectedRoom, rooms, {room: Room -> selectedRoom = room }, Modifier.fillMaxWidth(0.85f))
                    Spacer(Modifier.width(10.dp))
                    IconButton({refresh()}, Modifier.align(Alignment.CenterVertically)) { Icon(Icons.Filled.Refresh, "Refresh") }
                }
                Spacer(Modifier.height(5.dp))
                Text (text = "Refreshing also accepts invites", color = Color.Gray)
                Spacer(Modifier.height(10.dp))
                Row {
                    Button({onClose()}) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Button({select()}) {
                        Text("Select")
                    }
                }

            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomDropDown(
    selectedRoom: Room?,
    rooms: List<Room>,
    onRoomChanged: (Room) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                expanded = !expanded
            }
        ) {
            TextField(
                value = if(selectedRoom == null) "Select a room" else selectedRoom.displayName,
                onValueChange = {},
                readOnly = true,
                textStyle = MaterialTheme.typography.bodyMedium,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                for (room in rooms) {
                    DropdownMenuItem(
                        text = { Text(text = room.displayName) },
                        onClick = {
                            onRoomChanged(room)
                            expanded = false
                        }
                    )
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
            MatrixConfigUI(null, MatrixConfig("", "", "", "", ""), onSave = {}, onBack = {})
        }
    }
}