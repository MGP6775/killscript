package dev.schlaubi.gtakiller.common

import io.ktor.http.HttpHeaders
import io.ktor.resources.*

class Route {
    @Resource("events")
    class Events

    @Resource("status")
    class Status
}

val HttpHeaders.Username get() = "X-Username"
