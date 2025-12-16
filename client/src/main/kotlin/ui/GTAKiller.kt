package dev.schlaubi.mastermind.ui

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dev.schlaubi.gtakiller.common.KillGtaEvent
import dev.schlaubi.mastermind.core.Event
import dev.schlaubi.mastermind.core.currentApi
import dev.schlaubi.mastermind.core.gtaKillErrors
import dev.schlaubi.mastermind.core.settings.settings
import dev.schlaubi.mastermind.theme.AppTheme
import kotlinx.coroutines.flow.filterIsInstance

enum class Routes {
    Selector,
    Status
}

@Composable
fun GTAKiller() {
    val navController = rememberNavController()
    val selectorViewModel = viewModel { ServerSelectorViewModel(navController) }
    val focusRequester = remember { FocusRequester() }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(currentApi) {
        val api = currentApi ?: return@LaunchedEffect
        api.events
            .filterIsInstance<KillGtaEvent>()
            .collect {
                snackbarHostState.showSnackbar("Someone died, killing GTA5.exe ...", withDismissAction = true)
            }
    }

    LaunchedEffect(Unit) {
        gtaKillErrors
            .collect {
                val message = when (it) {
                    is Event.GtaProcessNotFound -> "${it.processName} is not running"
                    is Event.RestartError -> it.exception.message ?: "An unknown error occurred"
                }
                snackbarHostState.showSnackbar(message, withDismissAction = true)
            }
    }

    LaunchedEffect(Unit) {
        if (settings.currentUrl != null) {
            selectorViewModel
                .apply { setQuery(settings.currentUrl.toString()) }
                .connect()
        }
    }

    AppTheme {
        Scaffold(
            snackbarHost = { GTAKillerSnackbarHost(snackbarHostState) },
            modifier = Modifier.fillMaxSize()
                .focusRequester(focusRequester)
                .pointerInput(Unit) {
                    detectTapGestures {
                        focusRequester.requestFocus()
                    }
                }
        ) {
            NavHost(navController, Routes.Selector.name) {
                composable(Routes.Selector.name) { ServerSelector(navController, selectorViewModel) }

                composable(Routes.Status.name) { StatusScreen(navController) }
            }
        }
    }
}

@Composable
private fun GTAKillerSnackbarHost(state: SnackbarHostState) = Box(modifier = Modifier.fillMaxSize()) {
    SnackbarHost(state, modifier = Modifier.align(Alignment.BottomEnd).fillMaxSize(.35f).padding(16.dp))
}
