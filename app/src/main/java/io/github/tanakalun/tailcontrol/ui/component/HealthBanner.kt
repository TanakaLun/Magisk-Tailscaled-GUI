package io.github.tanakalun.tailcontrol.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.ui.theme.isInDarkTheme
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Close
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.theme.MiuixTheme.isDynamicColor
import top.yukonga.miuix.kmp.utils.PressFeedbackType

/**
 * 健康检查横幅，采用 KernelSU homepage `WarningCard` 的设计：
 * 语义色容器 + 单行消息，本页额外支持多行消息与关闭按钮。
 */
@Composable
fun HealthBanner(
    healthCheck: List<String>,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (healthCheck.isEmpty()) return

    var isVisible by remember { mutableStateOf(true) }

    if (!isVisible) return

    val dark = isInDarkTheme()
    val containerColor = when {
        isDynamicColor -> MiuixTheme.colorScheme.errorContainer
        dark -> Color(0xFF310808)
        else -> Color(0xFFF8E2E2)
    }
    val contentColor = when {
        isDynamicColor -> MiuixTheme.colorScheme.onErrorContainer
        else -> if (dark) Color(0xFFF72727) else Color(0xFFF72727)
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        colors = CardDefaults.defaultColors(
            color = containerColor,
            contentColor = contentColor,
        ),
        pressFeedbackType = PressFeedbackType.Sink,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            SelectionContainer {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = stringResource(R.string.health_check),
                        fontSize = 14.sp,
                        color = contentColor,
                    )
                    healthCheck.forEach { message ->
                        Text(
                            text = message,
                            fontSize = 12.sp,
                            color = contentColor.copy(alpha = 0.9f),
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                }
            }
            IconButton(
                onClick = { onDismiss() },
            ) {
                Icon(
                    MiuixIcons.Close,
                    contentDescription = "Dismiss",
                    tint = contentColor,
                )
            }
        }
    }
}