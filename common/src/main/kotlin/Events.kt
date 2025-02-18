package dev.schlaubi.gtakiller.common

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Event

@SerialName("kill")
@Serializable
data object KillGtaEvent : Event

@SerialName("update_kill_counter")
@Serializable
data class UpdateKillCounterEvent(val killCount: Int, val kill: Kill) : Event

@SerialName("update_name")
@Serializable
data class UpdateNameCommand(val name: String) : Event

@SerialName("name_updated")
@Serializable
data class UpdateNameEvent(val id: String, val name: String) : Event
