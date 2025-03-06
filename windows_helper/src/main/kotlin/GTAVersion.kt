package dev.schlaubi.mastermind.windows_helper

import kotlinx.serialization.Serializable

@Serializable
enum class GTAVersion(val process: String, val startBinary: String, val registryLocation: String) {
    LEGACY("GTA5.exe", "PlayGTAV.exe", "SOFTWARE\\WOW6432Node\\Rockstar Games\\Grand Theft Auto V"),
    ENHANCED("GTA5_Enhanced.exe", "PlayGTAV.exe", "SOFTWARE\\WOW6432Node\\Rockstar Games\\GTAV Enhanced");

    operator fun not() = when (this) {
        LEGACY -> ENHANCED
        ENHANCED -> LEGACY
    }

    companion object {
        fun fromOrdinal(ordinal: Byte): GTAVersion = GTAVersion.entries[ordinal.toInt()]
        fun systemDefault(): GTAVersion? {
            val ordinal = WindowsHelper.detect_version().takeIf { it >= 0 } ?: return null

            return fromOrdinal(ordinal)
        }
    }
}
