package dev.schlaubi.mastermind.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(useDarkTheme: Boolean = isSystemInDarkMode(), content: @Composable () -> Unit) {
    val colors = if (!useDarkTheme) {
        lightColorScheme()
    } else {
        darkColorScheme()
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
