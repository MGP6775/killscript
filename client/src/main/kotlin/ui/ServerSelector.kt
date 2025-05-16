package dev.schlaubi.mastermind.ui

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import dev.schlaubi.mastermind.core.APIClient
import dev.schlaubi.mastermind.core.currentApi
import dev.schlaubi.mastermind.core.settings.addServerOrMoveToTop
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.core.settings.writeSettings
import dev.schlaubi.mastermind.ui.components.DividerWithHeading
import io.ktor.http.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val DEFAULT_SERVER = "gta.schlau.bi"

private val protocols = listOf(URLProtocol.HTTPS, URLProtocol.HTTP)

data class State(val connecting: Boolean, val expanded: Boolean, val query: String) {
    companion object {
        val Default: State = State(false, false, settings.currentUrl?.toString() ?: "")
    }
}

class ServerSelectorViewModel(private val navController: NavController) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Default)
    val state = _state.asStateFlow()

    fun setQuery(query: String) {
        _state.tryEmit(state.value.copy(query = query))
    }

    fun expandSearch(open: Boolean = true) {
        _state.tryEmit(state.value.copy(expanded = open))
    }

    fun autofill(input: String) {
        _state.tryEmit(state.value.copy(query = input, expanded = false))
    }

    suspend fun connect() {
        _state.emit(state.value.copy(connecting = true))
        val url = state.value.query.parseUrl()
        val newClient = APIClient(url)
        currentApi?.disconnect()

        newClient.connectToWebSocket()
        writeSettings(settings.addServerOrMoveToTop(url))
        currentApi = newClient

        navController.navigate(Routes.Status.name)
        _state.emit(State.Default)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ServerSelector(
    navController: NavController,
    viewModel: ServerSelectorViewModel = viewModel { ServerSelectorViewModel(navController) },
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val coroutineScope = rememberCoroutineScope()

    val serverLine = state.query.parseUrl()

    Column(
        verticalArrangement = Arrangement.spacedBy(5.dp, Alignment.CenterVertically),
        horizontalAlignment = CenterHorizontally,
        modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Enter Server Address",
            style = MaterialTheme.typography.headlineSmall,
            color = contentColorFor(MaterialTheme.colorScheme.surface)
        )

        SearchBar(
            {
                SearchBarDefaults.InputField(
                    state.query,
                    { viewModel.setQuery(it) },
                    { viewModel.expandSearch(false) },
                    state.expanded,
                    { viewModel.expandSearch(it) },
                    placeholder = { Text(DEFAULT_SERVER) },
                    enabled = !state.connecting,
                )
            }, expanded = state.expanded, onExpandedChange = { viewModel.expandSearch(it) },
            modifier = Modifier.onKeyEvent {
                if ((it.key == Key.Escape || it.key == Key.Enter) && it.type == KeyEventType.KeyDown) {
                    viewModel.expandSearch(false)
                }
                true
            }) {
            DividerWithHeading("Recommendations")

            protocols.forEach {
                val url = URLBuilder().apply {
                    host = serverLine.host
                    if (serverLine.port != URLProtocol.WSS.defaultPort) {
                        port = serverLine.port
                    }
                    protocol = it
                }.buildString()
                DropdownMenuItem(
                    text = { Text(url) },
                    onClick = { viewModel.autofill(url) },
                    leadingIcon = { Icon(Icons.Default.Link, null) },
                    modifier = Modifier.focusable(true)
                )
            }

            val settings = settings

            if (settings.pastUrls.isNotEmpty()) {
                DividerWithHeading("Previously used servers")

                settings.pastUrls.forEach {
                    val url = it.toString()

                    DropdownMenuItem(
                        text = { Text(url) },
                        onClick = { viewModel.autofill(url) },
                        leadingIcon = { Icon(Icons.Default.History, null) },
                        modifier = Modifier.focusable(true)
                    )
                }
            }
        }

        Button({ coroutineScope.launch(Dispatchers.IO) { viewModel.connect() } }, enabled = !state.connecting) {
            if (state.connecting) {
                CircularProgressIndicator(Modifier.size(ButtonDefaults.IconSize))
            } else {
                Icon(Icons.Default.Link, contentDescription = "Connect")
            }
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Connect", style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun String.parseUrl(): Url {
    val url = ifBlank { DEFAULT_SERVER }
    return if ("://" in url) {
        Url(url)
    } else {
        Url("https://$url")
    }
}
