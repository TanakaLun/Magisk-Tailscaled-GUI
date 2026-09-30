package io.github.tanakalun.tailcontrol.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.core.manager.UpdateChecker
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.TextButton
import top.yukonga.miuix.kmp.window.WindowDialog

@Composable
fun UpdateDialog(
    result: UpdateChecker.CheckUpdateResult?,
    onDismiss: () -> Unit,
    onOpenDownloadPage: () -> Unit,
) {
    result?.let { updateResult ->
        WindowDialog(
            show = true,
            title = stringResource(R.string.update_found_title, updateResult.updateInfo.versionNumber),
            onDismissRequest = onDismiss,
        ) {
            Column {
                Text(stringResource(R.string.update_current_version, updateResult.currentVersion))
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(R.string.update_release_notes),
                    fontWeight = FontWeight.Bold,
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = updateResult.updateInfo.body,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState()),
                )
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.update_later),
                        onClick = onDismiss,
                    )
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.update_now),
                        onClick = {
                            onOpenDownloadPage()
                            onDismiss()
                        },
                    )
                }
            }
        }
    }
}