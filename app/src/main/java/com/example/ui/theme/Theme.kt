package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape

private val DarkColorScheme = darkColorScheme(
    primary = Mint500,
    onPrimary = Slate950,
    primaryContainer = Mint900,
    onPrimaryContainer = Mint100,
    secondary = Cyan600,
    onSecondary = Color.White,
    secondaryContainer = Cyan900,
    onSecondaryContainer = Cyan100,
    tertiary = Amber600,
    background = Slate950,
    onBackground = Slate100,
    surface = Slate900,
    onSurface = Slate100,
    surfaceVariant = Slate800,
    onSurfaceVariant = Slate300,
    outline = Slate700,
    error = Rose600,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Mint600,
    onPrimary = Color.White,
    primaryContainer = Mint100,
    onPrimaryContainer = Mint900,
    secondary = Cyan600,
    onSecondary = Color.White,
    secondaryContainer = Cyan100,
    onSecondaryContainer = Cyan900,
    tertiary = Amber600,
    background = Paper50,
    onBackground = Slate900,
    surface = Color.White,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate500,
    outline = Slate300,
    error = Rose600,
    onError = Color.White
)

val StudioShapes = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(22.dp)
)

@Composable
fun FreeFlowTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = StudioShapes,
        content = content
    )
}
