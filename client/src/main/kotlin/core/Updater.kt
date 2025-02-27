package dev.schlaubi.mastermind.core

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import dev.schlaubi.mastermind.BuildConfig
import dev.schlaubi.mastermind.theme.AppTheme
import dev.schlaubi.mastermind.util.Loom
import dev.schlaubi.mastermind.windows_helper.WindowsAPI
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.utils.io.*
import io.ktor.utils.io.core.remaining
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.io.readByteArray
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import java.nio.file.StandardOpenOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.createTempFile
import kotlin.io.use

private val client = HttpClient {
    install(ContentNegotiation) {
        val json = Json {
            ignoreUnknownKeys = true
        }

        json(json)
    }
}

@Serializable
data class Release(
    val name: String,
    val assets: List<Asset>
) {
    @Serializable
    data class Asset(@SerialName("browser_download_url") val url: String, val name: String)
}

data class Version(val major: Int, val minor: Int, val patch: Int) : Comparable<Version> {

    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor, Version::patch)

    companion object {
        fun parse(version: String): Version {
            val split = version.split(".")
            if (split.size != 3) return Version(-1, -1, -1)
            return Version(split[0].toInt(), split[1].toInt(), split[2].toInt())
        }
    }
}

private suspend fun retrieveLatestVersion() = client
    .get("https://api.github.com/repos/MGP6775/killscript/releases/latest").body<Release>()

private sealed interface State {
    interface HasRelease {
        val release: Release
    }

    object Checking : State
    object Closed : State
    data class UpdateAvailable(override val release: Release) : State, HasRelease
    data class Downloading(
        override val release: Release,
        val downloaded: Long = 0L,
        val size: Long? = null,
        val done: Boolean = false
    ) : State, HasRelease
}

@Composable
fun ApplicationScope.Updater() {
    var state by remember { mutableStateOf<State>(State.Checking) }
    if (state is State.Closed) return

    LaunchedEffect(Unit) {
        val latest = retrieveLatestVersion()
        if (Version.parse(latest.name) > Version.parse(BuildConfig.APP_VERSION)) {
            state = State.UpdateAvailable(latest)
        } else {
            client.close()
        }
    }


    val currentState = state
    if (currentState is State.HasRelease) {
        Window({ state = State.Closed }, title = "GTAKiller - Update") {
            AppTheme {
                Scaffold {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text("A new Version is available: ${currentState.release.name}")

                        if (currentState is State.UpdateAvailable) {
                            Button({ state = State.Downloading(currentState.release) }) {
                                Icon(
                                    Icons.Default.Download,
                                    "Download",
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Text("Download now")
                            }
                        } else if (currentState is State.Downloading) {
                            val scope = rememberCoroutineScope()
                            DisposableEffect(currentState.release) {
                                val asset = currentState.release.assets.first { it.name == "client.msi" }

                                scope.launch(Dispatchers.Loom) {
                                    val file = createTempFile("gtakiller_update", ".msi")
                                    val fileChannel =
                                        FileChannel.open(file, StandardOpenOption.CREATE, StandardOpenOption.WRITE)

                                    client.prepareGet(asset.url) {
                                        contentType(ContentType.Application.OctetStream)
                                    }.execute { response ->
                                        val input = response.bodyAsChannel()
                                        val size = response.contentLength()
                                        state = currentState.copy(size = size)

                                        fileChannel.use { output ->
                                            var downloaded = 0L
                                            while (!input.isClosedForRead) {
                                                val packet = input.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
                                                while (!packet.exhausted()) {
                                                    downloaded += packet.remaining
                                                    state = currentState.copy(downloaded = downloaded, size = size)
                                                    val bytes = packet.readByteArray()

                                                    output.write(ByteBuffer.wrap(bytes))
                                                }
                                            }
                                        }

                                        state = currentState.copy(done = true)
                                        val exitCode =
                                            WindowsAPI.spawnElevatedProcess(
                                                "msiexec",
                                                "/i",
                                                file.absolutePathString(),
                                                "/passive",
                                                "/quiet"
                                            )
                                        if (exitCode != 0) {
                                            error("msiexec returned with exit code $exitCode")
                                        }

                                        val myBinary = ProcessHandle.current().info().command().get()
                                        WindowsAPI.spawnDetachedProcess(myBinary)

                                        exitApplication()
                                    }
                                }

                                onDispose {
                                    scope.cancel()
                                }
                            }

                            if (currentState.size == null || currentState.done) {
                                LinearProgressIndicator()
                            } else {
                                LinearProgressIndicator({ currentState.downloaded / currentState.size.toFloat() })
                            }
                        }
                    }
                }
            }
        }
    }
}
