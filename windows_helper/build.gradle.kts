import org.jetbrains.kotlin.gradle.dsl.jvm.JvmTargetValidationMode
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile
import org.jetbrains.kotlin.konan.target.HostManager

plugins {
    java
    kotlin("jvm")
}

val jextractOutput: Provider<Directory> = layout.buildDirectory.dir("generated/jextract/main/java")

sourceSets {
    main {
        java.srcDir(jextractOutput)
        resources.srcDir(layout.buildDirectory.dir("generated/resources/main"))
    }
}

tasks {
    val compileRust by registering(Exec::class) {
        inputs.dir("src")
        outputs.dir("target")

        commandLine("cargo", "build", "--release")
    }

    val generateHeaders by registering(Exec::class) {
        dependsOn(compileRust)
        inputs.dir("src")
        outputs.dir("target")

        commandLine("cargo", "run", "--features", "headers", "--bin", "generate-headers")
    }

    val generateBindingsWithJextract by registering(Exec::class) {
        dependsOn(generateHeaders)
        val header = "target/windows_helper.h"
        inputs.file(header)
        outputs.dir(jextractOutput)

        val command = if (HostManager.hostIsMingw) {
            "jextract.bat"
        } else {
            "jextract"
        }

        val libraryPath = if (System.getenv("GITHUB_REF") != null) {
            "resources/windows_helper"
        } else {
            file("target/release/windows_helper").absolutePath
        }

        commandLine(
            command,
            "--header-class-name", "WindowsHelper",
            "--target-package", "dev.schlaubi.mastermind.windows_helper",
            "--library", libraryPath,
            "--output", jextractOutput.get().asFile.absolutePath,
            "--include-function", "read_gta_location",
            "--include-function", "register_keyboard_handler",
            "--include-function", "register_keyboard_hook",
            "--include-function", "free_c_string",
            "--include-typedef", "uint8_t",
            "--include-typedef", "size_t",
            "--include-struct", "Vec_uint8",
            header,
        )
    }

    compileJava {
        dependsOn(generateBindingsWithJextract)
    }

    compileKotlin {
        dependsOn(generateBindingsWithJextract)
    }
}

tasks {
    withType<KotlinJvmCompile>().configureEach {
        jvmTargetValidationMode.set(JvmTargetValidationMode.IGNORE)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_22
}