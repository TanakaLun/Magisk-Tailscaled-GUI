package io.github.tanakalun.tailcontrol.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.core.model.TailscaleDevice
import io.github.tanakalun.tailcontrol.ui.screen.peer.toLocalTime
import io.github.tanakalun.tailcontrol.ui.theme.LocalStatusColors
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Icon
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.ChevronForward
import top.yukonga.miuix.kmp.theme.MiuixTheme
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter


fun String.toLocalTime(): String {
    return try {
        LocalDateTime.parse(trim(), DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
            .atZone(ZoneId.of("UTC"))
            .withZoneSameInstant(ZoneId.systemDefault())
            .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
    } catch (e: Exception) {
        "$this (UTC)"
    }
}

@Composable
fun DeviceCard(
    device: TailscaleDevice,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val statusColors = LocalStatusColors.current
    val ip = device.ipv4 ?: device.ipv6
    val ipText = ip ?: "—"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onClick,
        showIndication = true,
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f)) {
                Text(device.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("IP: $ipText", fontSize = 13.sp)
                    Spacer(Modifier.width(2.dp))
                    CopyIpButton(ip = ip)
                }
                if (!device.online && !device.lastSeen.isNullOrBlank()) {
                    Text(
                        "${stringResource(R.string.last_seen)}: ${device.lastSeen.toLocalTime()}",
                        fontSize = 12.sp,
                    )
                }
            }
            Column(
                modifier = Modifier.padding(start = 16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                StatusTag(
                    label = stringResource(
                        if (device.online) R.string.status_online else R.string.status_offline
                    ),
                    backgroundColor = if (device.online) {
                        statusColors.online.copy(alpha = 0.15f)
                    } else {
                        statusColors.offline.copy(alpha = 0.15f)
                    },
                    contentColor = if (device.online) statusColors.online else statusColors.offline,
                )
                StatusTag(
                    label = device.os,
                    backgroundColor = MiuixTheme.colorScheme.secondaryContainer,
                    contentColor = MiuixTheme.colorScheme.onSecondaryContainer,
                )
            }
            Icon(
                MiuixIcons.ChevronForward,
                contentDescription = null,
                modifier = Modifier.padding(start = 8.dp),
            )
        }
    }
}