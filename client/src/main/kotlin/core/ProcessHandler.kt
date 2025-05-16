package dev.schlaubi.mastermind.core

import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.jvm.optionals.getOrNull
import kotlin.time.Duration.Companion.seconds

private val LOG = KotlinLogging.logger { }

sealed interface Event {
    object GtaProcessNotFound : Event
    data class RestartError(val exception: Exception) : Event
}

private val _events = MutableSharedFlow<Event>(extraBufferCapacity = Channel.UNLIMITED)
val gtaKillErrors = _events.asSharedFlow()

suspend fun killGta() {
    val version = settings.gtaVersion
    LOG.info { "Trying to kill ${version.process}" }

    val gtaProcess = ProcessHandle.allProcesses()
        .filter { it.info().command().getOrNull()?.contains(version.process) == true }
        .findFirst()
    if (gtaProcess.isPresent) {
        gtaProcess.get().destroyForcibly()

        delay(2.seconds)

        if (settings.autostartGta) {
            LOG.info { "Restarting GTA5.exe" }
            try {
                val gtaPath = WindowsAPI.readGtaLocation(version) / version.startBinary
                Runtime.getRuntime().exec(arrayOf(gtaPath.absolutePathString()))
            } catch (e: Exception) {
                _events.emit(Event.RestartError(e))
            }
        }
    } else {
        LOG.error { "GTA5.exe not found" }
        _events.emit(Event.GtaProcessNotFound)
    }
}
