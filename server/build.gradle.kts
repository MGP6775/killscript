plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(projects.common)
    implementation(libs.kotlinx.serialization.json.io)
    implementation(libs.kotlinx.io.core)
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.ktor.server.auth.jwt)
    implementation(libs.ktor.server.forwarded.header)
    implementation(libs.ktor.server.resources)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.jwt)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)
}

application {
    mainClass = "dev.schlaubi.gtakiller.ServerKt"
}
