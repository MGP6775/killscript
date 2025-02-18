package dev.schlaubi.mastermind.core.settings

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import io.ktor.http.Url
import kotlinx.serialization.Serializable

@Serializable
data class Settings(
    val currentUrl: Url?,
    val pastUrls: Set<Url>,
    val userName: String,
    val hotkey: Int = NativeKeyEvent.VC_F3
)

fun Settings.addServerOrMoveToTop(serverUrl: Url): Settings {
    val servers = if (serverUrl in pastUrls) (pastUrls - serverUrl) + serverUrl else pastUrls + serverUrl

    return copy(pastUrls = servers, currentUrl = serverUrl)
}
