package com.loanfinancial.lofi.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val LightColorScheme =
    lightColorScheme(
        primary = BrandMain,
        onPrimary = Color.White,
        primaryContainer = BrandSoft,
        onPrimaryContainer = Primary900,
        secondary = Primary600,
        onSecondary = Color.White,
        background = BgPage,
        onBackground = TextPrimary,
        surface = BgSurface,
        onSurface = TextPrimary,
        surfaceVariant = BgMuted,
        onSurfaceVariant = TextSecondary,
        outline = BorderDefault,
        outlineVariant = BorderMuted,
        error = Color(0xFFEF4444),
        onError = Color.White,
    )

private val DarkColorScheme =
    darkColorScheme(
        primary = BrandMain,
        onPrimary = Color.Black,
        primaryContainer = Primary700,
        onPrimaryContainer = Color.White,
        secondary = Primary400,
        onSecondary = Color.Black,
        background = DarkBgPage,
        onBackground = DarkTextPrimary,
        surface = DarkBgSurface,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkBgMuted,
        onSurfaceVariant = DarkTextSecondary,
        outline = DarkBorderDefault,
        outlineVariant = DarkBorderMuted,
        error = Color(0xFFF87171),
        onError = Color.Black,
    )

@Composable
fun LofiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) {
                    dynamicDarkColorScheme(context)
                } else {
                    dynamicLightColorScheme(context)
                }
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
