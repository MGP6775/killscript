package dev.schlaubi.gtakiller.common

import io.ktor.util.reflect.*
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

val eventSerializer: (TypeInfo, Any) -> String =
    { info, event -> Json.encodeToString<Event>(event as Event) }
val eventDeserializer: (TypeInfo, String) -> Event =
    { info, event -> Json.decodeFromString<Event>(event) }

@Serializable
sealed interface Event

@SerialName("kill")
@Serializable
data object KillGtaEvent : Event

@SerialName("update_kill_counter")
@Serializable
data class UpdateKillCounterEvent(val killCount: Int, val kill: Kill) : Event

@SerialName("name_updated")
@Serializable
data class UpdateNameEvent(val id: String, val name: String) : Event

@SerialName("ping")
@Serializable
data object PingEvent : Event
