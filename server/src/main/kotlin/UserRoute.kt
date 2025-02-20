package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.UpdateNameEvent
import dev.schlaubi.gtakiller.common.UserUpdateRequest
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.resources.patch
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import dev.schlaubi.gtakiller.common.Route as APIRoute

fun Route.userRoute() {
    patch<APIRoute.Me> {
        val (userId) = call.user

        val (newName) = call.receive<UserUpdateRequest>()
        writeStats(stats.updateName(userId, newName))

        broadcastEvent(UpdateNameEvent(userId, newName))

        call.respond(HttpStatusCode.Accepted, createToken(newName, userId))
    }
}
