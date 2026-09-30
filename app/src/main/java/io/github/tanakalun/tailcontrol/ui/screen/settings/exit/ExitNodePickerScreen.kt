package io.github.tanakalun.tailcontrol.ui.screen.settings.exit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.FloatingActionButton
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.RadioButton
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun ExitNodePickerScreen(
    onBack: () -> Unit,
    viewModel: ExitNodeViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val layoutDirection = LocalLayoutDirection.current
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop()
    val barColor = if (backdrop != null) ComposeColor.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop = backdrop) {
                TopAppBar(
                    title = stringResource(R.string.exit_node_picker_title),
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                modifier = Modifier.graphicsLayer {
                                    if (layoutDirection == LayoutDirection.Rtl) scaleX = -1f
                                },
                                imageVector = MiuixIcons.Back,
                                contentDescription = stringResource(R.string.back),
                            )
                        }
                    },
                )
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.save(onBack) }) {
                Icon(MiuixIcons.Ok, contentDescription = stringResource(R.string.save), tint = MiuixTheme.colorScheme.onPrimary)
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
        LazyColumn(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical()
                .scrollEndHaptic(),
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 8.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ui.suggestion?.let { sug ->
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = viewModel::applySuggestion,
                    ) {
                        Text(
                            stringResource(R.string.exit_node_suggest_use, sug),
                            modifier = Modifier.padding(14.dp),
                        )
                    }
                }
            }
            item {
                ChoiceRow(
                    label = stringResource(R.string.exit_node_none),
                    selected = ui.selected.isBlank(),
                ) { viewModel.select("") }
            }
            items(ui.candidates, key = { it.name }) { peer ->
                val identity = peer.ipv4 ?: peer.name
                ChoiceRow(
                    label = "${peer.name} (${peer.os})",
                    sub = identity,
                    selected = ui.selected == identity || ui.selected == peer.name,
                ) { viewModel.select(identity) }
            }
        }
        }
    }

    if (ui.saveError != null) {
        OverlayDialog(
            show = ui.saveError != null,
            title = stringResource(R.string.save_failed),
            summary = ui.saveError.orEmpty(),
            onDismissRequest = viewModel::consumeError,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.done),
                        onClick = viewModel::consumeError,
                    )
                }
            },
        )
    }
}

@Composable
private fun ChoiceRow(label: String, sub: String? = null, selected: Boolean, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            RadioButton(selected = selected, onClick = onClick)
            Column(Modifier.padding(start = 4.dp)) {
                Text(label, fontSize = 16.sp)
                if (!sub.isNullOrBlank()) Text(sub, fontSize = 13.sp)
            }
        }
    }
}