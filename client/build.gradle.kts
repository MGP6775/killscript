import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    id("buildsrc.convention.kotlin-jvm")
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.compose)
}

version = "1.3.0"

repositories {
    mavenCentral()
    google()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    implementation(projects.common)
    implementation(projects.windowsHelper)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.websockets)
    implementation(libs.ktor.client.resources)
    implementation(libs.ktor.client.content.negotiation)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.kotlin.logging)
    implementation(libs.logback.classic)

    implementation(libs.kord.gateway)

    implementation(compose.desktop.currentOs)
    implementation(compose.foundation)
    implementation(compose.runtime)
    implementation(compose.ui)
    implementation(compose.material3)
    implementation(compose.materialIconsExtended)
    implementation(compose.components.resources)
    implementation(libs.compose.navigation)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
}

tasks {
    val copyDll by registering(Copy::class) {
        dependsOn(":windows_helper:compileRust",":windows_helper:generateHeaders")
        from(project(":windows_helper").layout.projectDirectory.dir("target/release/windows_helper.dll"))
        include("*.dll")
        into(layout.buildDirectory.dir("dll/common"))
    }

    afterEvaluate {
        named("prepareAppResources") {
            dependsOn(copyDll)
        }
    }
}

compose {
    resources {
        packageOfResClass = "dev.schlaubi.mastermind.resources"
        customDirectory("main", provider { layout.projectDirectory.dir("src/main/composeResources") })
    }
    desktop {
        application {
            mainClass = "dev.schlaubi.mastermind.LauncherKt"
            jvmArgs("--enable-native-access=ALL-UNNAMED")

            nativeDistributions {
                modules(
                    "java.naming" // required by logback
                )
                targetFormats(TargetFormat.Msi)

                appResourcesRootDir.set(layout.buildDirectory.dir("dll"))

                licenseFile = rootProject.file("LICENSE")
                vendor = "Schlaubi"
                description = "GTA kill script"
                copyright = "(c) 2025 Michael Rittmeister"
                packageName = "GTA Killer"

                windows {
                    iconFile = layout.projectDirectory.file("icons/icon.ico")
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
}
