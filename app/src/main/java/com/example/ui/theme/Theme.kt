package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = MintGreen,
    onPrimary = DarkBackground,
    secondary = SoftAmber,
    onSecondary = DarkBackground,
    tertiary = ElectricTeal,
    background = DarkBackground,
    surface = DarkSurface,
    onBackground = TextLightGray,
    onSurface = TextLightGray,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextLightGray
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldGreen,
    onPrimary = OffWhiteBackground,
    secondary = GoldenAmber,
    onSecondary = TextDarkGray,
    tertiary = SoftTeal,
    background = OffWhiteBackground,
    surface = SoftGraySurface,
    onBackground = TextDarkGray,
    onSurface = TextDarkGray,
    surfaceVariant = SoftGraySurface,
    onSurfaceVariant = TextDarkGray
)

@Composable
fun CaprazcaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Let's disable dynamic color to preserve our custom branded aesthetic!
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
