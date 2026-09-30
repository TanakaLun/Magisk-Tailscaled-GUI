package io.github.tanakalun.tailcontrol.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import io.github.tanakalun.tailcontrol.core.model.StatusTone
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Immutable
data class StatusColors(
    val online: Color,
    val warning: Color,
    val offline: Color,
    val error: Color,
    val unknown: Color,
)

@Composable
@ReadOnlyComposable
fun statusColorsFor(colorMode: Int): StatusColors {
    val dark = when (colorMode) {
        1, 4 -> false
        2, 5 -> true
        else -> isSystemInDarkTheme()
    }
    return StatusColors(
        online = if (dark) Color(0xFF63D08C) else Color(0xFF1B873F),
        warning = if (dark) Color(0xFFFFD17A) else Color(0xFFB76A00),
        offline = MiuixTheme.colorScheme.onSurfaceVariantSummary,
        error = MiuixTheme.colorScheme.error,
        unknown = MiuixTheme.colorScheme.outline,
    )
}

val LocalStatusColors = staticCompositionLocalOf {
    StatusColors(
        online = Color(0xFF1B873F),
        warning = Color(0xFFB76A00),
        offline = Color(0xFF74777F),
        error = Color(0xFFBA1A1A),
        unknown = Color(0xFF8E9099),
    )
}

@Composable
@ReadOnlyComposable
fun StatusColors.colorFor(tone: StatusTone): Color = when (tone) {
    StatusTone.ONLINE -> online
    StatusTone.WARNING -> warning
    StatusTone.OFFLINE -> offline
    StatusTone.ERROR -> error
    StatusTone.UNKNOWN -> unknown
}
