package week11.st099681.finalproject.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = Blue,
    onPrimary = Color.White,
    secondary = SlateLight,
    onSecondary = TextPrimary,
    background = Navy,
    onBackground = TextPrimary,
    surface = NavyCard,
    onSurface = TextPrimary,
    surfaceVariant = Slate,
    onSurfaceVariant = TextSecondary,
    error = Red,
    onError = Color.White
)

@Composable
fun MotorVaultTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = Typography,
        content = content
    )
}
