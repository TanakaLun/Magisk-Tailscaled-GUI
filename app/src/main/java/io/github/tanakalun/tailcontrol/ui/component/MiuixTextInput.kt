package io.github.tanakalun.tailcontrol.ui.component

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import top.yukonga.miuix.kmp.basic.TextField

/**
 * 受控文本输入：外部 [value] 与 miuix 状态式 [TextField] 双向同步。
 * 用户输入通过 snapshotFlow 回传 [onValueChange]；外部更新回写 [value]。
 */
@Composable
fun MiuixTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
) {
    val state = rememberTextFieldState(value)
    LaunchedEffect(value) {
        if (state.text.toString() != value) {
            val currentLength = state.text.length
            state.edit {
                replace(0, currentLength, value)
            }
        }
    }
    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collect { newValue -> if (newValue != value) onValueChange(newValue) }
    }
    TextField(
        state = state,
        modifier = modifier,
        label = label,
        useLabelAsPlaceholder = placeholder.isBlank(),
    )
}