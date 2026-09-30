package io.github.tanakalun.tailcontrol.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import top.yukonga.miuix.kmp.basic.ScrollBehavior
import top.yukonga.miuix.kmp.blur.BlendColorEntry
import top.yukonga.miuix.kmp.blur.BlurDefaults
import top.yukonga.miuix.kmp.blur.LayerBackdrop
import top.yukonga.miuix.kmp.blur.isRuntimeShaderSupported
import top.yukonga.miuix.kmp.blur.rememberLayerBackdrop
import top.yukonga.miuix.kmp.blur.textureBlur
import top.yukonga.miuix.kmp.theme.MiuixTheme

/**
 * 全局模糊开关。由 MainActivity 从 Preferences 收集并提供；
 * 各屏通过 [LocalEnableBlur] 读取，并交给 [rememberBlurBackdrop] 判断是否创建 backdrop。
 */
val LocalEnableBlur = staticCompositionLocalOf { true }

/** blur 相关常量。 */
object AppBlur {
    const val RADIUS = 15f
    const val SURFACE_ALPHA = 0.8f
    const val NAVBAR_RADIUS = 15f
    const val NAVBAR_SURFACE_ALPHA = 0.8f
}

@Composable
fun rememberBlurBackdrop(): LayerBackdrop? {
    if (!LocalEnableBlur.current || !isRuntimeShaderSupported()) return null
    val surfaceColor = MiuixTheme.colorScheme.surface
    return rememberLayerBackdrop {
        drawRect(surfaceColor)
        drawContent()
    }
}

/**
 * TopAppBar 专用模糊容器：普通 textureBlur（非 progressive），
 * 对齐 miuix example 的 BlurredBar 用法。
 */
@Composable
fun BlurredBar(
    backdrop: LayerBackdrop?,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = if (backdrop != null) {
            modifier.fillMaxWidth().textureBlur(
                backdrop = backdrop,
                shape = RectangleShape,
                blurRadius = AppBlur.RADIUS,
                colors = BlurDefaults.blurColors(
                    blendColors = listOf(
                        BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(AppBlur.SURFACE_ALPHA)),
                    ),
                ),
            )
        } else {
            modifier
        },
    ) {
        content()
    }
}

/**
 * 导航栏专用模糊容器，对齐 miuix example 的 NavigationBar 实现：
 * `textureBlur` + `background(barColor)` + 点击拦截。
 * 需将内容（layerBackdrop）铺满到底部，navbar 之下存在被模糊的内容才有模糊观感。
 */
@Composable
fun NavBarBlur(
    backdrop: LayerBackdrop?,
    barColor: Color,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .then(if (backdrop != null) {
                Modifier.textureBlur(
                    backdrop = backdrop,
                    shape = RectangleShape,
                    blurRadius = AppBlur.NAVBAR_RADIUS,
                    colors = BlurDefaults.blurColors(
                        blendColors = listOf(
                            BlendColorEntry(color = MiuixTheme.colorScheme.surface.copy(AppBlur.NAVBAR_SURFACE_ALPHA)),
                        ),
                    ),
                )
            } else {
                Modifier
            })
            .background(barColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            ),
    ) {
        content()
    }
}