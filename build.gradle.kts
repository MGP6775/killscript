import org.jetbrains.kotlin.gradle.dsl.KotlinBaseExtension

subprojects {
    afterEvaluate {
        configure<KotlinBaseExtension> {
            jvmToolchain(25)
        }
    }
}
