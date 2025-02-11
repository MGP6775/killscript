package dev.schlaubi.gtakiller

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*


fun main() {
    val sessions = mutableListOf<DefaultWebSocketServerSession>()

    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        install(WebSockets)

        routing {
            webSocket {
                sessions += this

                println("Got session: $this")

                for (frame in incoming) {
                    println("GOT FRAME: $frame")

                    sessions.forEach {
                        if (it != this) {
                            it.send(frame)
                        }
                    }
                }

                sessions -= this

                println("Lost session: $this")
            }
        }
    }.start(wait = true)
}
