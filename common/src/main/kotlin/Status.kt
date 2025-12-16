package dev.schlaubi.gtakiller.common

import kotlin.time.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Status(
    val kills: List<Kill>,
    val count: Int
) {
    val scoreboard: List<ScoreboardEntry>
        get() = kills
            .groupBy { it.user }
            .map { ScoreboardEntry(it.key, it.value.size) }
            .sortedByDescending { it.kills }


    fun updateName(id: String, name: String): Status =
        copy(kills = kills.map { if (it.ipHash == id) it.copy(user = name) else it })
}

@Serializable
data class ScoreboardEntry(val user: String, val kills: Int)

@Serializable
data class Kill(
    val timestamp: Instant,
    val user: String,
    val ipHash: String
)
