package io.github.tanakalun.tailcontrol.ui.screen.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.BuildConfig
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.ui.component.MiuixTextInput
import io.github.tanakalun.tailcontrol.ui.component.SectionHeader
import io.github.tanakalun.tailcontrol.ui.component.UpdateDialog
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.COLOR_MODE_OPTIONS
import io.github.tanakalun.tailcontrol.ui.theme.LocalEnableBlur
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CircularProgressIndicator
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.basic.rememberPullToRefreshState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Ok
import top.yukonga.miuix.kmp.icon.extended.Update
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.preference.OverlayDropdownPreference
import top.yukonga.miuix.kmp.preference.SwitchPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun SettingsScreen(
    onOpenAccounts: () -> Unit,
    onOpenExitNode: () -> Unit,
    onOpenSubnet: () -> Unit,
    onOpenExperimental: () -> Unit,
    onOpenLogs: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
    updateViewModel: UpdateViewModel = hiltViewModel(),
    bottomInset: Dp = 0.dp,
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val updateState by updateViewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scrollBehavior = MiuixScrollBehavior()
    val pullToRefreshState = rememberPullToRefreshState()
    val backdrop = rememberBlurBackdrop()
    val barColor = if (backdrop != null) ComposeColor.Transparent else MiuixTheme.colorScheme.surface

    // Toast 提示
    LaunchedEffect(updateState) {
        when (updateState) {
            is UpdateViewModel.UpdateUiState.NoUpdate -> {
                android.widget.Toast.makeText(
                    context,
                    R.string.update_already_latest,
                    android.widget.Toast.LENGTH_SHORT,
                ).show()
                updateViewModel.resetState()
            }
            is UpdateViewModel.UpdateUiState.Error -> {
                val errorMsg = (updateState as UpdateViewModel.UpdateUiState.Error).message
                android.widget.Toast.makeText(context, errorMsg, android.widget.Toast.LENGTH_SHORT).show()
                updateViewModel.resetState()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            BlurredBar(backdrop = backdrop) {
                TopAppBar(
                    title = stringResource(R.string.tailscale_settings),
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                    actions = {
                        if (ui.dirty) {
                            IconButton(onClick = viewModel::save) {
                                Icon(
                                    MiuixIcons.Ok,
                                    contentDescription = stringResource(R.string.save),
                                )
                            }
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
        PullToRefresh(
            isRefreshing = ui.refreshing,
            pullToRefreshState = pullToRefreshState,
            onRefresh = { viewModel.refreshIdentity(force = true) },
            refreshTexts = listOf(
                stringResource(R.string.refresh_pulling),
                stringResource(R.string.refresh_release),
                stringResource(R.string.refresh_refreshing),
                stringResource(R.string.refresh_complete),
            ),
            contentPadding = padding,
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical()
                    .scrollEndHaptic(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    start = padding.calculateStartPadding(LocalLayoutDirection.current),
                    end = padding.calculateEndPadding(LocalLayoutDirection.current),
                    bottom = padding.calculateBottomPadding() + bottomInset,
                ),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                item { SectionHeader(stringResource(R.string.settings_group_connection)) }
                // 用户信息
                item {
                    Card(
                        Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        ArrowPreference(
                            title = ui.username.ifBlank {
                                stringResource(if (ui.isLoggedIn) R.string.unknown else R.string.status_service_needslogin)
                            },
                            summary = stringResource(R.string.manage),
                            onClick = onOpenAccounts,
                        )
                    }
                }

                // 开关项
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column {
                            SwitchPreference(
                                title = stringResource(R.string.subnet),
                                summary = "--accept-routes",
                                checked = ui.settings.acceptRoutes,
                                onCheckedChange = { v -> viewModel.update { it.copy(acceptRoutes = v) } },
                            )
                            SwitchPreference(
                                title = stringResource(R.string.accept_dns),
                                summary = "--accept-dns",
                                checked = ui.settings.acceptDns,
                                onCheckedChange = { v -> viewModel.update { it.copy(acceptDns = v) } },
                            )
                            SwitchPreference(
                                title = stringResource(R.string.advertise_exit_node),
                                summary = "--advertise-exit-node",
                                checked = ui.settings.advertiseExitNode,
                                onCheckedChange = { v -> viewModel.update { it.copy(advertiseExitNode = v) } },
                            )
                        }
                    }
                }

                // 选择器
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column {
                            ArrowPreference(
                                title = stringResource(R.string.exit_node),
                                summary = ui.settings.exitNode.ifBlank { stringResource(R.string.exit_node_none) },
                                onClick = onOpenExitNode,
                            )
                            ArrowPreference(
                                title = stringResource(R.string.advertise_subnets_routes),
                                summary = ui.settings.advertiseRoutes.ifBlank { stringResource(R.string.none) },
                                onClick = onOpenSubnet,
                            )
                        }
                    }
                }

                // 自由参数
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            MiuixTextInput(
                                value = ui.settings.customName,
                                onValueChange = { v -> viewModel.update { it.copy(customName = v) } },
                                modifier = Modifier.fillMaxWidth(),
                                label = "${stringResource(R.string.hostname_set)} (--hostname)",
                                placeholder = stringResource(R.string.hostname_set_descripe),
                            )
                            MiuixTextInput(
                                value = ui.settings.customParams,
                                onValueChange = { v -> viewModel.update { it.copy(customParams = v) } },
                                modifier = Modifier.fillMaxWidth(),
                                label = stringResource(R.string.custom_parameters),
                                placeholder = "--webclient --update-check=false",
                            )
                        }
                    }
                }

                // SSH
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        SwitchPreference(
                            title = stringResource(R.string.advertise_ssh),
                            summary = stringResource(R.string.advertise_ssh_desc),
                            checked = ui.sshServerEnabled,
                            onCheckedChange = viewModel::setSshServer,
                            enabled = !ui.sshUpdating,
                        )
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_group_diagnostics)) }
                // DNS 状态
                ui.dnsStatus?.let { dns ->
                    item {
                        Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                            SelectionContainer {
                                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("DNS", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        "${stringResource(R.string.dns_tailscale_dns)}: " +
                                            if (dns.tailscaleDnsEnabled) stringResource(R.string.dns_enabled) else stringResource(R.string.dns_disabled),
                                    )
                                    Text(
                                        "MagicDNS: " +
                                            if (dns.magicDnsEnabled) stringResource(R.string.dns_enabled) else stringResource(R.string.dns_disabled),
                                    )
                                    if (!dns.magicDnsSuffix.isNullOrBlank())
                                        Text("Suffix: ${dns.magicDnsSuffix}", fontSize = 13.sp)
                                    if (!dns.deviceDnsName.isNullOrBlank())
                                        Text("This device: ${dns.deviceDnsName}", fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                // 二进制路径
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        SelectionContainer {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(stringResource(R.string.binary_path_title), fontWeight = FontWeight.SemiBold)
                                Text(stringResource(R.string.binary_path_desc), fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                val missing = stringResource(R.string.binary_path_missing)
                                BinaryRow("tailscale", ui.binaries?.tailscale ?: missing)
                                BinaryRow("tailscaled", ui.binaries?.tailscaled ?: missing)
                                BinaryRow("tailscaled.service", ui.binaries?.service ?: missing)
                            }
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_group_appearance)) }
                // 主题模式 + 模糊效果
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column {
                            OverlayDropdownPreference(
                                title = stringResource(R.string.theme_mode),
                                items = COLOR_MODE_OPTIONS,
                                selectedIndex = ui.colorMode,
                                onSelectedIndexChange = viewModel::setColorMode,
                            )
                            SwitchPreference(
                                title = stringResource(R.string.enable_blur),
                                summary = stringResource(R.string.enable_blur_desc),
                                checked = ui.enableBlur,
                                onCheckedChange = viewModel::setEnableBlur,
                            )
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_group_more)) }
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column {
                            ArrowPreference(
                                title = stringResource(R.string.experimental_title),
                                summary = stringResource(R.string.experimental_subtitle),
                                onClick = onOpenExperimental,
                            )
                            ArrowPreference(
                                title = stringResource(R.string.nav_logs),
                                onClick = onOpenLogs,
                            )
                        }
                    }
                }

                item { SectionHeader(stringResource(R.string.settings_group_about)) }
                item {
                    Card(Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                        Column {
                            ArrowPreference(
                                title = stringResource(R.string.project_name),
                                summary = stringResource(R.string.project_description),
                                startAction = {
                                    Icon(
                                        painter = painterResource(id = R.drawable.github),
                                        contentDescription = stringResource(R.string.github_repo),
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                onClick = {
                                    val intent = android.content.Intent(
                                        android.content.Intent.ACTION_VIEW,
                                        "https://github.com/ArchChen1/Magisk-Tailscaled-GUI".toUri(),
                                    )
                                    context.startActivity(intent)
                                },
                            )
                            when (updateState) {
                                is UpdateViewModel.UpdateUiState.Checking -> {
                                    Row(
                                        Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            strokeWidth = 2.dp,
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(stringResource(R.string.checking_update), fontSize = 13.sp)
                                    }
                                }
                                else -> {
                                    ArrowPreference(
                                        title = "${stringResource(R.string.current_version)}: ${BuildConfig.VERSION_NAME}",
                                        summary = stringResource(R.string.check_updates),
                                        startAction = {
                                            Icon(
                                                MiuixIcons.Update,
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        },
                                        onClick = { updateViewModel.checkUpdate(showNoUpdateTip = true) },
                                    )
                                }
                            }
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
        }
    }

    if (ui.saveError != null) {
        OverlayDialog(
            show = ui.saveError != null,
            title = stringResource(R.string.save_failed),
            summary = "${stringResource(R.string.save_failed_text)}:\n${ui.saveError}",
            onDismissRequest = viewModel::consumeSaveError,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.done),
                        onClick = viewModel::consumeSaveError,
                    )
                }
            },
        )
    }
    if (ui.saveSuccess) {
        OverlayDialog(
            show = ui.saveSuccess,
            title = stringResource(R.string.save_success),
            onDismissRequest = viewModel::consumeSaveSuccess,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.done),
                        onClick = viewModel::consumeSaveSuccess,
                    )
                }
            },
        )
    }

    UpdateDialog(
        result = if (updateState is UpdateViewModel.UpdateUiState.HasUpdate)
            (updateState as UpdateViewModel.UpdateUiState.HasUpdate).result
        else null,
        onDismiss = { updateViewModel.resetState() },
        onOpenDownloadPage = { updateViewModel.openDownloadPage() },
    )
}

@Composable
private fun BinaryRow(name: String, path: String) {
    Row(
        Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "$name:",
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(end = 8.dp),
        )
        Text(
            path,
            fontSize = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}