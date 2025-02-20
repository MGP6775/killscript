package dev.schlaubi.mastermind.core

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import dev.kord.gateway.retry.LinearRetry
import dev.schlaubi.gtakiller.common.*
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.resources.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.serialization.json.Json
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.seconds

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
            pingInterval = 2.seconds
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Resources)

        defaultRequest {
            url.takeFrom(this@APIClient.url)
            val token = settings.tokens[url.build().hostWithPort]
            token?.let { bearerAuth(it) }
        }
    }

    private var webSocketSession: DefaultClientWebSocketSession? = null
    private val _events = MutableSharedFlow<Event>()
    val events = _events.asSharedFlow()
    private val retry = LinearRetry(2.seconds, 20.seconds, 10)

    suspend fun connectToWebSocket(isRetry: Boolean = false) {
        webSocketSession?.close()
        if (url.hostWithPort !in settings.tokens) {
            writeSettings(settings.copy(tokens = settings.tokens + (url.hostWithPort to register().token)))
        }
        val session = try {
            client.webSocketSession {
                url {
                    url.takeFrom(this@APIClient.url)
                    protocol = if (url.protocol.isSecure()) {
                        URLProtocol.WSS
                    } else {
                        URLProtocol.WS
                    }

                    client.href(Route.Events(), this)
                }

                bearerAuth(settings.tokens[this@APIClient.url.hostWithPort]!!)
                headers.append(HttpHeaders.Username, settings.userName)
            }
        } catch (e: Exception) {
            LOG.error(e) { "Could not connect to websocket" }
            if (isRetry) {
                retry.retry()
                LOG.warn { "Retrying ..." }
                connectToWebSocket(isRetry = true)
            }
            return
        }
        retry.reset()

        webSocketSession = session

        session.launch {
            for (message in session.incoming) {
                val event = client.plugin(WebSockets).contentConverter!!.deserialize<Event>(message)
                    LOG.debug { "Received event: $event" }
                    _events.emit(event)
                    handleEvent(event)
                }

            LOG.info { "Lost connection to websocket" }
            connectToWebSocket(isRetry = true)
        }
    }

    suspend fun getCurrentStatus() = client.get(Route.Status()).body<Status>()

    suspend fun register() = client.post(Route.SignUp()) {
        contentType(ContentType.Application.Json)
        setBody(UserCreateRequest(settings.userName))
    }.body<JWTUser>()

    suspend fun updateName(name: String) = client.patch(Route.Me()) {
        contentType(ContentType.Application.Json)
        setBody(UserUpdateRequest(name))
    }.body<JWTUser>().also {
        writeSettings(settings.copy(tokens = settings.tokens + (url.hostWithPort to it.token)))
    }

    private suspend fun handleEvent(event: Event) {
        when (event) {
            is KillGtaEvent -> dev.schlaubi.mastermind.core.killGta()
            else -> {}
        }
    }

    suspend fun killGta() = client.post(Route.Kill()).body<Unit>()

    fun disconnect() {
        client.close()
        webSocketSession?.cancel()
    }
}
