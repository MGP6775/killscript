# Ktor
-keepclassmembers class io.ktor.** { volatile <fields>; }
-keep class io.ktor.client.engine.okhttp.OkHttpEngineContainer
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider

# SLF4j
-keep class org.slf4j.simple.SimpleServiceProvider
-dontwarn io.github.oshai.kotlinlogging.logback.**

# serialization
# For some reason if we don't do this, we get a VerifyError at runtime
# Serializer for classes with named companion objects are retrieved using `getDeclaredClasses`.
# If you have any, replace classes with those containing named companion objects.
-keepattributes InnerClasses # Needed for `getDeclaredClasses`.

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}

-dontwarn kotlinx.atomicfu.**
-dontwarn io.netty.**
-dontwarn com.typesafe.**
-dontwarn org.slf4j.**
-dontwarn ch.qos.logback.**
-dontwarn okhttp3.**
-dontwarn io.ktor.**

# hotkeys
-dontwarn dev.schlaubi.mastermind.windows_helper.**
-keep class dev.schlaubi.mastermind.windows_helper.** { *; }

# compose
-dontoptimize
