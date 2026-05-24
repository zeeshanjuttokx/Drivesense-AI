package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = HDCyanAccent,
    secondary = HDEmerald,
    tertiary = HDCyanAccent,
    background = HDBackground,
    surface = HDSurface,
    surfaceVariant = HDSurface,
    onPrimary = HDBackground,
    onSecondary = HDBackground,
    onTertiary = HDBackground,
    onBackground = HDTextPrimary,
    onSurface = HDTextPrimary,
    onSurfaceVariant = HDTextSecondary,
    error = HDAmber,
    outline = HDBorder
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Always dark
    dynamicColor: Boolean = false, // Disabled
    content: @Composable () -> Unit,
) {
    MaterialTheme(colorScheme = DarkColorScheme, typography = Typography, content = content)
}
