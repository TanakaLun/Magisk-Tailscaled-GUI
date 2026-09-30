package io.github.tanakalun.tailcontrol.ui.screen.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.core.model.BackendState
import io.github.tanakalun.tailcontrol.core.model.TailscaleDevice
import io.github.tanakalun.tailcontrol.ui.component.DeviceCard
import io.github.tanakalun.tailcontrol.ui.component.HealthBanner
import io.github.tanakalun.tailcontrol.ui.component.StatusCard
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.LocalEnableBlur
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.IconButton
import top.yukonga.miuix.kmp.basic.MiuixScrollBehavior
import top.yukonga.miuix.kmp.basic.PullToRefresh
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Contacts
import top.yukonga.miuix.kmp.icon.extended.Notes
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun HomeScreen(
    onPeerClick: (TailscaleDevice) -> Unit,
    onOpenAccounts: () -> Unit,
    onOpenLogs: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel(),
    bottomInset: Dp = 0.dp,
) {
    val ui by viewModel.state.collectAsStateWithLifecycle()
    val status = ui.status
    val self = status.self
    val isRunning = status.backendState is BackendState.Running
    val undismissedHealthCheck = viewModel.getUndismissedHealthCheck()
    val scrollBehavior = MiuixScrollBehavior()
    val backdrop = rememberBlurBackdrop()
    val barColor = if (backdrop != null) ComposeColor.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop = backdrop) {
                TopAppBar(
                    title = stringResource(R.string.app_name),
                    color = barColor,
                    scrollBehavior = scrollBehavior,
                    actions = {
                        IconButton(onClick = onOpenAccounts) {
                            Icon(
                                MiuixIcons.Contacts,
                                contentDescription = stringResource(R.string.nav_accounts),
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                        IconButton(onClick = onOpenLogs) {
                            Icon(
                                MiuixIcons.Notes,
                                contentDescription = stringResource(R.string.nav_logs),
                                tint = MiuixTheme.colorScheme.onSurface,
                            )
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
        val refreshTexts = listOf(
            stringResource(R.string.refresh_pulling),
            stringResource(R.string.refresh_release),
            stringResource(R.string.refresh_refreshing),
            stringResource(R.string.refresh_complete),
        )
        PullToRefresh(
            isRefreshing = ui.isRefreshing,
            onRefresh = viewModel::manualRefresh,
            refreshTexts = refreshTexts,
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 24.dp + bottomInset),
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(scrollBehavior.nestedScrollConnection)
                    .overScrollVertical()
                    .scrollEndHaptic(),
            ) {
                if (undismissedHealthCheck.isNotEmpty()) {
                    item {
                        HealthBanner(
                            healthCheck = undismissedHealthCheck,
                            onDismiss = viewModel::dismissAllHealthCheck,
                            modifier = Modifier.padding(top = 12.dp),
                        )
                    }
                }
                item {
                    StatusCard(
                        state = status.backendState,
                        online = self?.online == true,
                        hostName = self?.name ?: stringResource(R.string.unknown),
                        ip = self?.ipv4 ?: self?.ipv6 ?: "—",
                        modifier = Modifier.padding(top = 12.dp),
                    )
                }
                item {
                    Button(
                        onClick = viewModel::toggleTailscale,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp),
                    ) {
                        Text(
                            if (isRunning || status.backendState is BackendState.NeedsLogin)
                                stringResource(R.string.stop_tailscale)
                            else
                                stringResource(R.string.start_tailscale),
                        )
                    }
                }
                items(status.peers, key = { it.name + it.rawHostName }) { peer ->
                    DeviceCard(device = peer, onClick = { onPeerClick(peer) })
                }
            }
        }
        }
    }
}