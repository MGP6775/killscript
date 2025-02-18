package dev.schlaubi.mastermind.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.schlaubi.gtakiller.common.Status
import dev.schlaubi.gtakiller.common.UpdateKillCounterEvent
import dev.schlaubi.gtakiller.common.UpdateNameEvent
import dev.schlaubi.mastermind.core.APIClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface State {
    object Loading : State
    data class Monitoring(val status: Status) : State
}

class LeaderboardViewModel(private val api: APIClient) : ViewModel() {
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    suspend fun monitorEvents() {
        api.events.collect { event ->
                val currentState = _state.value as? State.Monitoring ?: return@collect
                when (event) {
                    is UpdateKillCounterEvent -> _state.emit(currentState.copy(status = currentState.status.copy(kills = currentState.status.kills + event.kill)))
                    is UpdateNameEvent -> _state.emit(currentState.copy(status = currentState.status.updateName(event.id, event.name)))

                    else -> Unit
                }
            }
    }

    suspend fun fetchInitialData() {
        val status = api.getCurrentStatus()

        _state.emit(State.Monitoring(status))
    }
}

@Composable
fun Leaderboard(
    api: APIClient, model: LeaderboardViewModel = viewModel { LeaderboardViewModel(api) }, modifier: Modifier = Modifier
) {
    val state by model.state.collectAsState()
    val currentState = state

    LaunchedEffect(api) {
        model.monitorEvents()
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier.fillMaxSize()
    ) {
        Text(
            "Leaderboard", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = modifier.fillMaxSize()
        ) {
            if (currentState is State.Loading) {
                LaunchedEffect(api) { model.fetchInitialData() }
                LoadingIndicator()
            } else if (currentState is State.Monitoring) {
                LeaderboardTable(currentState.status)
            }
        }
    }
}

@Composable
private fun LoadingIndicator() = Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
    CircularProgressIndicator()
    Text("Loading...", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
}

@Composable
private fun LeaderboardTable(status: Status) {
    Text(
        "Total kills: ${status.kills.size}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Column(
        modifier = Modifier.fillMaxSize().padding(25.dp)
            .border(3.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(15.dp)),
    ) {
        LeaderboardRow {
            LeaderboardColumn("Name")
            LeaderboardColumn("Rank")
        }
        SelectionContainer {
            LazyColumn {
                items(status.scoreboard.size) { index ->
                    val score = status.scoreboard[index]
                    LeaderboardRow(color = if (index % 2 == 1) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant) {
                        LeaderboardColumn(score.user)
                        LeaderboardColumn(score.kills)
                    }
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(
    color: Color = Color.Unspecified, modifier: Modifier = Modifier, content: @Composable RowScope.() -> Unit
) = Row(
    horizontalArrangement = Arrangement.SpaceBetween,
    modifier = modifier.background(color).fillMaxWidth().padding(10.dp),
    content = content
)

@Composable
private fun LeaderboardColumn(text: Any, modifier: Modifier = Modifier) = Text(
    text.toString(),
    style = MaterialTheme.typography.titleMedium,
    modifier = modifier,
    color = MaterialTheme.colorScheme.onSurface,
)
