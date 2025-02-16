package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.Kill
import dev.schlaubi.gtakiller.common.KillGtaEvent
import dev.schlaubi.gtakiller.common.Route
import dev.schlaubi.gtakiller.common.UpdateKillCounterEvent
import io.github.oshai.kotlinlogging.KotlinLogging
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
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

private var lastKill: Instant? = null

fun main() {
    val sessions = mutableListOf<DefaultWebSocketServerSession>()

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
                webSocket {
                    sessions += this

                    val name = call.request.headers["X-Username"] ?: "Anonymous"

                    LOG.info { "Got session from: ${call.request.origin.remoteHost}" }

                    while (isActive) {
                        val event = receiveEvent()
                        LOG.debug { "GOT FRAME: $event" }

                        sessions.forEach {
                            it.send(event)
                        }

                        if (event is KillGtaEvent) {
                            val kill = Kill(Clock.System.now(), name)
                            val timeSinceLastKill = Clock.System.now() - (lastKill ?: Instant.DISTANT_PAST)
                            if (timeSinceLastKill >= 10.seconds) {
                                writeStats(
                                    stats.copy(
                                        stats.kills + kill,
                                        count = stats.count + 1
                                    )
                                )
                            }
                            sessions.forEach {
                                it.send(UpdateKillCounterEvent(stats.count, kill))
                            }
                        }
                    }

                    sessions -= this

                    LOG.info { "Lost session: ${call.request.origin.remoteHost}" }
                }
            }

            get<Route.Status> {
                call.respond(stats)
            }
        }
    }.start(wait = true)
}
