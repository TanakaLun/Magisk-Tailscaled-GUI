package io.github.tanakalun.tailcontrol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import top.yukonga.miuix.kmp.theme.ColorSchemeMode
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.ThemeController
import top.yukonga.miuix.kmp.theme.ThemeColorSpec
import top.yukonga.miuix.kmp.theme.ThemePaletteStyle

/** 主题模式：0=跟随系统 1=浅色 2=深色 3=Monet 跟随系统 4=Monet 浅色 5=Monet 深色 */
const val DEFAULT_COLOR_MODE = 3

val COLOR_MODE_OPTIONS = listOf(
    "Miuix 跟随系统",
    "Miuix 浅色",
    "Miuix 深色",
    "Monet 跟随系统",
    "Monet 浅色",
    "Monet 深色",
)

val LocalColorMode = staticCompositionLocalOf { DEFAULT_COLOR_MODE }

@Composable
fun TailControlTheme(
    colorMode: Int = DEFAULT_COLOR_MODE,
    content: @Composable () -> Unit,
) {
    val spec = ThemeColorSpec.Spec2021
    val style = ThemePaletteStyle.Content
    val controller = remember(colorMode) {
        when (colorMode) {
            1 -> ThemeController(ColorSchemeMode.Light)
            2 -> ThemeController(ColorSchemeMode.Dark)
            3 -> ThemeController(ColorSchemeMode.MonetSystem, colorSpec = spec, paletteStyle = style)
            4 -> ThemeController(ColorSchemeMode.MonetLight, colorSpec = spec, paletteStyle = style)
            5 -> ThemeController(ColorSchemeMode.MonetDark, colorSpec = spec, paletteStyle = style)
            else -> ThemeController(ColorSchemeMode.System)
        }
    }
    CompositionLocalProvider(LocalColorMode provides colorMode) {
        MiuixTheme(controller = controller) {
            CompositionLocalProvider(LocalStatusColors provides statusColorsFor(colorMode)) {
                content()
            }
        }
    }
}

@Composable
fun isInDarkTheme(): Boolean = when (LocalColorMode.current) {
    1, 4 -> false
    2, 5 -> true
    else -> isSystemInDarkTheme()
}