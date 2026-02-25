package dev.vizualjack.matrix_shortcut.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.autofill.AutofillType
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.R
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.matrix.MatrixClient
import dev.vizualjack.matrix_shortcut.core.matrix.Room
import dev.vizualjack.matrix_shortcut.ui.components.TextButton
import dev.vizualjack.matrix_shortcut.ui.components.Dropdown
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.components.Popup
import dev.vizualjack.matrix_shortcut.ui.components.Section
import dev.vizualjack.matrix_shortcut.ui.components.Text
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun MatrixConfigUI(activity: AppActivity?, config: MatrixConfig, onSave:(config: MatrixConfig) -> Unit, onBack:() -> Unit) {
    var serverDomain by rememberSaveable { mutableStateOf(config.serverDomain) }
    var userName by rememberSaveable { mutableStateOf(config.userName) }
    var accessToken by rememberSaveable { mutableStateOf(config.accessToken) }
    var refreshToken by rememberSaveable { mutableStateOf(config.refreshToken) }
    var targetRoom by rememberSaveable { mutableStateOf(config.targetRoom) }

    var shownPopup by remember { mutableStateOf(ShownPopup.NONE) }

    fun save() {
        onSave(MatrixConfig(serverDomain, userName, accessToken, refreshToken, targetRoom))
        onBack()
    }

    fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            if(activity == null) return@launch
            val matrixClient = MatrixClient(activity.applicationContext, serverDomain!!, userName!!, accessToken!!, refreshToken!!)
            val result = matrixClient.logout()
            if(!result.success) return@launch
            CoroutineScope(Dispatchers.Main).launch {
                userName = null
                accessToken = null
                refreshToken = null
            }
        }
    }

    Column(
        modifier = Modifier
            .systemBarsPadding()
            .padding(16.dp)
            .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                IconButton(onClick = {onBack()}) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        Modifier.size(30.dp)
                    )
                }
            }

            Text("Matrix server", color = colorResource(R.color.text), size = 4f, fontWeight = FontWeight.Bold, modifier = Modifier.align(Alignment.Center))
        }


        Section("Server settings") {
            EditStringField(labelText = "Server domain", placeholderText = "server.domain", value = serverDomain ?: "", onValueChanged = {serverDomain = it.trim()}, modifier = Modifier.fillMaxWidth())
        }

        Section("User account") {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(modifier = Modifier.background(colorResource(R.color.accent_button), CircleShape).padding(8.dp)) {
                    Icon(Icons.Default.Person, "Account icon")
                }
                Text(if (userName != null) "Logged in as " + userName else "Please login", modifier = Modifier.weight(1f))
                TextButton(if (userName != null) "Logout" else "Login", {
                    if(userName != null) logout()
                    else shownPopup = ShownPopup.LOGIN
                })
            }
        }

        Section("Room configuration") {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
                EditStringField(
                    labelText = "Room id",
                    placeholderText = "abcdefgh:server.domain",
                    value = targetRoom ?: "",
                    onValueChanged = { targetRoom = it.trim() },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(15.dp)){
                    TextButton("Create room", {
                        shownPopup = ShownPopup.ROOM_CREATOR
                    }, modifier = Modifier.weight(1f))

                    TextButton("Select room", {
                        shownPopup = ShownPopup.ROOM_SELECTOR
                    }, modifier = Modifier.weight(1f))
                }
            }
        }

        Section("Diagnostics") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton("Send test message", {
                    if(activity == null) return@TextButton
                    if(serverDomain == null || serverDomain == "" || accessToken == null || accessToken == "") {
                        activity.sendToastText("Please login first")
                        return@TextButton
                    }
                    if(targetRoom == null || targetRoom == "") {
                        activity.sendToastText("Please select a room")
                        return@TextButton
                    }
                    CoroutineScope(Dispatchers.Default).launch {
                        val result = MatrixClient(activity.applicationContext, serverDomain!!, userName!!, accessToken!!, refreshToken).sendMessage(targetRoom!!, "That worked well!")
                        CoroutineScope(Dispatchers.Main).launch {
                            activity.sendToastText(if (result.success) "Successfully sent test message!" else "Error while sending a test message: " + result.error)
                        }
                    }
                }, Modifier.fillMaxWidth())
                Text("This will send a test message to the specified room.", 2.5f, align = TextAlign.Center, modifier = Modifier.fillMaxWidth())
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(15.dp), verticalAlignment = Alignment.Bottom, modifier = Modifier.fillMaxHeight(1f)) {
            TextButton("Cancel", {onBack()}, modifier = Modifier.weight(1f))
            TextButton("Save", {save()}, modifier = Modifier.weight(1f))
        }
    }

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

@OptIn(ExperimentalComposeUiApi::class)
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

    Popup ({ onClose() }) {
        Column(
            modifier = Modifier.padding(30.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text ("Login to " + serverDomain,
                4f
            )
            Spacer(Modifier.height(5.dp))
            var statusColor = Color.White
            if(loginStatus != null) {
                if (loginStatus!! == LoginStatus.SUCCESS) statusColor = colorResource(R.color.success)
                else if (loginStatus!! != LoginStatus.LOGGING_IN) statusColor = colorResource(R.color.error)
            }
            Text (
                if(loginStatus != null) loginStatus!!.text else "",
                color = statusColor
            )
            Spacer(modifier = Modifier.height(5.dp))
            EditStringField(labelText = "username",
                value = userName,
                onValueChanged = {userName = it.trim()},
                modifier = Modifier.fillMaxWidth(),
                hidden = false,
                autofillType = AutofillType.Username
            )
            Spacer(modifier = Modifier.height(10.dp))
            EditStringField(labelText = "password",
                value = password,
                onValueChanged = {password = it.trim()},
                modifier = Modifier.fillMaxWidth(),
                hidden = true,
                autofillType = AutofillType.Password
            )
            Spacer(modifier = Modifier.height(15.dp))
            Text("Password will be never saved!",
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(15.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton("Cancel",
                    modifier = Modifier.weight(1f),
                    enabled = loginStatus != LoginStatus.LOGGING_IN,
                    onClick =  {
                        onClose()
                    }
                )
                TextButton("Login",
                    enabled = loginStatus != LoginStatus.LOGGING_IN,
                    onClick = {
                        login()
                    },
                    modifier = Modifier.weight(1f),
                    color = colorResource(R.color.accent_button),
                )
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

@OptIn(ExperimentalComposeUiApi::class)
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

    Popup({ onClose() }) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text (
                "Create private room / direct message",
                3.5f,
                align = TextAlign.Center
            )
            Spacer(Modifier.height(5.dp))
            var statusColor = Color.White
            if(createStatus != null && createStatus != CreateStatus.CREATING) {
                if (createStatus!! != CreateStatus.SUCCESS) statusColor = Color.Red
                else statusColor = Color.Green
            }
            Text (
                if(createStatus != null) createStatus!!.text else "",
                color = statusColor
            )
            val shape = RoundedCornerShape(10.dp)
            Box(Modifier.fillMaxWidth().background(colorResource(R.color.section), shape).border(0.5.dp, colorResource(R.color.border), shape).padding(13.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(modifier = Modifier.background(colorResource(R.color.accent_button), CircleShape).padding(8.dp)) {
                        Icon(Icons.Default.Person, "Account icon")
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Direct message")
                        Text("Only the invited user can join", 2.5f, color = colorResource(R.color.text_info))
                    }
                    Switch(directMessageRoom, {directMessageRoom = it}, colors = SwitchDefaults.colors(
                        uncheckedBorderColor = Color.Transparent,
                        uncheckedTrackColor = colorResource(R.color.placeholder),
                        checkedTrackColor = colorResource(R.color.text_accent),
                        checkedThumbColor = colorResource(R.color.text),
                    ))
                }
            }
            Spacer(Modifier.height(5.dp))
            if(!directMessageRoom) {
                EditStringField(labelText = "room name",
                    placeholderText = "e.g Project X",
                    value = roomName,
                    onValueChanged = {roomName = it},
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.height(5.dp))
            EditStringField(labelText = "user to invite",
                placeholderText = "username or username:server.domain",
                value = inviteUserName,
                onValueChanged = {inviteUserName = it.trim()},
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
            Text("Username is enough,\nif the user is on the same server", size = 2.5f, align = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                TextButton("Cancel",{onClose()}, modifier = Modifier.weight(1f))
                TextButton("Create", {create()}, modifier = Modifier.weight(1f), color = colorResource(R.color.accent_button))
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

    var selectStatus by rememberSaveable { mutableStateOf<SelectStatus?>(SelectStatus.NEED_TO_SELECT_A_ROOM) }

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

    Popup ({ onClose() }) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(Modifier.fillMaxWidth()) {
                Column(Modifier.weight(1f)){
                    Text ("Room selector",
                        size = 3.5f,
                        align = TextAlign.Left
                    )
                    Text ("Refreshing also accepts invites", color = colorResource(R.color.text_info))
                }
                IconButton({refresh()}, Modifier.align(Alignment.CenterVertically)) { Icon(Icons.Filled.Refresh, "Refresh") }
            }

            Spacer(Modifier.height(5.dp))

            var statusColor = Color.White
            if(selectStatus != null && selectStatus != SelectStatus.GETTING_DATA) statusColor = Color.Red
            Text (if(selectStatus != null) selectStatus!!.text else "",
                color = statusColor
            )

            Spacer(Modifier.height(14.dp))

            Column(Modifier.fillMaxHeight(0.5f)) {
                val roomsList = rooms.toTypedArray()
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    items(roomsList.size) { index ->
                        val room = roomsList[index]
                        RoomSelectorEntry(room.displayName, room.membersAmount,selectedRoom == room, {selectedRoom = room})
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton("Cancel", {onClose()}, Modifier.weight(1f))
                TextButton("Select", {select()}, Modifier.weight(1f), color = colorResource(R.color.accent_button))
            }
        }
    }
}

@Composable
fun RoomSelectorEntry(name: String, members: Int, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth().background(if(selected) colorResource(R.color.accent_button) else colorResource(R.color.text_input), RoundedCornerShape(10.dp)).clickable { onClick() }) {
        Row(Modifier.padding(14.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name)
                Text("$members Members", color = colorResource(R.color.text_info))
            }
            if(selected) Text("Selected", color = colorResource(R.color.text_accent))
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
            MatrixConfigUI(null, MatrixConfig("abc.domain", "username_here", "aaa", "aaa", "muuh"), onSave = {}, onBack = {})
        }
    }
}