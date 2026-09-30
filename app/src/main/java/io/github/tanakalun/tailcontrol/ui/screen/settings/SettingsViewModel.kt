package io.github.tanakalun.tailcontrol.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import io.github.tanakalun.tailcontrol.core.data.BinaryPaths
import io.github.tanakalun.tailcontrol.core.data.PreferencesRepository
import io.github.tanakalun.tailcontrol.core.data.TailscaleRepository
import io.github.tanakalun.tailcontrol.core.model.DnsStatus
import io.github.tanakalun.tailcontrol.core.model.TailscaleSettings
import javax.inject.Inject

data class SettingsUiState(
    val settings: TailscaleSettings = TailscaleSettings(),
    val username: String = "",
    val isLoggedIn: Boolean = false,
    val saveError: String? = null,
    val saveSuccess: Boolean = false,
    val colorMode: Int = PreferencesRepository.DEFAULT_COLOR_MODE,
    val dnsStatus: DnsStatus? = null,
    val sshServerEnabled: Boolean = false,
    val sshUpdating: Boolean = false,
    val binaries: BinaryPaths? = null,
    val dirty: Boolean = false,
    val refreshing: Boolean = false,
    val enableBlur: Boolean = true,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: PreferencesRepository,
    private val tailRepo: TailscaleRepository,
) : ViewModel() {

    private val _ui = MutableStateFlow(SettingsUiState())
    val ui: StateFlow<SettingsUiState> = _ui.asStateFlow()

    /** 首次获取账户信息后置位，避免对 entry/组合重建重复拉取。 */
    private var identityLoaded = false

    init {
        viewModelScope.launch {
            prefs.tailscaleSettings.collect { _ui.value = _ui.value.copy(settings = it) }
        }
        viewModelScope.launch {
            prefs.colorMode.collect { _ui.value = _ui.value.copy(colorMode = it) }
        }
        viewModelScope.launch {
            prefs.enableBlur.collect { _ui.value = _ui.value.copy(enableBlur = it) }
        }
        viewModelScope.launch {
            prefs.sshServerEnabled.collect {
                _ui.value = _ui.value.copy(sshServerEnabled = it)
                // 确保系统实际状态与 UI 状态同步
                if (it) {
                    tailRepo.setSshServer(true)
                }
            }
        }
        refreshIdentity()
    }

    /** 首次加载自动拉取一次；[force] 为 PullToRefresh 手动触发时强制重拉。 */
    fun refreshIdentity(force: Boolean = false) {
        if (identityLoaded && !force) return
        identityLoaded = true
        _ui.value = _ui.value.copy(refreshing = true)
        viewModelScope.launch(Dispatchers.IO) {
            val status = tailRepo.fetchStatus()
            val self = status.self
            val isLoggedIn = self != null && status.backendState !is io.github.tanakalun.tailcontrol.core.model.BackendState.NeedsLogin
            val displayName = self?.userId?.let { uid -> status.users[uid]?.displayName }.orEmpty()
            _ui.value = _ui.value.copy(
                isLoggedIn = isLoggedIn,
                username = displayName.ifEmpty { self?.name.orEmpty() },
            )
            // DNS 状态
            runCatching {
                _ui.value = _ui.value.copy(dnsStatus = tailRepo.dnsStatus())
            }
            // 二进制实际路径（兜底 PATH 之后 shell 会调用的那个）
            runCatching {
                _ui.value = _ui.value.copy(binaries = tailRepo.resolveBinaries())
            }
            _ui.value = _ui.value.copy(refreshing = false)
        }
    }

    fun setSshServer(enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            _ui.value = _ui.value.copy(sshUpdating = true)
            val r = tailRepo.setSshServer(enabled)
            if (r.ok) {
                prefs.setSshServerEnabled(enabled)
                _ui.value = _ui.value.copy(sshServerEnabled = enabled, sshUpdating = false)
            } else {
                _ui.value = _ui.value.copy(sshUpdating = false, saveError = r.text.ifBlank { "ssh toggle failed" })
            }
        }
    }

    fun update(update: (TailscaleSettings) -> TailscaleSettings) {
        _ui.value = _ui.value.copy(
            settings = update(_ui.value.settings),
            dirty = true,
        )
    }

    fun save() {
        viewModelScope.launch(Dispatchers.IO) {
            val s = _ui.value.settings
            val r = tailRepo.set(s.toCliArgs())
            if (!r.ok && r.text.isNotBlank()) {
                _ui.value = _ui.value.copy(saveError = r.text)
            } else {
                prefs.saveTailscaleSettings(s)
                _ui.value = _ui.value.copy(saveSuccess = true, dirty = false)
            }
        }
    }

    fun consumeSaveError() { _ui.value = _ui.value.copy(saveError = null) }
    fun consumeSaveSuccess() { _ui.value = _ui.value.copy(saveSuccess = false) }

    fun setColorMode(index: Int) {
        viewModelScope.launch { prefs.setColorMode(index) }
    }

    fun setEnableBlur(enabled: Boolean) {
        viewModelScope.launch { prefs.setEnableBlur(enabled) }
    }
}
