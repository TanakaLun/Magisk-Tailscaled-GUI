package io.github.tanakalun.tailcontrol.ui.nav

import kotlinx.serialization.Serializable
import top.yukonga.miuix.kmp.nav.core.NavKey

@Serializable
sealed interface Route : NavKey {
    @Serializable
    data object Main : Route

    @Serializable
    data object Accounts : Route

    @Serializable
    data object Logs : Route

    @Serializable
    data object ExitNodePicker : Route

    @Serializable
    data object SubnetEditor : Route

    @Serializable
    data object Experimental : Route

    @Serializable
    data object NavBarCustomizer : Route

    @Serializable
    data class PeerDetail(val name: String) : Route
}