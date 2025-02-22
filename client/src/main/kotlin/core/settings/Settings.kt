package dev.schlaubi.mastermind.core.settings

import io.ktor.http.*
import kotlinx.serialization.Serializable

const val F3_KEY = 113

@Serializable
data class Settings(
    val currentUrl: Url?,
    val pastUrls: Set<Url>,
    val userName: String,
    val hotkey: Int = F3_KEY,
    val tokens: Map<String, String> = emptyMap()
)

fun Settings.addServerOrMoveToTop(serverUrl: Url): Settings {
    val servers = if (serverUrl in pastUrls) (pastUrls - serverUrl) + serverUrl else pastUrls + serverUrl

    return copy(pastUrls = servers, currentUrl = serverUrl)
}
