package io.github.tanakalun.tailcontrol.share

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.ui.component.DeviceCard
import top.yukonga.miuix.kmp.basic.Button
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.LinearProgressIndicator
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.basic.TopAppBar
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.theme.MiuixTheme

@Composable
fun FileShareScreen(
    uri: Uri,
    onClose: () -> Unit,
    viewModel: FileShareViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    LaunchedEffect(uri) { viewModel.bind(uri) }
    val progress = ui.progressPercent.coerceIn(0, 100) / 100f

    Scaffold(
        topBar = { TopAppBar(title = ui.fileName.ifBlank { stringResource(R.string.share_label) }) },
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize().padding(start = 12.dp, end = 12.dp)) {
            Spacer(Modifier.height(8.dp))

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        stringResource(
                            R.string.transfer_status,
                            ui.progressText.ifBlank { stringResource(R.string.waiting) },
                        ),
                        fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
                    )
                    Spacer(Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth(),
                    )
                    if (ui.transferring) {
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = viewModel::openCancelDialog) {
                            Text(stringResource(R.string.force_cancel_transfer))
                        }
                    }
                }
            }

            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(stringResource(R.string.device_list_autofresh, ui.countdown))
                Button(onClick = viewModel::refreshOnce) { Text(stringResource(R.string.refresh)) }
            }

            LazyColumn(
                modifier = Modifier.fillMaxWidth().weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(ui.peers, key = { it.name }) { peer ->
                    Box(Modifier.fillMaxWidth()) {
                        DeviceCard(device = peer, onClick = {
                            if (!ui.transferring && peer.online) viewModel.send(peer)
                        })
                    }
                }
            }
        }
    }

    if (ui.showNotRunningDialog) {
        OverlayDialog(
            show = ui.showNotRunningDialog,
            title = "Tailscale ${stringResource(R.string.status_service_stopped)}",
            summary = stringResource(R.string.tailscale_not_running),
            onDismissRequest = viewModel::dismissNotRunning,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cancel),
                        onClick = viewModel::dismissNotRunning,
                    )
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.start_tailscale),
                        onClick = {
                            viewModel.dismissNotRunning()
                            viewModel.startTailscale()
                        },
                    )
                }
            },
        )
    }
    if (ui.showOfflineDialog) {
        OverlayDialog(
            show = ui.showOfflineDialog,
            title = "Tailscale ${stringResource(R.string.offline_now)}",
            summary = stringResource(R.string.tailnet_unreachable),
            onDismissRequest = viewModel::dismissOffline,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.done),
                        onClick = viewModel::dismissOffline,
                    )
                }
            },
        )
    }
    if (ui.cancelDialogOpen) {
        OverlayDialog(
            show = ui.cancelDialogOpen,
            title = stringResource(R.string.force_cancel_title),
            summary = stringResource(R.string.force_cancel_warn),
            onDismissRequest = viewModel::dismissCancelDialog,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cancel),
                        onClick = viewModel::dismissCancelDialog,
                    )
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.confirm),
                        onClick = { viewModel.confirmCancel(onClose) },
                    )
                }
            },
        )
    }
    if (ui.processingDialog) {
        OverlayDialog(
            show = ui.processingDialog,
            title = stringResource(R.string.drop_canceling),
            summary = stringResource(R.string.cancel_please_wait),
            onDismissRequest = {},
        ) {}
    }
    if (ui.doneDialog) {
        OverlayDialog(
            show = ui.doneDialog,
            title = stringResource(R.string.operation_complete),
            summary = stringResource(R.string.tailreboot),
            onDismissRequest = { viewModel.dismissDoneDialog(); onClose() },
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.done),
                        onClick = { viewModel.dismissDoneDialog(); onClose() },
                    )
                }
            },
        )
    }
}