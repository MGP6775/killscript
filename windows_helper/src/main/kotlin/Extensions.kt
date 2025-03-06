package dev.schlaubi.mastermind.windows_helper

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator
import kotlin.io.path.Path

private fun readString(producer: (SegmentAllocator) -> MemorySegment): String = Arena.ofConfined().use { arena ->
    val vec = producer(arena)
    val ptr = Vec_uint8.ptr(vec)
    try {
        val vectorLength = Vec_uint8.len(vec)
        println(Vec_uint8.cap(vec))
        val bytes = ptr
            .reinterpret(vectorLength)
            .asByteBuffer()

        Charsets.UTF_8.decode(bytes).toString()
    } finally {
        WindowsHelper.free_c_string(ptr)
    }
}

private fun SegmentAllocator.allocateCString(value: String) = Vec_uint8.allocate(this).also {
    allocateStringSegment(value, it)
}

private fun SegmentAllocator.allocateStringSegment(
    value: String,
    segment: MemorySegment
) {
    val ptr = allocateFrom(value)
    val len = value.length.toLong()

    Vec_uint8.len(segment, len)
    Vec_uint8.ptr(segment, ptr)
}

private fun SegmentAllocator.allocateCStrings(vararg values: String) = slice_ref_Vec_uint8.allocate(this).also {
    val array = Vec_uint8.allocateArray(values.size.toLong(), this)
    values.forEachIndexed { index, value ->
        val entry = array.asSlice(index.toLong() * Vec_uint8.sizeof(), Vec_uint8.sizeof())

        allocateStringSegment(value, entry)
    }

    slice_ref_Vec_uint8.len(it, values.size.toLong())
    slice_ref_Vec_uint8.ptr(it, array)
}

object WindowsAPI {
    fun readGtaLocation(version: GTAVersion) = Arena.ofConfined().use {
        Path(readString { WindowsHelper.read_gta_location(it, version.ordinal.toByte())})
    }

    fun registerKeyboardHook() = WindowsHelper.register_keyboard_hook()

    fun registerKeyboardListener(arena: Arena, callback: (Int) -> Unit) {
        val lambda = `register_keyboard_handler$cb`.allocate({ callback(it) }, arena)
        WindowsHelper.register_keyboard_handler(lambda)
    }

    fun spawnDetachedProcess(command: String) =
        Arena.ofConfined().use { WindowsHelper.spawn_detached_process(it.allocateCString(command)) }

    fun spawnElevatedProcess(command: String, vararg args: String) =
        Arena.ofConfined().use {
            val binary = it.allocateCString(command)
            val args = it.allocateCStrings(*args)

            WindowsHelper.spawn_elevated_process(binary, args)
        }
}
