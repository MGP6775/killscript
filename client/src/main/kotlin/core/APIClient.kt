package dev.schlaubi.mastermind.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.schlaubi.gtakiller.common.Event
import dev.schlaubi.gtakiller.common.KillGtaEvent
import dev.schlaubi.gtakiller.common.Route
import dev.schlaubi.gtakiller.common.Status
import dev.schlaubi.gtakiller.common.Username
import dev.schlaubi.mastermind.core.settings.settings
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext

var currentApi by mutableStateOf<APIClient?>(null)

val safeApi get() = currentApi ?: error("No API client set")

private val LOG = KotlinLogging.logger { }

class APIClient(val url: Url) : CoroutineScope {
    override val coroutineContext: CoroutineContext = Dispatchers.IO + SupervisorJob()

    private val client = HttpClient {
        install(ContentNegotiation) {
            json()
        }
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Resources)

        defaultRequest {
            url.takeFrom(this@APIClient.url)
        }
    }

    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()

    suspend fun connectToWebSocket() {
        webSocketSession?.close()
        val session = client.webSocketSession {
            url {
                url.takeFrom(this@APIClient.url)
                protocol = if (url.protocol.isSecure()) {
                    URLProtocol.WSS
                } else {
                    URLProtocol.WS
                }

                client.href(Route.Events(), this)
            }

            headers.append(HttpHeaders.Username, settings.userName)
        }

        webSocketSession = session

        session.launch {
            launch {
                while (isActive) {
                    val event = session.receiveDeserialized<Event>()
                    LOG.debug { "Received event: $event" }
                    _events.emit(event)
                    handleEvent(event)
                }
            }

            val reason = session.closeReason.await()
            LOG.info { "Lost connection to websocket: ${reason?.message}" }
        }
    }

    suspend fun getCurrentStatus() = client.get(Route.Status()).body<Status>()

    suspend fun sendEvent(event: Event) {
        LOG.debug { "Sending event: $event" }
        webSocketSession?.sendSerialized(event) ?: error("Not connected")
    }

    fun disconnect() {
        client.close()
        webSocketSession?.cancel()
    }
}

suspend fun reportKillCommand() = safeApi.sendEvent(KillGtaEvent)

private suspend fun handleEvent(event: Event) {
    when (event) {
        is KillGtaEvent -> killGta()
        else -> {}
    }
}
