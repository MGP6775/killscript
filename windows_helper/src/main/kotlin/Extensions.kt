package dev.schlaubi.mastermind.windows_helper

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.SegmentAllocator

private fun readString(producer: (SegmentAllocator) -> MemorySegment): String = Arena.ofConfined().use { arena ->
    val vec = producer(arena)
    val ptr = Vec_uint8.ptr(vec)
    try {
        val vectorLength = Vec_uint8.len(vec)
        val bytes = ptr
            .reinterpret(vectorLength)
            .asByteBuffer()

        Charsets.UTF_8.decode(bytes).toString()
    } finally {
        WindowsHelper.free_c_string(ptr)
    }
}


object WindowsAPI : Arena by Arena.ofConfined() {
    fun readGtaLocation() = readString(WindowsHelper::read_gta_location)

    fun registerKeyboardHook() = WindowsHelper.register_keyboard_hook()

    fun registerKeyboardListener(callback: (Int) -> Unit) {
        val lambda = `register_keyboard_handler$cb`.allocate({ callback(it) }, this)
        WindowsHelper.register_keyboard_handler(lambda)
    }
}
