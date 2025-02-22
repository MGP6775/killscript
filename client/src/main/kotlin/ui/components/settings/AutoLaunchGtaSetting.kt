package dev.schlaubi.mastermind.ui.components.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun AutoLaunchGtaSetting() {
    val scope = rememberCoroutineScope()

    CheckboxWithHeading("Auto-Launch GTA", settings.autostartGta, {
        scope.launch(Dispatchers.IO) {
            writeSettings(settings.copy(autostartGta = it))
        }
    })
}
