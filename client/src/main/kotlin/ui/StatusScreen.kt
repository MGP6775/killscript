package dev.schlaubi.mastermind.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import dev.schlaubi.mastermind.core.currentApi
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import dev.schlaubi.mastermind.ui.components.Leaderboard
import dev.schlaubi.mastermind.ui.components.settings.Settings
import io.ktor.http.*
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatusScreen(navController: NavController) {
    val api = currentApi ?: error("Not connected to server")
    val scope = rememberCoroutineScope()

    Column(
        horizontalAlignment = Alignment.End,
        modifier = Modifier.fillMaxWidth()
    ) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberTooltipPositionProvider(TooltipAnchorPosition.Above),
            tooltip = { PlainTooltip { Text("Disconnect from server") } },
            state = rememberTooltipState()
        ) {
            IconButton({ scope.launch { writeSettings(settings.copy(currentUrl = null)); navController.navigate(Routes.Selector.name) } }) {
                Icon(Icons.AutoMirrored.Filled.Logout, "Disconnect")
            }
        }
    }

    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxSize().padding(25.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Connected to ${api.url.hostWithPortIfSpecified}",
                style = MaterialTheme.typography.headlineLarge,
                color = contentColorFor(MaterialTheme.colorScheme.surface)
            )
        }

        Spacer(Modifier.height(15.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxSize()
        ) {
            Leaderboard(api, modifier = Modifier.weight(1f))
            VerticalDivider(thickness = 3.dp, modifier = Modifier.padding(vertical = 25.dp))
            Settings(modifier = Modifier.weight(1f))
        }
    }
}
