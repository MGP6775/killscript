import org.panteleyev.jpackage.ImageType

plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("org.panteleyev.jpackageplugin") version "1.6.1"
}

version = "1.0.0"

dependencies {
    implementation(projects.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jnativehook)
    implementation(libs.ktor.client.cio)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple)
}

tasks {
    val copyDependencies by registering(Copy::class) {
        from(configurations.runtimeClasspath).into(layout.buildDirectory.dir("jars"))
    }

    val copyJar by registering(Copy::class) {
        from(jar).into(layout.buildDirectory.dir("jars"))
    }

    jpackage {
        dependsOn(build, copyDependencies, copyJar)
        winConsole = true

        input = layout.buildDirectory.dir("jars").get().asFile.absolutePath

        appName = "GTA KILL"
        vendor = "Schlaubi"
        type = ImageType.APP_IMAGE

        mainJar = jar.get().archiveFile.get().asFile.absolutePath
        mainClass = "dev.schlaubi.mastermind.MainKt"

        destination = layout.buildDirectory.dir("dist").get().asFile.absolutePath
    }

    register<Tar>("packageDist") {
        dependsOn(jpackage)
        from(jpackage.get().destination + "/" + jpackage.get().appName)

        compression = Compression.GZIP
        archiveExtension = "tar.gz"
        destinationDirectory = layout.buildDirectory.dir("dist")
    }
}
