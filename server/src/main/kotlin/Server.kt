package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.*
import dev.schlaubi.gtakiller.common.Route
import dev.schlaubi.gtakiller.util.hashIpAddress
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.http.*
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds
import io.ktor.server.routing.Route as KtorRoute

private val LOG = KotlinLogging.logger { }

private var lastKill: Instant? = null

@OptIn(DelicateCoroutinesApi::class)
fun main() {

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(WebSockets) {
            contentConverter = KotlinxWebsocketSerializationConverter(Json)
        }
        install(Resources)
        install(ContentNegotiation) {
            json()
        }
        install(XForwardedHeaders)

        routing {
            resource<Route.Events> {
                websocketHandler()
            }

            get<Route.Status> {
                call.respond(stats)
            }
        }
    }.start(wait = true)
}

private fun KtorRoute.websocketHandler() {
    val sessions = mutableListOf<DefaultWebSocketServerSession>()

    webSocket {
        sessions += this

        val ipHash = call.request.origin.remoteHost.hashIpAddress()
        var name = call.request.headers[HttpHeaders.Username] ?: "Anonymous"

        LOG.info { "Got session from: ${call.request.origin.remoteHost}" }

        launch {
            while (isActive) {
                val event = receiveEvent()
                LOG.debug { "GOT FRAME: $event" }

                if (event is UpdateNameCommand) {
                    name = event.name
                    writeStats(stats.updateName(ipHash, name))
                    sessions.forEach {
                        it.send(UpdateNameEvent(ipHash, name))
                    }
                }

                sessions.forEach {
                    if (it != this@websocketHandler) {
                        it.send(event)
                    }
                }

                if (event is KillGtaEvent) {
                    val kill = Kill(Clock.System.now(), name, ipHash)
                    val timeSinceLastKill = Clock.System.now() - (lastKill ?: Instant.DISTANT_PAST)
                    if (timeSinceLastKill >= 10.seconds) {
                        lastKill = Clock.System.now()
                        writeStats(
                            stats.copy(
                                stats.kills + kill,
                                count = stats.count + 1
                            )
                        )

                        sessions.forEach {
                            it.send(UpdateKillCounterEvent(stats.count, kill))
                        }
                    } else {
                        LOG.debug { "Ignoring kill, last kill was $timeSinceLastKill ago" }
                    }
                }
            }
        }

        val reason = closeReason.await() // await closure

        sessions -= this

        LOG.info { "Lost session: ${call.request.origin.remoteHost} because of: ${reason?.message}" }
    }
}
