package dev.schlaubi.gtakiller

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*


fun main() {
    val sessions = mutableListOf<DefaultWebSocketServerSession>()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(WebSockets)
        install(XForwardedHeaders)

        routing {
            webSocket {
                sessions += this

                println("Got session from: ${call.request.origin.remoteHost}")

                for (frame in incoming) {
                    println("GOT FRAME: $frame")

                    sessions.forEach {
                        if (it != this) {
                            it.send(frame)
                        }
                    }
                }

                sessions -= this

                println("Lost session: ${call.request.origin.remoteHost}")
            }
        }
    }.start(wait = true)
}
