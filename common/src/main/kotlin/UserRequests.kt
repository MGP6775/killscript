package dev.schlaubi.gtakiller.common

import kotlinx.serialization.Serializable

@Serializable
data class UserUpdateRequest(val name: String)

@Serializable
data class UserCreateRequest(val name: String)

@Serializable
data class JWTUser(val id: String, val name: String, val token: String)
