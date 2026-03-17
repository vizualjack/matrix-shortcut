package dev.vizualjack.matrix_shortcut.ui.screen

import android.content.Context
import android.widget.ScrollView
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Text
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
import androidx.compose.ui.input.pointer.motionEventSpy
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.vizualjack.matrix_shortcut.AppActivity
import dev.vizualjack.matrix_shortcut.core.VibrationManager
import dev.vizualjack.matrix_shortcut.core.data.MatrixConfig
import dev.vizualjack.matrix_shortcut.core.data.Settings
import dev.vizualjack.matrix_shortcut.core.data.VibrationConfig
import dev.vizualjack.matrix_shortcut.core.data.VibrationConfigEntry
import dev.vizualjack.matrix_shortcut.core.matrix.MatrixChecker
import dev.vizualjack.matrix_shortcut.core.matrix.MatrixClient
import dev.vizualjack.matrix_shortcut.core.matrix.Room
import dev.vizualjack.matrix_shortcut.ui.components.EditNumberField
import dev.vizualjack.matrix_shortcut.ui.components.EditStringField
import dev.vizualjack.matrix_shortcut.ui.components.Popup
import dev.vizualjack.matrix_shortcut.ui.components.Screen
import dev.vizualjack.matrix_shortcut.ui.components.Section
import dev.vizualjack.matrix_shortcut.ui.components.TextButton
import dev.vizualjack.matrix_shortcut.ui.theme.AppTheme
import dev.vizualjack.matrix_shortcut.ui.theme.spacing
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
fun SettingsUI(activity: AppActivity?, settings: Settings, onSave:(settings: Settings) -> Unit, onBack:() -> Unit) {
    var serverDomain by rememberSaveable { mutableStateOf(settings.matrixConfig.serverDomain) }
    var userName by rememberSaveable { mutableStateOf(settings.matrixConfig.userName) }
    var accessToken by rememberSaveable { mutableStateOf(settings.matrixConfig.accessToken) }
    var refreshToken by rememberSaveable { mutableStateOf(settings.matrixConfig.refreshToken) }
    var targetRoom by rememberSaveable { mutableStateOf(settings.matrixConfig.targetRoom) }
    var wakeUpVibrationDurationMillis by rememberSaveable { mutableStateOf(settings.vibrationConfig.onWakeUp?.durationMillis) }
    var wakeUpVibrationAmplitude by rememberSaveable { mutableStateOf(settings.vibrationConfig.onWakeUp?.amplitude) }
    var gestureDetectedVibrationDurationMillis by rememberSaveable { mutableStateOf(settings.vibrationConfig.onGestureDetected?.durationMillis) }
    var gestureDetectedVibrationAmplitude by rememberSaveable { mutableStateOf(settings.vibrationConfig.onGestureDetected?.amplitude) }

    var shownPopup by remember { mutableStateOf(ShownPopup.NONE) }
    var domainCheckResult by remember { mutableStateOf<MatrixChecker.CheckResult?>(null) }

    fun save() {
        val matrixConfig = MatrixConfig(serverDomain, userName, accessToken, refreshToken, targetRoom)
        var wakeUpVibration: VibrationConfigEntry? = null
        if(wakeUpVibrationDurationMillis != null && wakeUpVibrationAmplitude != null)
            wakeUpVibration = VibrationConfigEntry(wakeUpVibrationDurationMillis!!, wakeUpVibrationAmplitude!!)
        var onGestureDetectedVibration: VibrationConfigEntry? = null
        if(gestureDetectedVibrationDurationMillis != null && gestureDetectedVibrationAmplitude != null)
            onGestureDetectedVibration = VibrationConfigEntry(gestureDetectedVibrationDurationMillis!!, gestureDetectedVibrationAmplitude!!)
        val vibrationConfig = VibrationConfig(wakeUpVibration, onGestureDetectedVibration)
        onSave(Settings(matrixConfig, vibrationConfig))
        onBack()
    }

    fun logout() {
        CoroutineScope(Dispatchers.Default).launch {
            if(activity == null) return@launch
            val matrixClient = MatrixClient(activity.applicationContext, serverDomain!!, userName!!, accessToken!!, refreshToken)
            val result = matrixClient.logout()
            if(!result.success) return@launch
            CoroutineScope(Dispatchers.Main).launch {
                userName = null
                accessToken = null
                refreshToken = null
            }
        }
    }

    fun checkServerDomain(afterSuccessJob: (() -> Unit)? = null) {
        if (serverDomain == null || activity == null) {
            if(activity != null && afterSuccessJob != null) activity.sendToastText("Please enter a server domain")
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            val result = MatrixChecker(activity.applicationContext).checkInstance(serverDomain!!)
            CoroutineScope(Dispatchers.Main).launch {
                domainCheckResult = result
                if(domainCheckResult == MatrixChecker.CheckResult.NO_MATRIX_INSTANCE) activity.sendToastText("No matrix server found at this domain")
                else if (domainCheckResult == MatrixChecker.CheckResult.UNREACHABLE) activity.sendToastText("Server unreachable")
                else if (afterSuccessJob != null) afterSuccessJob()
                else activity.sendToastText("Server reachable")
            }
        }
    }

    Screen({
        IconButton(onClick = {onBack()}, Modifier.align(Alignment.TopStart)) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back"
            )
        }

        Text("Settings", modifier = Modifier.align(Alignment.Center), style = MaterialTheme.typography.titleLarge)
    },{
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm),
            modifier = Modifier.verticalScroll(rememberScrollState())
        ) {
            Section("Server settings") {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    EditStringField(labelText = "Server domain",
                        placeholderText = "server.domain",
                        value = serverDomain ?: "",
                        onValueChanged = {
                            serverDomain = it.trim()
                            domainCheckResult = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    TextButton("Check", { checkServerDomain() }, Modifier.fillMaxWidth())
                }
            }

            Section("User account") {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                ) {
                    Box(modifier = Modifier
                        .background(MaterialTheme.colorScheme.secondaryContainer, CircleShape)
                        .padding(MaterialTheme.spacing.sm)
                    ) {
                        Icon(Icons.Default.Person, "Account icon", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }

                    if (userName != null) {
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                            Text("Logged in as ", style = MaterialTheme.typography.bodyMedium)
                            Text(userName!!, style = MaterialTheme.typography.labelLarge)
                        }
                    }
                    else Text( "Please log in", modifier = Modifier.weight(1f), style = MaterialTheme.typography.labelLarge)
                    TextButton(
                        if (userName != null) "Log out" else "Log in",
                        {
                            if(userName != null) logout()
                            else if(domainCheckResult != MatrixChecker.CheckResult.OK) checkServerDomain({shownPopup = ShownPopup.LOGIN})
                            else shownPopup = ShownPopup.LOGIN
                        }
                    )
                }
            }

            Section("Room configuration") {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
                ) {
                    EditStringField(
                        labelText = "Room id",
                        placeholderText = "abcdefgh:server.domain",
                        value = targetRoom ?: "",
                        onValueChanged = { targetRoom = it.trim() },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)){
                        TextButton("Create room", { shownPopup = ShownPopup.ROOM_CREATOR }, Modifier.weight(1f))
                        TextButton("Select room", { shownPopup = ShownPopup.ROOM_SELECTOR }, Modifier.weight(1f))
                    }
                }
            }

            Section("Diagnostics") {
                Column(verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    TextButton("Send test message", {
                        if(activity == null) return@TextButton
                        if(serverDomain == null || serverDomain == "" || accessToken == null || accessToken == "") {
                            activity.sendToastText("Please log in first")
                            return@TextButton
                        }
                        if(targetRoom == null || targetRoom == "") {
                            activity.sendToastText("Please select a room")
                            return@TextButton
                        }
                        CoroutineScope(Dispatchers.Default).launch {
                            val result = MatrixClient(activity.applicationContext, serverDomain!!, userName!!, accessToken!!, refreshToken).sendMessage(targetRoom!!, "Test successful.")
                            CoroutineScope(Dispatchers.Main).launch {
                                activity.sendToastText(if (result.success) "Test message sent successfully!" else "Error sending test message: " + result.error)
                            }
                        }
                    }, Modifier.fillMaxWidth())
                    Text("This will send a test message to the selected room.", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
            }

            Section("Vibration (Wake up / Ready)") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                        EditNumberField("Duration (ms)",
                            wakeUpVibrationDurationMillis?.toInt(),
                            { value ->
                                wakeUpVibrationDurationMillis = value?.toLong()
                                if((wakeUpVibrationDurationMillis ?: 0) > 1000) wakeUpVibrationDurationMillis = 1000
                            },
                            modifier = Modifier.weight(1f)
                        )
                        EditNumberField("Amplitude (1-255)",
                            wakeUpVibrationAmplitude,
                            { value ->
                                wakeUpVibrationAmplitude = value
                                if((wakeUpVibrationAmplitude ?: 0) > 255) wakeUpVibrationAmplitude = 255
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text( "Leave one field blank to disable vibration", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    TextButton("Test", {
                        CoroutineScope(Dispatchers.Main).launch {
                            if(activity == null) return@launch
                            VibrationManager(activity.applicationContext).vibrate(wakeUpVibrationDurationMillis, wakeUpVibrationAmplitude)
                        }
                    }, modifier = Modifier.fillMaxWidth())
                }
            }

            Section("Vibration (Shortcut detected)") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                        EditNumberField("Duration (ms)",
                            gestureDetectedVibrationDurationMillis?.toInt(),
                            {value ->
                                gestureDetectedVibrationDurationMillis = value?.toLong()
                                if((gestureDetectedVibrationDurationMillis ?: 0) > 1000) gestureDetectedVibrationDurationMillis = 1000
                            },
                            modifier = Modifier.weight(1f)
                        )
                        EditNumberField("Amplitude (1-255)",
                            gestureDetectedVibrationAmplitude,
                            { value ->
                                gestureDetectedVibrationAmplitude = value
                                if((gestureDetectedVibrationAmplitude ?: 0) > 255) gestureDetectedVibrationAmplitude = 255
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Text( "Leave one field blank to disable vibration", style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
                    TextButton("Test", {
                        CoroutineScope(Dispatchers.Main).launch {
                            if(activity == null) return@launch
                            VibrationManager(activity.applicationContext).vibrate(gestureDetectedVibrationDurationMillis, gestureDetectedVibrationAmplitude)
                        }
                    }, modifier = Modifier.fillMaxWidth())
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg),
                verticalAlignment = Alignment.Bottom,
            ) {
                TextButton("Cancel", { onBack() }, Modifier.weight(1f))
                TextButton("Save", { save() }, Modifier.weight(1f), true)
            }
        }
    })

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
        else shownPopup = ShownPopup.NONE
    }

    if (shownPopup != ShownPopup.NONE && shownPopup != ShownPopup.LOGIN) {
        if(serverDomain == null || serverDomain == "" || accessToken == null || accessToken == "") {
            activity?.sendToastText("Please log in first")
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
    FAILED("Incorrect username or password"),
    SUCCESS("Login successful"),
    MISSING_DATA("Please enter username and password"),
    SERVER_UNREACHABLE("Server unreachable"),
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
            Thread.sleep(500)
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

    Popup(onDismissRequest = {onClose()}, header = {
        Text("Log in to " + serverDomain, style = MaterialTheme.typography.titleMedium)
    }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            var statusColor = MaterialTheme.colorScheme.onBackground
            if(loginStatus != null) {
                if (loginStatus!! == LoginStatus.SUCCESS) statusColor = MaterialTheme.colorScheme.tertiary
                else if (loginStatus!! != LoginStatus.LOGGING_IN) statusColor = MaterialTheme.colorScheme.error
            }
            if(loginStatus != null) {
                Text (
                    loginStatus!!.text,
                    color = statusColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            EditStringField(labelText = "username",
                value = userName,
                onValueChanged = {userName = it.trim()},
                modifier = Modifier.fillMaxWidth(),
                hidden = false,
                autofillType = AutofillType.Username
            )

            EditStringField(labelText = "password",
                value = password,
                onValueChanged = {password = it.trim()},
                modifier = Modifier.fillMaxWidth(),
                hidden = true,
                autofillType = AutofillType.Password
            )

            Text("Password will never be saved",
                color = MaterialTheme.colorScheme.onPrimary,
                style = MaterialTheme.typography.bodyMedium
            )

            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.md)) {
                TextButton("Cancel", { onClose() }, Modifier.weight(1f), enabled = !(loginStatus in arrayOf(LoginStatus.LOGGING_IN, LoginStatus.SUCCESS)))
                TextButton("Login", { login() }, Modifier.weight(1f), true, enabled = !(loginStatus in arrayOf(LoginStatus.LOGGING_IN, LoginStatus.SUCCESS)))
            }
        }
    }
}

enum class CreateStatus(val text: String) {
    CREATING("Creating room..."),
    UNAUTHORIZED("Invalid access token. Please log in again"),
    SUCCESS("Room created successfully"),
    MISSING_DATA("Please fill in all fields"),
    SERVER_UNREACHABLE("Server unreachable"),
    TOO_MANY_REQUESTS("Server got too many requests, try again later"),
    UNKNOWN("Unknown error"),
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
            Thread.sleep(500)
            withContext(Dispatchers.Main) {
                onRoomCreated(result.value!!)
                onClose()
            }
        }
    }

    Popup(onDismissRequest = { onClose() }, header = {
        Text("Create room", style = MaterialTheme.typography.titleMedium)
    }) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            var statusColor = Color.White
            if(createStatus != null && createStatus != CreateStatus.CREATING) {
                if (createStatus!! != CreateStatus.SUCCESS) statusColor = MaterialTheme.colorScheme.error
                else statusColor = MaterialTheme.colorScheme.tertiary
            }
            if(createStatus != null) {
                Text (
                    createStatus!!.text,
                    color = statusColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Section(padding = MaterialTheme.spacing.xs, modifier = Modifier.clickable(indication = null, interactionSource = remember { MutableInteractionSource() }) { directMessageRoom = !directMessageRoom }) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                    Box(modifier = Modifier.background(MaterialTheme.colorScheme.secondaryContainer, CircleShape).padding(MaterialTheme.spacing.sm)) {
                        Icon(Icons.Default.Person, "Account icon", tint = MaterialTheme.colorScheme.onSecondaryContainer)
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Direct message", style = MaterialTheme.typography.labelLarge)
                        Text("Only the invited user can join", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(directMessageRoom, {directMessageRoom = it}, colors = SwitchDefaults.colors(
                        uncheckedBorderColor = Color.Transparent,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant,
                        uncheckedThumbColor = MaterialTheme.colorScheme.onPrimary,
                        checkedTrackColor = MaterialTheme.colorScheme.onSecondaryContainer,
                        checkedThumbColor = MaterialTheme.colorScheme.onBackground,
                    ))
                }
            }

            if(!directMessageRoom) {
                EditStringField(labelText = "room name",
                    placeholderText = "Project X",
                    value = roomName,
                    onValueChanged = {roomName = it},
                    modifier = Modifier.fillMaxWidth()
                )
            }

            EditStringField(labelText = "user to invite",
                placeholderText = "username:server.domain",
                value = inviteUserName,
                onValueChanged = {inviteUserName = it.trim()},
                modifier = Modifier.fillMaxWidth()
            )

            Text("Username is enough if on the same server", Modifier.fillMaxWidth(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)

            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.lg)) {
                TextButton("Cancel", { onClose() }, Modifier.weight(1f))
                TextButton("Create", { create() }, Modifier.weight(1f), true)
            }
        }
    }
}

enum class SelectStatus(val text: String) {
    GETTING_DATA("Getting data..."),
    UNAUTHORIZED("Invalid access token. Please log in again"),
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

    Popup(onDismissRequest = { onClose() }, header = {
        Row(Modifier.fillMaxWidth()) {
            Column(Modifier.weight(1f)){
                Text("Select room", style = MaterialTheme.typography.titleMedium)
                Text ("Invitations are also accepted during refresh", style = MaterialTheme.typography.bodyMedium)
            }
            IconButton({refresh()}, Modifier.align(Alignment.CenterVertically)) {
                Icon(Icons.Filled.Refresh, "Refresh")
            }
        }
    }) {
        Column(
            Modifier.fillMaxHeight(0.5f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)
        ) {
            var statusColor = MaterialTheme.colorScheme.onBackground
            if(selectStatus != null && selectStatus != SelectStatus.GETTING_DATA) statusColor = MaterialTheme.colorScheme.error
            if(selectStatus != null) {
                Text(
                    selectStatus!!.text,
                    color = statusColor,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(Modifier.weight(1f)) {
                val roomsList = rooms.toTypedArray()
                LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.xs)) {
                    items(roomsList.size) { index ->
                        val room = roomsList[index]
                        RoomSelectorEntry(room.displayName, room.membersAmount, room == selectedRoom, {selectedRoom = room})
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.sm)) {
                TextButton("Cancel", { onClose() }, Modifier.weight(1f))
                TextButton("Select", { select() }, Modifier.weight(1f), true)
            }
        }
    }
}

@Composable
fun RoomSelectorEntry(name: String, members: Int, selected: Boolean, onClick: () -> Unit) {
    Box(Modifier.fillMaxWidth()
        .background(if(selected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer, MaterialTheme.shapes.medium)
        .clickable { onClick() }
    ) {
        Row(Modifier.padding(MaterialTheme.spacing.md)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(name, style = MaterialTheme.typography.labelLarge)
                Text(members.toString() + (if (members == 1) " Member" else " Members"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSecondary)
            }
            if(selected) Text("Selected", modifier = Modifier.padding(MaterialTheme.spacing.md, 0.dp, 0.dp, 0.dp), color = MaterialTheme.colorScheme.onSecondary, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsPreview() {
    AppTheme {
        Surface(
            color = MaterialTheme.colorScheme.background,
//            modifier = Modifier.height(500.dp),
        ) {
            SettingsUI(null,
                Settings(
                    MatrixConfig("abc.domain", "username_here", "aaa", "aaa", "muuh"),
                    VibrationConfig()
                ),
                onSave = {},
                onBack = {})
        }
    }
}