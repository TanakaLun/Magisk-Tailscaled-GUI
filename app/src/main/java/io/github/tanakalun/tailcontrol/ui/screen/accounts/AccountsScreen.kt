package io.github.tanakalun.tailcontrol.ui.screen.accounts

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.core.model.AccountItem
import io.github.tanakalun.tailcontrol.ui.component.SectionHeader
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import top.yukonga.miuix.kmp.basic.BasicComponentDefaults
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.CardDefaults
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
import top.yukonga.miuix.kmp.overlay.OverlayDialog
import top.yukonga.miuix.kmp.preference.ArrowPreference
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.overScrollVertical
import top.yukonga.miuix.kmp.utils.scrollEndHaptic

@Composable
fun AccountsScreen(
    onBack: () -> Unit,
    viewModel: AccountsViewModel = hiltViewModel(),
) {
    val ui by viewModel.ui.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val layoutDirection = LocalLayoutDirection.current
    val scrollBehavior = MiuixScrollBehavior()
    val waitingLink = stringResource(R.string.waiting_link)
    val backdrop = rememberBlurBackdrop()
    val barColor = if (backdrop != null) ComposeColor.Transparent else MiuixTheme.colorScheme.surface

    Scaffold(
        topBar = {
            BlurredBar(backdrop = backdrop) {
                TopAppBar(
                    title = stringResource(R.string.tailscale_accounts),
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
        LazyColumn(
            Modifier
                .fillMaxSize()
                .nestedScroll(scrollBehavior.nestedScrollConnection)
                .overScrollVertical()
                .scrollEndHaptic(),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item {
                SectionHeader(stringResource(R.string.accounts_group_accounts))
            }
            items(ui.accounts, key = { it.id }) { item ->
                AccountCard(item) {
                    if (!item.isCurrent) {
                        Toast.makeText(context, context.getString(R.string.switching), Toast.LENGTH_SHORT).show()
                        viewModel.switchAccount(item.id)
                    }
                }
            }
            item {
                SectionHeader(stringResource(R.string.accounts_group_actions))
            }
            item {
                Card(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp),
                ) {
                    Column {
                        ArrowPreference(
                            title = stringResource(R.string.admin_console),
                            onClick = {
                                runCatching {
                                    context.startActivity(
                                        Intent(Intent.ACTION_VIEW, "https://tailscale.com/admin".toUri())
                                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                    )
                                }.onFailure {
                                    Toast.makeText(context, R.string.unable_browser, Toast.LENGTH_SHORT).show()
                                }
                            },
                        )
                        ArrowPreference(
                            title = stringResource(R.string.add_new_acc),
                            onClick = {
                                viewModel.startLoginWithSavedSettings(waitingLink)
                            },
                        )
                        if (ui.accounts.any { it.isCurrent }) {
                            ArrowPreference(
                                title = stringResource(R.string.logout),
                                titleColor = BasicComponentDefaults.titleColor(color = MiuixTheme.colorScheme.error),
                                onClick = viewModel::openLogoutDialog,
                            )
                        }
                    }
                }
            }
        }
        }
    }

    if (ui.loginDialogOpen) {
        OverlayDialog(
            show = ui.loginDialogOpen,
            title = "Tailscale ${stringResource(R.string.login)}",
            onDismissRequest = { },
            content = {
                Column {
                    SelectionContainer {
                        Text(ui.loginDialogText)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth()) {
                        if (ui.loginSuccess) {
                            TextButton(
                                modifier = Modifier.fillMaxWidth(),
                                text = stringResource(R.string.done),
                                onClick = viewModel::closeLoginDialog,
                            )
                        } else {
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.copy_open),
                                onClick = {
                                    if (ui.loginUrl.isNotEmpty()) {
                                        copyAndOpen(context, ui.loginUrl)
                                    }
                                },
                            )
                            TextButton(
                                modifier = Modifier.weight(1f),
                                text = stringResource(R.string.cancel),
                                onClick = viewModel::closeLoginDialog,
                            )
                        }
                    }
                }
            },
        )
    }

    if (ui.logoutDialogOpen) {
        OverlayDialog(
            show = ui.logoutDialogOpen,
            title = stringResource(R.string.confirm_sign_out),
            summary = stringResource(R.string.confirm_sign_out_text),
            onDismissRequest = viewModel::dismissLogoutDialog,
            content = {
                Row(Modifier.fillMaxWidth()) {
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.cancel),
                        onClick = viewModel::dismissLogoutDialog,
                    )
                    TextButton(
                        modifier = Modifier.weight(1f),
                        text = stringResource(R.string.logout),
                        onClick = viewModel::confirmLogout,
                    )
                }
            },
        )
    }
}

@Composable
private fun AccountCard(item: AccountItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        onClick = onClick,
        colors = if (item.isCurrent) {
            CardDefaults.defaultColors(color = MiuixTheme.colorScheme.primaryContainer)
        } else {
            CardDefaults.defaultColors()
        },
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(item.account, fontWeight = FontWeight.SemiBold)
            if (item.isCurrent) {
                Text(
                    stringResource(R.string.current),
                    fontSize = 13.sp,
                    color = MiuixTheme.colorScheme.primary,
                )
            }
        }
    }
}

private fun copyAndOpen(ctx: Context, url: String) {
    val cm = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    cm.setPrimaryClip(ClipData.newPlainText("Tailscale Login", url))
    Toast.makeText(ctx, R.string.url_copied, Toast.LENGTH_SHORT).show()
    runCatching {
        ctx.startActivity(Intent(Intent.ACTION_VIEW, url.toUri()).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
    }.onFailure {
        Toast.makeText(ctx, "${ctx.getString(R.string.unable_browser)}: ${it.message}", Toast.LENGTH_SHORT).show()
    }
}