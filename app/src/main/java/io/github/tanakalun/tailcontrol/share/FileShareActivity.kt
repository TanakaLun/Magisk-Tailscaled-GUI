package io.github.tanakalun.tailcontrol.share

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint
import io.github.tanakalun.tailcontrol.R
import io.github.tanakalun.tailcontrol.core.shell.RootShell
import kotlinx.coroutines.launch
import io.github.tanakalun.tailcontrol.ui.component.RootDeniedDialog
import io.github.tanakalun.tailcontrol.ui.theme.TailControlTheme
import javax.inject.Inject

@AndroidEntryPoint
class FileShareActivity : ComponentActivity() {

    @Inject lateinit var rootShell: RootShell

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        if (Build.VERSION.SDK_INT >= 33) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val uri: Uri? = if (Build.VERSION.SDK_INT >= 33) {
            intent?.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
        } else {
            @Suppress("DEPRECATION") intent?.getParcelableExtra(Intent.EXTRA_STREAM)
        }

        if (uri == null) {
            finish()
            return
        }

        setContent {
            TailControlTheme {
                RootGate(rootShell = rootShell, onExit = { finishAffinity() }) {
                    FileShareScreen(
                        uri = uri,
                        onClose = { finishAffinity() },
                    )
                }
            }
        }
    }
}

@Composable
private fun RootGate(
    rootShell: RootShell,
    onExit: () -> Unit,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    var rootGranted by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        rootGranted = rootShell.isRoot()
    }
    if (!rootGranted) {
        RootDeniedDialog(
            onRetry = {
                scope.launch { rootGranted = rootShell.isRoot() }
            },
            onExit = onExit,
        )
    } else {
        content()
    }
}
