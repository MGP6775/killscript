package dev.schlaubi.mastermind.core

import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlin.io.path.absolutePathString
import kotlin.io.path.div
import kotlin.jvm.optionals.getOrNull

private val LOG = KotlinLogging.logger { }

private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = Channel.UNLIMITED)
val gtaKillErrors = _events.asSharedFlow()

suspend fun killGta() {
    LOG.info { "Trying to kill GTA5.exe" }

    val gtaProcess = ProcessHandle.allProcesses()
        .filter { it.info().command().getOrNull()?.contains("GTA5.exe") == true }
        .findFirst()
    if (gtaProcess.isPresent) {
        gtaProcess.get().destroyForcibly()

        if (settings.autostartGta) {
            LOG.info { "Restarting GTA5.exe" }
            val gtaPath = WindowsAPI.readGtaLocation() / "PlayGTAV.exe"
            Runtime.getRuntime().exec(arrayOf(gtaPath.absolutePathString()))
        }
    } else {
        LOG.error { "GTA5.exe not found" }
        _events.emit(Unit)
    }
}
