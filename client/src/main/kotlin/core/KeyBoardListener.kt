package dev.schlaubi.mastermind.core

import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.util.Loom
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun registerKeyBoardListener() = WindowsAPI.registerKeyboardListener { keyCode ->
    if (keyCode == settings.hotkey) {
        runBlocking(Dispatchers.Loom) {
            reportAndKill()
        }
    }
}

suspend fun reportAndKill() {
    safeApi.killGta()
    killGta()
}
