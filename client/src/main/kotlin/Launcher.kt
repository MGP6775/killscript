package dev.schlaubi.mastermind

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import dev.schlaubi.mastermind.core.KeyBoardListener
import dev.schlaubi.mastermind.core.Updater
import dev.schlaubi.mastermind.resources.Res
import dev.schlaubi.mastermind.resources.icon
import dev.schlaubi.mastermind.theme.AppTheme
import dev.schlaubi.mastermind.ui.GTAKiller
import org.jetbrains.compose.resources.painterResource

fun main() = application {
    Updater()
    Window(title = "GTA Killer", icon = painterResource(Res.drawable.icon), onCloseRequest = ::exitApplication) {
        var loading by remember { mutableStateOf(true) }
        KeyBoardListener()
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
