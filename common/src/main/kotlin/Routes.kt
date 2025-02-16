package dev.schlaubi.gtakiller.common

import io.ktor.resources.*

class Route {
    @Resource("events")
    class Events

    @Resource("status")
    class Status
}
