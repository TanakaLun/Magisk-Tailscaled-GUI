package io.github.tanakalun.tailcontrol.ui.screen.settings.experimental

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun NavBarCustomizerScreen(
    onBack: () -> Unit,
    viewModel: NavBarCustomizerViewModel = hiltViewModel(),
) {
    // ui state 是唯一真相来源，开关状态和预览都从这里读
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val layoutDirection = LocalLayoutDirection.current
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop()
    val barColor = if (backdrop != null) ComposeColor.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop = backdrop) {
                TopAppBar(
                    title = stringResource(R.string.nav_customizer_title),
                    subtitle = stringResource(R.string.nav_customizer_hint),
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
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp)
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .overScrollVertical()
                .scrollEndHaptic(),
        ) {
            Spacer(Modifier.height(padding.calculateTopPadding()))
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Column {
                    // 固定项
                    viewModel.pinnedDestinations.toList().forEach { dest ->
                        SwitchPreference(
                            title = stringResource(dest.labelRes),
                            summary = stringResource(R.string.nav_customizer_pinned),
                            checked = true,
                            onCheckedChange = {},
                            enabled = false,
                        )
                    }
                    // 可切换项：checked 直接从 ui.hiddenItems 计算
                    viewModel.toggleableDestinations.forEach { dest ->
                        SwitchPreference(
                            title = stringResource(dest.labelRes),
                            checked = dest.name !in ui.hiddenItems,
                            onCheckedChange = { viewModel.toggle(dest) },
                        )
                    }
                }
            }
            Spacer(Modifier.height(padding.calculateBottomPadding()))
        }
        }
    }
}