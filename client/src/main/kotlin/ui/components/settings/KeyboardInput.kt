package dev.schlaubi.mastermind.ui.components.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import dev.schlaubi.mastermind.util.keys
import kotlinx.coroutines.launch

@Composable
fun KeyboardInput() {
    var isError by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Box {
        InputWithHeading(
            { Icon(Icons.Default.Keyboard, null) }, "HotKey", initialValue = "F3",
            isError = isError,
            onValueChange = { isError = it !in keys },
            onSubmit = {
                if (!isError) {
                    scope.launch {
                        writeSettings(settings.copy(hotkey = keys[it]!!))
                    }
                }
            }
        )
    }
}
