package dev.schlaubi.gtakiller.util

import java.security.MessageDigest

@OptIn(ExperimentalStdlibApi::class)
fun String.hashIpAddress(): String {
    val bytes = MessageDigest.getInstance("SHA-256").digest(toByteArray())
    return bytes.toHexString()
}
