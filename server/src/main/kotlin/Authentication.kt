package dev.schlaubi.gtakiller

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import dev.schlaubi.gtakiller.common.JWTUser
import dev.schlaubi.gtakiller.common.Route.SignUp
import dev.schlaubi.gtakiller.common.UserCreateRequest
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.resources.*
import io.ktor.server.response.*
import io.ktor.server.routing.Route
import io.ktor.util.*

private val algorithm = Algorithm.HMAC256("secret")

private val verifier = JWT.require(algorithm).build()

data class JWTUserData(val id: String, val name: String)

fun Application.installAuth() {
    install(Authentication) {
        jwt {
            verifier { verifier }
            validate { JWTUserData(it.payload.subject!!, it.getClaim("user", String::class)!!) }
        }
    }
}

val ApplicationCall.user: JWTUserData
    get() {
        val token = principal<JWTUserData>()!!

        return token
    }

fun createToken(name: String, id: String = generateNonce()): JWTUser {
    val token = JWT.create().withSubject(id).withClaim("user", name).sign(algorithm)

    return JWTUser(id, name, token)
}

fun Route.signUpRoute() {
    post<SignUp> {
        val (name) = call.receive<UserCreateRequest>()
        call.respond(createToken(name))
    }
}
