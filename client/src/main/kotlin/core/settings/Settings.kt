package dev.schlaubi.mastermind.core.settings

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.nativeKeyCode
import dev.schlaubi.mastermind.windows_helper.GTAVersion
import io.ktor.http.*
import kotlinx.serialization.Serializable

val F3_KEY = Key.F3.nativeKeyCode

@Serializable
data class Settings(
    val currentUrl: Url?,
    val pastUrls: Set<Url>,
    val userName: String,
    val hotkey: Int = F3_KEY,
    val gtaVersion: GTAVersion = GTAVersion.LEGACY,
    val autostartGta: Boolean = true,
    val tokens: Map<String, String> = emptyMap()
)

fun Settings.addServerOrMoveToTop(serverUrl: Url): Settings {
    val servers = if (serverUrl in pastUrls) (pastUrls - serverUrl) + serverUrl else pastUrls + serverUrl

    return copy(pastUrls = servers, currentUrl = serverUrl)
}
