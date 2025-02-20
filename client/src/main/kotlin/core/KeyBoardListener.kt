package dev.schlaubi.mastermind.core

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.util.Loom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking

fun registerKeyBoardListener() = GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
    override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
        if (nativeEvent.keyCode == settings.hotkey) {
            runBlocking(Dispatchers.Loom) {
                reportAndKill()
            }
        }
    }
})

suspend fun reportAndKill() {
    safeApi.killGta()
    killGta()
}
