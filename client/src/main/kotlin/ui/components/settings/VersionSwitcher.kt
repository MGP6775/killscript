package dev.schlaubi.mastermind.ui.components.settings

import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import dev.schlaubi.mastermind.windows_helper.GTAVersion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VersionSwitcher(modifier: Modifier = Modifier) {
    val scope = rememberCoroutineScope()
    InputWithHeading("Version Switcher", modifier) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            val currentVersion = settings.gtaVersion
            GTAVersion("Legacy", currentVersion == GTAVersion.LEGACY)
            Switch(currentVersion == GTAVersion.ENHANCED, {
                scope.launch(Dispatchers.IO) {
                    writeSettings(settings.copy(gtaVersion = if (it) GTAVersion.ENHANCED else GTAVersion.LEGACY))
                }
            })
            GTAVersion("Enhanced", currentVersion == GTAVersion.ENHANCED)
        }
    }
}

@Composable
private fun GTAVersion(name: String, selected: Boolean) {
    val transition = updateTransition(selected)
    val opacity = transition.animateFloat { if (it) 1f else 0.5f }

    Text(name, color = contentColorFor(MaterialTheme.colorScheme.surface).copy(alpha = opacity.value))
}