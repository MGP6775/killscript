# References to classes only used if needed
-dontwarn ch.qos.logback.**
-dontwarn io.ktor.network.sockets.**

-keep,allowshrinking,allowobfuscation class androidx.compose.runtime.* { *; }

# ktor
-keep class io.ktor.client.HttpClientEngineContainer
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider

# Hotkeys
-dontwarn dev.schlaubi.mastermind.windows_helper.**
-keep class dev.schlaubi.mastermind.windows_helper.register_keyboard_handler$cb$Function {
    void apply(int);
}

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

# Logback
-keep class org.slf4j.spi.SLF4JServiceProvider
-keep class ch.qos.logback.classic.spi.LogbackServiceProvider

-optimizations !method/specialization/parametertype