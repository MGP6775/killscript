package dev.schlaubi.mastermind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.schlaubi.mastermind.core.Updater
import dev.schlaubi.mastermind.core.registerKeyboardHandler
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import dev.schlaubi.mastermind.resources.Res
import dev.schlaubi.mastermind.resources.icon
import dev.schlaubi.mastermind.theme.AppTheme
import dev.schlaubi.mastermind.ui.GTAKiller
import dev.schlaubi.mastermind.util.Loom
import dev.schlaubi.mastermind.windows_helper.GTAVersion
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import java.lang.foreign.Arena

private val LOG = KotlinLogging.logger { }

fun main() = application {
    Updater()
    LaunchedEffect(Unit) {
    }
    Window(title = "GTA Killer", icon = painterResource(Res.drawable.icon), onCloseRequest = ::exitApplication) {
        var loading by remember { mutableStateOf(true) }
        val scope = rememberCoroutineScope()

        DisposableEffect(Unit) {
            val arena = Arena.ofShared()
            scope.launch(Dispatchers.Loom) {
                WindowsAPI.registerKeyboardHook()
                registerKeyboardHandler(arena)

                val detectedVersion = GTAVersion.systemDefault()
                LOG.info { "Detected GTA version: $detectedVersion" }

                if (detectedVersion != null) {
                    writeSettings(settings.copy(gtaVersion = detectedVersion))
                }

                loading = false
            }

            onDispose { arena.close() }
        }
        if (loading) {
            AppTheme {
                Scaffold {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                        Text(
                            "Launching App ...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        GTAKiller()
    }
}
