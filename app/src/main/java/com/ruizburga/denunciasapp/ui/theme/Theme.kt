package com.ruizburga.denunciasapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val ColorSecondary = Color(0xFF91ACD9)

private val PremiumDarkScheme = darkColorScheme(
    primary = Teal,
    onPrimary = Navy,
    primaryContainer = TealLight,
    onPrimaryContainer = Ink,
    secondary = ColorSecondary,
    onSecondary = Navy,
    tertiary = Amber,
    onTertiary = Navy,
    error = Danger,
    onError = Navy,
    errorContainer = DangerContainer,
    onErrorContainer = Ink,
    background = Canvas,
    onBackground = Ink,
    surface = SurfaceBase,
    onSurface = Ink,
    surfaceVariant = SurfaceRaised,
    onSurfaceVariant = Muted,
    outline = Border,
    outlineVariant = Border
)

@Composable
fun DenunciasAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = PremiumDarkScheme,
        typography = Typography,
        content = content
    )
}
