package io.github.tanakalun.tailcontrol.ui.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.SmallTitle

/**
 * 分组标题，直接使用 miuix 标准的 [SmallTitle]（默认 onBackgroundVariant 颜色与 insideMargin）。
 * 请在 LazyColumn/Column 中直接调用，不要额外叠加水平内边距。
 */
@Composable
fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    SmallTitle(
        text = text,
        modifier = modifier.fillMaxWidth(),
    )
}