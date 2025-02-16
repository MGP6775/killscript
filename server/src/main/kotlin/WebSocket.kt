package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.Event
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.server.websocket.*

private val LOG = KotlinLogging.logger { }

suspend fun DefaultWebSocketServerSession.send(event: Event) {
    LOG.debug { "Sending event: $event" }
    sendSerialized<Event>(event)
}

suspend fun DefaultWebSocketServerSession.receiveEvent(): Event {
    val event = receiveDeserialized<Event>()
    LOG.debug { "Received event: $event" }

    return event
}
