package io.github.tanakalun.tailcontrol.ui.screen.settings.experimental

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Back
import top.yukonga.miuix.kmp.icon.extended.Backup
import top.yukonga.miuix.kmp.icon.extended.Refresh
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun ExperimentalScreen(
    onBack: () -> Unit,
    onOpenNavBarCustomizer: () -> Unit,
    viewModel: ExperimentalViewModel = hiltViewModel(),
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
                    title = stringResource(R.string.experimental_title),
                    subtitle = stringResource(R.string.experimental_warning),
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
                .padding(padding)
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
        Column(
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .verticalScroll(rememberScrollState())
                .overScrollVertical()
                .scrollEndHaptic(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Spacer(Modifier.height(4.dp))

            // AltRepo optimization
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Column {
                    SwitchPreference(
                        title = stringResource(R.string.experimental_altrepo_title),
                        summary = stringResource(R.string.experimental_altrepo_desc),
                        checked = ui.altRepoOptimization,
                        onCheckedChange = viewModel::setAltRepoOptimization,
                    )

                    // 路由操作按钮区：仅在 AltRepo 开启时显示
                    if (ui.altRepoOptimization) {
                        Row(
                            Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Button(
                                onClick = { viewModel.syncRoutes() },
                                enabled = !ui.routeSyncLoading,
                                modifier = Modifier.weight(1f),
                            ) {
                                if (ui.routeSyncLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp,
                                    )
                                } else {
                                    Icon(
                                        MiuixIcons.Refresh,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(end = 4.dp),
                                    )
                                }
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.experimental_route_sync_btn))
                            }

                            // 还原备份按钮（仅当备份存在时可见）
                            if (ui.hasRouteBackup) {
                                Button(
                                    onClick = { viewModel.restoreBackup() },
                                    enabled = !ui.routeSyncLoading,
                                    modifier = Modifier.weight(1f),
                                ) {
                                    Icon(
                                        MiuixIcons.Backup,
                                        contentDescription = null,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .padding(end = 4.dp),
                                    )
                                    Spacer(Modifier.width(4.dp))
                                    Text(stringResource(R.string.experimental_route_restore_btn))
                                }
                            }
                        }

                        Text(
                            stringResource(R.string.experimental_route_sync_hint),
                            fontSize = 12.sp,
                            color = MiuixTheme.colorScheme.onSurfaceVariantSummary,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        )
                    }
                }
            }

            // 导航栏自定义入口：标准 ArrowPreference，无 start 图标
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                ArrowPreference(
                    title = stringResource(R.string.nav_customizer_title),
                    summary = stringResource(R.string.nav_customizer_subtitle),
                    onClick = onOpenNavBarCustomizer,
                )
            }

            // Health banner
            Card(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                SwitchPreference(
                    title = stringResource(R.string.experimental_health_banner_title),
                    summary = stringResource(R.string.experimental_health_banner_desc),
                    checked = ui.healthBannerDisabled,
                    onCheckedChange = viewModel::setHealthBannerDisabled,
                )
            }
            Spacer(Modifier.height(80.dp))
        }
        }
    }

    // 操作结果 Dialog
    if (ui.routeSyncMessage != null) {
        OverlayDialog(
            show = ui.routeSyncMessage != null,
            title = stringResource(
                if (ui.routeSyncSuccess) R.string.experimental_route_sync_success_title
                else R.string.experimental_route_sync_failed_title
            ),
            onDismissRequest = { viewModel.clearSyncMessage() },
            content = {
                Column {
                    Text(
                        ui.routeSyncMessage ?: "",
                        fontSize = 13.sp,
                        fontFamily = FontFamily.Monospace,
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        TextButton(
                            modifier = Modifier.fillMaxWidth(),
                            text = stringResource(R.string.done),
                            onClick = { viewModel.clearSyncMessage() },
                        )
                    }
                }
            },
        )
    }
}