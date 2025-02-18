package dev.schlaubi.mastermind.ui.components.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.input.key.*
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp

@Composable
fun Settings(modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text("Settings", color = MaterialTheme.colorScheme.onSurface, style = MaterialTheme.typography.headlineMedium)

        Column(
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            UsernameInput()
            KeyboardInput()
        }
    }
}

@Composable
fun InputWithHeading(
    leadingIcon: @Composable () -> Unit,
    heading: String,
    initialValue: String,
    enabled: Boolean = true,
    trailingIcon: @Composable () -> Unit = {},
    isError: Boolean = false,
    onValueChange: (String) -> Unit = {},
    onSubmit: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var value by remember { mutableStateOf(initialValue) }
    val focusRequester = LocalFocusManager.current

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = modifier
    ) {
        Text(heading, style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = value,
            { value = it; onValueChange(it) },
            leadingIcon = leadingIcon,
            trailingIcon = trailingIcon,
            colors = TextFieldDefaults.colors(),
            enabled = enabled,
            isError = isError,
            maxLines = 1,
            modifier = Modifier.onKeyEvent {
                if (it.type != KeyEventType.KeyDown) return@onKeyEvent true

                when (it.key) {
                    Key.Escape -> {
                        value = initialValue
                        focusRequester.clearFocus()
                    }
                }

                false
            }.onFocusChanged {
                if (!it.isFocused && value != initialValue) {
                    onSubmit(value)
                }
            })
    }
}
