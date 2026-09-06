package org.forestdept.seemsai.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = ForestGreenPrimary,
    secondary = ForestGreenLight,
    tertiary = RadarAmber,
    background = ForestDark950,
    surface = ForestDark900,
    surfaceVariant = ForestCard,
    onPrimary = TextWhite,
    onSecondary = TextWhite,
    onTertiary = ForestDark950,
    onBackground = TextWhite,
    onSurface = TextWhite,
    onSurfaceVariant = TextMutedSage,
    error = AlertRed,
    onError = TextWhite
)

@Composable
fun SEEMSAITheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}

