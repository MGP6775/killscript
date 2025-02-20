package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.Route
import io.ktor.serialization.kotlinx.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.plugins.forwardedheaders.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.serialization.json.Json

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
        installAuth()

        routing {
            get<Route.Status> {
                call.respond(stats)
            }
            signUpRoute()

            authenticate {
                eventHandler()
                userRoute()
                killGTARoute()
            }

        }
    }.start(wait = true)
}
