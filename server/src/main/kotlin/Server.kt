package dev.schlaubi.gtakiller

import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

private val LOG = KotlinLogging.logger { }

fun main() {
    val sessions = mutableListOf<DefaultWebSocketServerSession>()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(WebSockets)
        install(XForwardedHeaders)

        routing {
            webSocket {
                sessions += this

                LOG.info { "Got session from: ${call.request.origin.remoteHost}" }

                for (frame in incoming) {
                    LOG.debug { "GOT FRAME: $frame" }

                    sessions.forEach {
                        if (it != this) {
                            it.send(frame)
                        }
                    }
                }

                sessions -= this

                LOG.info { "Lost session: ${call.request.origin.remoteHost}" }
            }
        }
    }.start(wait = true)
}
