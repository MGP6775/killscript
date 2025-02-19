import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
}

version = "1.0.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(projects.common)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.jnativehook)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)

    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}

compose.desktop {
    application {
        mainClass = "dev.schlaubi.mastermind.LauncherKt"
        nativeDistributions {
            modules(
                "java.naming" // required by logback
            )
            targetFormats(TargetFormat.Msi)

            licenseFile = rootProject.file("LICENSE")
            vendor = "Schlaubi"
            description = "GTA kill script"
            copyright = "(c) 2025 Michael Rittmeister"
            packageName = "GTA Killer"

            windows {
                console = true
                menuGroup = "GTA Killer"
                upgradeUuid = "8193b8f9-1355-4d0f-9c6f-6619d0f18604"
            }
        }

        buildTypes {
            release {
                proguard {
                    version = libs.versions.proguard
                    configurationFiles.from(project.file("rules.pro"))
                }
            }
        }
    }
}
