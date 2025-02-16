@file:OptIn(ExperimentalSerializationApi::class)

package dev.schlaubi.gtakiller

import dev.schlaubi.gtakiller.common.Status
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.io.decodeFromSource
import kotlinx.serialization.json.io.encodeToSink

private val mutex = Mutex()

var stats = readStats()
    private set

private val file get() = Path("store/stats.json")

private val fs get() = SystemFileSystem

private fun readStats(): Status {
    if (!fs.exists(file)) {
        return Status(emptyList(), 0)
    }

    return fs.source(file).buffered().use {
        Json.decodeFromSource(it)
    }
}

suspend fun writeStats(status: Status) {
    stats = status

    if (!fs.exists(file.parent!!)) {
        fs.createDirectories(file.parent!!)
    }

    mutex.withLock {
        fs.sink(file).buffered().use {
            Json.encodeToSink(status, it)
        }
    }
}
