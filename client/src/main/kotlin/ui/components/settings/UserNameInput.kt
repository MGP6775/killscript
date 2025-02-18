package dev.schlaubi.mastermind.ui.components.settings

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import dev.schlaubi.gtakiller.common.UpdateNameCommand
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
            safeApi.sendEvent(UpdateNameCommand(newValue))
            writeSettings(settings.copy(userName = newValue))
            loading = false
        }
    })
}
