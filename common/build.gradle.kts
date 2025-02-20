plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(libs.kotlinx.serialization.json)
    api(libs.kotlinx.datetime)
    api(libs.ktor.resources)
    api(libs.ktor.http)
    api(libs.ktor.serialization.kotlinx.json)
}
