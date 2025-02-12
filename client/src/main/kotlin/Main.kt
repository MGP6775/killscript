package dev.schlaubi.mastermind

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

private lateinit var session: DefaultClientWebSocketSession

private val LoomDispatcher = Executors
    .newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

private val client = HttpClient {
    install(WebSockets) {
        pingInterval = 15.seconds
    }
}

private fun CoroutineScope.connect() {
    launch {
        session = client.webSocketSession("wss://ks.haxis.me")

        LOG.info { "Connection established" }
        for (frame in session.incoming) {
            LOG.trace { "Got frame: $frame" }
            val incoming = (frame as? Frame.Text)?.readText() ?: continue
            LOG.debug { "Got frame text: $incoming" }
            if (incoming == "KILL_GTA") {
                kill()
            }
        }

        LOG.warn { "Connection closed, trying to reconnect" }

        connect()
    }
}

suspend fun main() = coroutineScope {
    connect()

    GlobalScreen.registerNativeHook()

    GlobalScreen.addNativeKeyListener(object : NativeKeyListener {
        override fun nativeKeyPressed(nativeEvent: NativeKeyEvent) {
            if (nativeEvent.keyCode == NativeKeyEvent.VC_F3) {
                println("F3 pressed, sending kill command")

                runBlocking(LoomDispatcher) { reportAndKill() }
            }
        }
    })
}

private fun kill() {
    LOG.info { "Trying to kill GTA5.exe" }

    val gtaProcess = ProcessHandle.allProcesses()
        .filter { it.info().command().getOrNull()?.contains("GTA5.exe") == true }
        .findFirst()
    if (gtaProcess.isPresent) {
        gtaProcess.get().destroyForcibly()
    } else {
        LOG.error { "GTA5.exe not found" }
    }
}

private suspend fun report() = session.outgoing.send(Frame.Text("KILL_GTA"))

private suspend fun reportAndKill() {
    kill()
    report()
}
