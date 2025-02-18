package dev.schlaubi.mastermind

import androidx.compose.ui.window.singleWindowApplication
import com.github.kwhat.jnativehook.GlobalScreen
import dev.schlaubi.mastermind.core.registerKeyBoardListener
import dev.schlaubi.mastermind.ui.GTAKiller

fun main() {
    GlobalScreen.registerNativeHook()
    registerKeyBoardListener()

    singleWindowApplication(title = "GTA Killer") {
        GTAKiller()
    }
}
