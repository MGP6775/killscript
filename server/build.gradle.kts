plugins {
    id("buildsrc.convention.kotlin-jvm")
    application
}

dependencies {
    implementation(libs.ktor.server.netty)
    implementation(libs.ktor.server.websockets)
    implementation(libs.slf4j.simple)
}

application {
    mainClass = "dev.schlaubi.gtakiller.ServerKt"
}
