package io.github.tanakalun.tailcontrol.ui.nav

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Article
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.ui.graphics.vector.ImageVector
import io.github.tanakalun.tailcontrol.R
import top.yukonga.miuix.kmp.icon.MiuixIcons
import top.yukonga.miuix.kmp.icon.extended.Home
import top.yukonga.miuix.kmp.icon.extended.Send
import top.yukonga.miuix.kmp.icon.extended.Settings

enum class TopLevelDestination(
    val icon: ImageVector,
    @param:StringRes val labelRes: Int,
    val showInBar: Boolean = true,
) {
    Home(MiuixIcons.Home, R.string.nav_home, true),
    Drop(MiuixIcons.Send, R.string.nav_drop, true),
    Netcheck(Icons.Filled.NetworkCheck, R.string.nav_netcheck, true),
    Traffic(Icons.Filled.Analytics, R.string.nav_traffic, true),
    Settings(MiuixIcons.Settings, R.string.nav_settings, true),
    Accounts(Icons.Filled.PeopleAlt, R.string.nav_accounts, false),
    Logs(Icons.AutoMirrored.Filled.Article, R.string.nav_logs, false),
}