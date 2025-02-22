package dev.schlaubi.mastermind

import androidx.compose.ui.window.singleWindowApplication
import dev.schlaubi.mastermind.core.registerKeyBoardListener
import dev.schlaubi.mastermind.ui.GTAKiller
import dev.schlaubi.mastermind.windows_helper.WindowsAPI

fun main() {
    WindowsAPI.registerKeyboardHook()
    registerKeyBoardListener()

    singleWindowApplication(title = "GTA Killer") {
        GTAKiller()
    }
}
