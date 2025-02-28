package dev.schlaubi.mastermind.core

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.util.Loom
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.lang.foreign.Arena

@Composable
fun KeyBoardListener() {
    SideEffect { WindowsAPI.registerKeyboardHook() }
    DisposableEffect(Unit) {
        val arena = Arena.ofConfined()

        WindowsAPI.registerKeyboardListener(arena) { keyCode ->
            if (keyCode == settings.hotkey) {
                runBlocking(Dispatchers.Loom) {
                    reportAndKill()
                }
            }
        }

        onDispose {
            arena.close()
        }
    }
}

suspend fun reportAndKill() {
    safeApi.killGta()
    killGta()
}
