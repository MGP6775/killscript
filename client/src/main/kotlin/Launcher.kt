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
import dev.schlaubi.mastermind.core.registerKeyBoardListener
import dev.schlaubi.mastermind.resources.Res
import dev.schlaubi.mastermind.resources.icon
import dev.schlaubi.mastermind.theme.AppTheme
import dev.schlaubi.mastermind.ui.GTAKiller
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Updater()
    Window(title = "GTA Killer", icon = painterResource(Res.drawable.icon), onCloseRequest = ::exitApplication) {
        var loading by remember { mutableStateOf(true) }
        if (loading) {
            SideEffect {
                WindowsAPI.registerKeyboardHook()
                registerKeyBoardListener()
            }

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
