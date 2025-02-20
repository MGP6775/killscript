package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.Event
import dev.schlaubi.gtakiller.common.Route.Events
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.resources.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

data class AuthenticatedServerWebSocketSession(val id: String, val session: DefaultWebSocketServerSession) :
    DefaultWebSocketServerSession by session

private val sessions = mutableListOf<AuthenticatedServerWebSocketSession>()

private val LOG = KotlinLogging.logger { }

fun Route.eventHandler() = resource<Events> {
    webSocket {
        val (userId) = call.user
        val session = AuthenticatedServerWebSocketSession(userId, this)
        sessions += session

        LOG.info { "User $userId opened WebSocket connection" }

        for (frame in incoming) {
            LOG.trace { "Received frame: $frame" }
        }

        LOG.info { "User $userId closed WebSocket connection" }
        sessions -= session
    }
}

suspend fun broadcastEvent(event: Event, predicate: (AuthenticatedServerWebSocketSession) -> Boolean = { true }) =
    sessions.forEach {
        if (predicate(it)) try {
            LOG.trace { "Sending event $event to $it" }
            it.sendSerialized(event)
        } catch (e: Exception) {
            LOG.warn(e) { "Error while sending event to $it" }
            sessions -= it
        }
    }

suspend fun broadcastEventExceptForUser(event: Event, userId: String) = broadcastEvent(event) { it.id != userId }
