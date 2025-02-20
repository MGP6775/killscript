package dev.schlaubi.mastermind.ui.components.settings

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import dev.schlaubi.gtakiller.common.UpdateNameEvent
import dev.schlaubi.mastermind.core.safeApi
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.launch

@Composable
fun UsernameInput() {
    var loading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(settings.userName) {
        safeApi.events.filterIsInstance<UpdateNameEvent>().filter { it.name == settings.userName }
            .collect { loading = false }
    }

    InputWithHeading({
        if (loading) CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize)) else Icon(
            Icons.Default.AccountBox, "Username"
        )
    }, "Username", initialValue = settings.userName, enabled = !loading, onSubmit = { newValue ->
        loading = true
        scope.launch(Dispatchers.IO) {
            safeApi.updateName(newValue)
            writeSettings(settings.copy(userName = newValue))
        }
    })
}
