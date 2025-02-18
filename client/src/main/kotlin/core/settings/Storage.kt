@file:OptIn(ExperimentalSerializationApi::class)

package dev.schlaubi.mastermind.core.settings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

private val mutex = Mutex()

var settings by mutableStateOf(readSettings())
    private set

private val file get() = Path(getAppDataRoaming(), "settings.json")

private val fs get() = SystemFileSystem

private fun readSettings(): Settings {

    if (!fs.exists(file)) {
        return Settings(
            null, emptySet(), System.getProperty("user.name"),
            NativeKeyEvent.VC_F3
        )
    }

    return fs.source(file).buffered().use {
        Json.decodeFromSource(it)
    }
}

suspend fun writeSettings(status: Settings) {
    settings = status

    if (!fs.exists(file.parent!!)) {
        fs.createDirectories(file.parent!!)
    }

    mutex.withLock {
        fs.sink(file).buffered().use {
            Json.encodeToSink(status, it)
        }
    }
}

private fun getAppDataRoaming(): Path {
    val os = System.getProperty("os.name")
    val basePath = when {
        os.contains("windows", ignoreCase = true) -> Path(System.getenv("APPDATA"))
        os.contains("mac", ignoreCase = true) ->
            Path(System.getenv("HOME"), "Library", "Application Support")

        else -> Path(System.getProperty("user.home"))
    }
    return Path(basePath, "GTA-Killer")
}
