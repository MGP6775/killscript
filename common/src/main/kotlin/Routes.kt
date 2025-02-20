package dev.schlaubi.gtakiller.common

import io.ktor.http.HttpHeaders
import io.ktor.resources.*

class Route {
    @Resource("events")
    class Events

    @Resource("status")
    class Status

    @Resource("sign-up")
    class SignUp

    @Resource("kill")
    class Kill

    @Resource("users/@me")
    class Me
}

val HttpHeaders.Username get() = "X-Username"
