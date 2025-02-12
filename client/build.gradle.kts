plugins {
    id("buildsrc.convention.kotlin-jvm")
    id("org.graalvm.buildtools.native") version "0.10.5"
    application
}

version = "1.0.0"

configurations {
    nativeImageCompileOnly {
        isCanBeResolved = true
    }
}

dependencies {
    implementation(libs.jnativehook)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation(libs.kotlin.logging)
    implementation(libs.slf4j.simple)
}

application {
    mainClass = "dev.schlaubi.mastermind.MainKt"
    applicationDefaultJvmArgs = listOf("-agentlib:native-image-agent=config-output-dir=C:\\Users\\micha\\IdeaProjects\\gtakiller\\output.json")
}

graalvmNative {
    toolchainDetection = true

    binaries {
        named("main") {
            mainClass = "dev.schlaubi.mastermind.MainKt"
        }
    }
}
