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
            onPrimary = Color.Black,
            onSecondary = Color.White
        )
        "Emerald" -> darkColorScheme(
            primary = EmeraldGold,
            secondary = EmeraldGreen,
            background = EmeraldDark,
            surface = Color(0xFF0A1A10),
            onPrimary = Color.Black,
            onSecondary = Color.White
        )
        "Ocean" -> darkColorScheme(
            primary = OceanGold,
            secondary = OceanBlue,
            background = OceanDark,
            surface = Color(0xFF0A0C1A),
            onPrimary = Color.Black,
            onSecondary = Color.White
        )
        else -> darkColorScheme(
            primary = WineGold,
            secondary = WineRed,
            background = WineDark,
            surface = WineSurface,
            onPrimary = Color.Black,
            onSecondary = Color.White
        )
    }

    MaterialTheme(colorScheme = colorScheme, content = content)
}
