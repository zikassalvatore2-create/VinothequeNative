package com.vinotheque.nativeapp.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun ProvideVinothequeTheme(themeName: String, content: @Composable () -> Unit) {
    val colorScheme = when (themeName) {
        "Burgundy" -> darkColorScheme(
            primary = BurgundyGold,
            secondary = BurgundyRed,
            background = BurgundyDark,
            surface = Color(0xFF1A0A0C),
            surfaceVariant = Color(0xFF2D1418),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "Emerald" -> darkColorScheme(
            primary = EmeraldGold,
            secondary = EmeraldGreen,
            background = EmeraldDark,
            surface = Color(0xFF0A1A10),
            surfaceVariant = Color(0xFF142D1C),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        "Ocean" -> darkColorScheme(
            primary = OceanGold,
            secondary = OceanBlue,
            background = OceanDark,
            surface = Color(0xFF0A0C1A),
            surfaceVariant = Color(0xFF14182D),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
        else -> darkColorScheme(
            primary = WineGold,
            secondary = WineRed,
            background = WineDark,
            surface = WineSurface,
            surfaceVariant = Color(0xFF2A2A2A),
            onPrimary = Color.Black,
            onSecondary = Color.White,
            onBackground = Color.White,
            onSurface = Color.White
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
