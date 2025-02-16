package dev.schlaubi.mastermind

import com.github.kwhat.jnativehook.GlobalScreen
import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent
import com.github.kwhat.jnativehook.keyboard.NativeKeyListener
import dev.schlaubi.gtakiller.common.Event
import dev.schlaubi.gtakiller.common.KillGtaEvent
import dev.schlaubi.gtakiller.common.Route
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.util.concurrent.Executors
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

private lateinit var session: DefaultClientWebSocketSession

private val LoomDispatcher = Executors
    .newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

private val client = HttpClient {
    install(WebSockets) {
        pingInterval = 2.seconds
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    }
    install(Resources)
}

private fun CoroutineScope.connect() {
    launch {
        session = client.webSocketSession {
            url {
                takeFrom("ws://localhost:8080")
                client.href(Route.Events(), this)
            }

            headers.append("X-Username", System.getProperty("user.name"))
        }

        LOG.info { "Connection established" }
        while (isActive) {
            val frame = session.receiveDeserialized<Event>()
            LOG.trace { "Got frame: $frame" }
            if (frame is KillGtaEvent) {
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

private suspend fun report() = session.sendSerialized<Event>(KillGtaEvent)

private suspend fun reportAndKill() {
    kill()
    report()
}
