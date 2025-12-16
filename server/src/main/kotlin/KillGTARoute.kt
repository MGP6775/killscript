package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.KillGtaEvent
import dev.schlaubi.gtakiller.common.Route.Kill
import dev.schlaubi.gtakiller.common.UpdateKillCounterEvent
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.server.resources.post
import kotlin.time.Clock
import kotlin.time.Instant
import kotlin.time.Duration.Companion.seconds
import dev.schlaubi.gtakiller.common.Kill as KillObject

private val lastKill = Instant.DISTANT_PAST

fun Route.killGTARoute() {
    post<Kill> {
        val (userId, name) = call.user

        broadcastEventExceptForUser(KillGtaEvent, userId)

        val killTime = Clock.System.now()
        val timeSinceLastKill = killTime - lastKill
        if (timeSinceLastKill > 10.seconds) {
            val kill = KillObject(killTime, name, userId)

            writeStats(stats.copy(kills = stats.kills + kill))

            broadcastEvent(UpdateKillCounterEvent(stats.kills.size, kill))
        }

        call.respond(HttpStatusCode.Accepted)
    }
}
