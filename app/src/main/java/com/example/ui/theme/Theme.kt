package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.data.settings.ColorPalette
import com.example.data.settings.ThemeMode

fun getKhataColorScheme(palette: ColorPalette, darkTheme: Boolean): ColorScheme {
    val primary = Color(palette.primaryColor)
    val secondary = Color(palette.secondaryColor)

    return if (darkTheme) {
        darkColorScheme(
            primary = when (palette) {
                ColorPalette.TRADITIONAL_KHATA -> KhataDarkPrimary
                ColorPalette.EMERALD_GREEN -> Color(0xFF34D399)
                ColorPalette.ROYAL_INDIGO -> Color(0xFF93C5FD)
                ColorPalette.SLATE_CHARCOAL -> Color(0xFF38BDF8)
                ColorPalette.WARM_TERRACOTTA -> Color(0xFFFB923C)
                ColorPalette.ROYAL_PURPLE -> Color(0xFFC084FC)
            },
            onPrimary = Color(0xFF1E293B),
            primaryContainer = primary.copy(alpha = 0.35f),
            onPrimaryContainer = Color(0xFFF1F5F9),
            secondary = secondary,
            onSecondary = Color(0xFF1E293B),
            secondaryContainer = secondary.copy(alpha = 0.25f),
            onSecondaryContainer = Color(0xFFFEF3C7),
            tertiary = Color(0xFFF59E0B),
            onTertiary = Color.Black,
            background = Color(0xFF121212),
            onBackground = Color(0xFFF3F4F6),
            surface = Color(0xFF1E1E1E),
            onSurface = Color(0xFFF3F4F6),
            surfaceVariant = Color(0xFF2C2C2C),
            onSurfaceVariant = Color(0xFF9CA3AF),
            outline = Color(0xFF4B5563)
        )
    } else {
        lightColorScheme(
            primary = primary,
            onPrimary = Color.White,
            primaryContainer = primary.copy(alpha = 0.12f),
            onPrimaryContainer = primary,
            secondary = secondary,
            onSecondary = Color.White,
            secondaryContainer = secondary.copy(alpha = 0.12f),
            onSecondaryContainer = secondary,
            tertiary = KhataTertiary,
            onTertiary = Color.White,
            tertiaryContainer = KhataTertiaryContainer,
            onTertiaryContainer = KhataOnTertiaryContainer,
            background = Color(0xFFF9FAFB),
            onBackground = Color(0xFF111827),
            surface = Color.White,
            onSurface = Color(0xFF111827),
            surfaceVariant = Color(0xFFF3F4F6),
            onSurfaceVariant = Color(0xFF4B5563),
            outline = Color(0xFFE5E7EB)
        )
    }
}

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    colorPalette: ColorPalette = ColorPalette.TRADITIONAL_KHATA,
    content: @Composable () -> Unit
) {
    val systemInDark = isSystemInDarkTheme()
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> systemInDark
        ThemeMode.LIGHT -> false
        ThemeMode.DARK -> true
    }

    val colorScheme = getKhataColorScheme(colorPalette, isDark)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

