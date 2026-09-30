package io.github.tanakalun.tailcontrol

import android.Manifest
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color as ComposeColor
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import io.github.tanakalun.tailcontrol.core.data.PreferencesRepository
import io.github.tanakalun.tailcontrol.core.shell.RootShell
import io.github.tanakalun.tailcontrol.ui.component.RootDeniedDialog
import io.github.tanakalun.tailcontrol.ui.component.UpdateDialog
import io.github.tanakalun.tailcontrol.ui.nav.MainPagerState
import io.github.tanakalun.tailcontrol.ui.nav.Navigator
import io.github.tanakalun.tailcontrol.ui.nav.Route
import io.github.tanakalun.tailcontrol.ui.nav.TopLevelDestination
import io.github.tanakalun.tailcontrol.ui.nav.rememberMainPagerState
import io.github.tanakalun.tailcontrol.ui.nav.shouldExpandNavigationRail
import io.github.tanakalun.tailcontrol.ui.nav.shouldShowSplitPane
import io.github.tanakalun.tailcontrol.ui.screen.accounts.AccountsScreen
import io.github.tanakalun.tailcontrol.ui.screen.drop.DropScreen
import io.github.tanakalun.tailcontrol.ui.screen.home.HomeScreen
import io.github.tanakalun.tailcontrol.ui.screen.log.LogScreen
import io.github.tanakalun.tailcontrol.ui.screen.netcheck.NetcheckScreen
import io.github.tanakalun.tailcontrol.ui.screen.peer.PeerDetailScreen
import io.github.tanakalun.tailcontrol.ui.screen.settings.SettingsScreen
import io.github.tanakalun.tailcontrol.ui.screen.settings.UpdateViewModel
import io.github.tanakalun.tailcontrol.ui.screen.settings.exit.ExitNodePickerScreen
import io.github.tanakalun.tailcontrol.ui.screen.settings.experimental.ExperimentalScreen
import io.github.tanakalun.tailcontrol.ui.screen.settings.experimental.NavBarCustomizerScreen
import io.github.tanakalun.tailcontrol.ui.screen.settings.experimental.NavBarCustomizerViewModel
import io.github.tanakalun.tailcontrol.ui.screen.settings.subnet.SubnetEditorScreen
import io.github.tanakalun.tailcontrol.ui.screen.traffic.TrafficScreen
import io.github.tanakalun.tailcontrol.ui.theme.BlurredBar
import io.github.tanakalun.tailcontrol.ui.theme.NavBarBlur
import io.github.tanakalun.tailcontrol.ui.theme.TailControlTheme
import io.github.tanakalun.tailcontrol.ui.theme.LocalEnableBlur
import io.github.tanakalun.tailcontrol.ui.theme.isInDarkTheme
import io.github.tanakalun.tailcontrol.ui.theme.rememberBlurBackdrop
import kotlinx.coroutines.launch
import top.yukonga.miuix.kmp.basic.NavigationBar
import top.yukonga.miuix.kmp.basic.NavigationBarItem
import top.yukonga.miuix.kmp.basic.NavigationRail
import top.yukonga.miuix.kmp.basic.NavigationRailItem
import top.yukonga.miuix.kmp.basic.NavigationRailValue
import top.yukonga.miuix.kmp.basic.Scaffold
import top.yukonga.miuix.kmp.basic.rememberNavigationRailState
import top.yukonga.miuix.kmp.blur.layerBackdrop
import top.yukonga.miuix.kmp.nav.core.NavCornerClipMode
import top.yukonga.miuix.kmp.nav.core.NavDisplay
import top.yukonga.miuix.kmp.nav.core.NavDisplayEffects
import top.yukonga.miuix.kmp.nav.core.rememberNavBackStack
import top.yukonga.miuix.kmp.nav.core.rememberNavSystemCornerRadius
import top.yukonga.miuix.kmp.nav.transition.NavSwipeDirection
import top.yukonga.miuix.kmp.nav.transition.NavTransitions
import top.yukonga.miuix.kmp.theme.MiuixTheme
import top.yukonga.miuix.kmp.utils.PagerGestureNestedScrollConnection
import top.yukonga.miuix.kmp.utils.PagerInterceptionMode
import top.yukonga.miuix.kmp.utils.pagerGestureOverride
import javax.inject.Inject

val LocalNavigator = staticCompositionLocalOf<Navigator> { error("No navigator provided!") }

/** 全部 tab 的目的地（与 pager 页序严格一致）。 */
private val allTabDestinations: List<TopLevelDestination> =
    TopLevelDestination.entries.filter { it.showInBar }

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var prefs: PreferencesRepository
    @Inject lateinit var rootShell: RootShell

    private val notifPermission = registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* ignore */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 33) {
            notifPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        setContent {
            val colorMode by prefs.colorMode.collectAsState(initial = PreferencesRepository.DEFAULT_COLOR_MODE)
            val enableBlur by prefs.enableBlur.collectAsState(initial = true)
            TailControlTheme(colorMode = colorMode) {
                CompositionLocalProvider(LocalEnableBlur provides enableBlur) {
                    MainActivityContent()
                }
            }
        }
    }

    @Composable
    fun MainActivityContent() {
        val scope = rememberCoroutineScope()
        var rootDenied by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            rootDenied = !rootShell.isRoot()
        }

        val updateViewModel: UpdateViewModel = viewModel()
        val silentUpdate by updateViewModel.silentUpdateResult.collectAsState()
        var showDialog by remember { mutableStateOf(false) }

        LaunchedEffect(Unit) {
            lifecycleScope.launch {
                updateViewModel.silentCheckUpdateIfNeeded()
            }
        }

        LaunchedEffect(silentUpdate) {
            if (silentUpdate != null) {
                showDialog = true
            }
        }
        if (showDialog && silentUpdate != null) {
            UpdateDialog(
                result = silentUpdate,
                onDismiss = { updateViewModel.consumeSilentUpdate() },
                onOpenDownloadPage = { updateViewModel.openDownloadPage() },
            )
        }

        val dark = isInDarkTheme()
        DisposableEffect(dark) {
            enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
            )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }
            onDispose {}
        }

        MainNavHost()

        if (rootDenied) {
            RootDeniedDialog(
                onRetry = {
                    scope.launch { rootDenied = !rootShell.isRoot() }
                },
                onExit = { finish() },
            )
        }
    }
}

@Composable
fun MainNavHost() {
    val navBarViewModel: NavBarCustomizerViewModel = hiltViewModel()
    val navBarUi by navBarViewModel.ui.collectAsStateWithLifecycle()

    val backStack = rememberNavBackStack<Route>(Route.Main)
    val navigator = remember { Navigator(backStack) }

    val pagerState = rememberPagerState(pageCount = { allTabDestinations.size })
    val mainPagerState = rememberMainPagerState(pagerState)
    val isSubPage = backStack.size > 1

    LaunchedEffect(pagerState.currentPage) {
        mainPagerState.syncPage()
    }

    // 可见 tab（受 NavBarCustomizer 隐藏项控制）
    val visibleDestinations = remember(navBarUi.hiddenItems) {
        allTabDestinations.filter { it.name !in navBarUi.hiddenItems }
    }
    val pageIndexOf: Map<TopLevelDestination, Int> =
        allTabDestinations.withIndex().associate { (index, dest) -> dest to index }

    // 返回处理：栈内只有 Main 时，pager 不在首页则先回首页；否则交给 NavDisplay 弹出子页。
    BackHandler(enabled = !isSubPage && mainPagerState.selectedPage != 0) {
        mainPagerState.animateToPage(0)
    }

    val navCornerRadius = rememberNavSystemCornerRadius()
    val surfaceColor = MiuixTheme.colorScheme.surface
    val effects = remember(navCornerRadius, surfaceColor) {
        NavDisplayEffects(
            enableCornerClip = true,
            cornerClipRadius = navCornerRadius,
            cornerClipMode = NavCornerClipMode.Leading,
            dimAmount = 0.45f,
            backdropColor = surfaceColor,
        )
    }
    val swipeBackDirection = when {
        LocalLayoutDirection.current == LayoutDirection.Rtl -> NavSwipeDirection.RightToLeft
        else -> NavSwipeDirection.LeftToRight
    }

    val isWideScreen = shouldShowSplitPane()

    CompositionLocalProvider(LocalNavigator provides navigator) {
        Row(Modifier.fillMaxSize()) {
            if (isWideScreen) {
                PersistentNavigationRail(
                    navigationItems = visibleDestinations,
                    pageIndexOf = pageIndexOf,
                    mainPagerState = mainPagerState,
                    navigator = navigator,
                )
            }
            Box(
                Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .then(if (isWideScreen) Modifier.clipToBounds() else Modifier),
            ) {
                NavDisplay(
                    backStack = backStack,
                    onBack = { if (backStack.size > 1) navigator.pop() },
                    transition = NavTransitions.MiuixDefault,
                    effects = effects,
                ) {
                    entry<Route.Main>(swipeDismiss = NavSwipeDirection.None) {
                        HomeMain(
                            navigationItems = visibleDestinations,
                            pageIndexOf = pageIndexOf,
                            mainPagerState = mainPagerState,
                            isWideScreen = isWideScreen,
                        )
                    }
                    entry<Route.Accounts>(swipeDismiss = swipeBackDirection) {
                        AccountsScreen(onBack = { navigator.pop() })
                    }
                    entry<Route.Logs>(swipeDismiss = swipeBackDirection) {
                        LogScreen(onBack = { navigator.pop() })
                    }
                    entry<Route.ExitNodePicker>(swipeDismiss = swipeBackDirection) {
                        ExitNodePickerScreen(onBack = { navigator.pop() })
                    }
                    entry<Route.SubnetEditor>(swipeDismiss = swipeBackDirection) {
                        SubnetEditorScreen(onBack = { navigator.pop() })
                    }
                    entry<Route.Experimental>(swipeDismiss = swipeBackDirection) {
                        ExperimentalScreen(
                            onBack = { navigator.pop() },
                            onOpenNavBarCustomizer = { navigator.push(Route.NavBarCustomizer) },
                        )
                    }
                    entry<Route.NavBarCustomizer>(swipeDismiss = swipeBackDirection) {
                        NavBarCustomizerScreen(onBack = { navigator.pop() })
                    }
                    entry<Route.PeerDetail>(swipeDismiss = swipeBackDirection) { route ->
                        PeerDetailScreen(
                            peerName = route.name,
                            onBack = { navigator.pop() },
                        )
                    }
                }
            }
        }
    }
}

/** compact：外层 Scaffold 底部是 NavigationBar；宽屏：只有内容，rail 由外层提供。 */
@Composable
private fun HomeMain(
    navigationItems: List<TopLevelDestination>,
    pageIndexOf: Map<TopLevelDestination, Int>,
    mainPagerState: MainPagerState,
    isWideScreen: Boolean,
) {
    val backdrop = rememberBlurBackdrop()
    val barColor = if (backdrop != null) ComposeColor.Transparent else MiuixTheme.colorScheme.surface

    if (isWideScreen) {
        PagerContent(mainPagerState, pageIndexOf, bottomInset = 0.dp)
        return
    }

    Scaffold(
        bottomBar = {
            NavBarBlur(backdrop = backdrop, barColor = barColor) {
                NavigationBar(color = barColor) {
                    navigationItems.forEach { dest ->
                        val index = pageIndexOf[dest] ?: return@forEach
                        NavigationBarItem(
                            selected = mainPagerState.selectedPage == index,
                            onClick = { mainPagerState.animateToPage(index) },
                            icon = dest.icon,
                            label = stringResource(dest.labelRes),
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        val bottomInset = innerPadding.calculateBottomPadding()
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(if (backdrop != null) Modifier.layerBackdrop(backdrop) else Modifier),
        ) {
            PagerContent(
                mainPagerState,
                pageIndexOf,
                bottomInset = bottomInset,
            )
        }
    }
}

@Composable
private fun PagerContent(
    mainPagerState: MainPagerState,
    pageIndexOf: Map<TopLevelDestination, Int>,
    bottomInset: Dp,
) {
    HorizontalPager(
        state = mainPagerState.pagerState,
        modifier = Modifier
            .fillMaxSize()
            .pagerGestureOverride(
                pagerState = mainPagerState.pagerState,
                mode = PagerInterceptionMode.CrossAxisInterceptor,
                enabled = true,
            ),
        userScrollEnabled = false,
        pageNestedScrollConnection = PagerGestureNestedScrollConnection,
    ) { page ->
        Box(
            Modifier
                .fillMaxSize(),
        ) {
            PagerScreen(page = page, bottomInset = bottomInset)
        }
    }
}

@Composable
private fun PagerScreen(page: Int, bottomInset: Dp) {
    val navigator = LocalNavigator.current
    when (page) {
        0 -> HomeScreen(
            onPeerClick = { navigator.push(Route.PeerDetail(it.name)) },
            onOpenAccounts = { navigator.push(Route.Accounts) },
            onOpenLogs = { navigator.push(Route.Logs) },
            bottomInset = bottomInset,
        )
        1 -> DropScreen(bottomInset = bottomInset)
        2 -> NetcheckScreen(bottomInset = bottomInset)
        3 -> TrafficScreen(bottomInset = bottomInset)
        4 -> SettingsScreen(
            onOpenAccounts = { navigator.push(Route.Accounts) },
            onOpenExitNode = { navigator.push(Route.ExitNodePicker) },
            onOpenSubnet = { navigator.push(Route.SubnetEditor) },
            onOpenExperimental = { navigator.push(Route.Experimental) },
            onOpenLogs = { navigator.push(Route.Logs) },
            bottomInset = bottomInset,
        )
    }
}

@Composable
private fun PersistentNavigationRail(
    navigationItems: List<TopLevelDestination>,
    pageIndexOf: Map<TopLevelDestination, Int>,
    mainPagerState: MainPagerState,
    navigator: Navigator,
) {
    val expandRail = shouldExpandNavigationRail()
    val railState = rememberNavigationRailState(
        initialValue = if (expandRail) NavigationRailValue.Expanded else NavigationRailValue.Collapsed,
    )
    LaunchedEffect(expandRail) {
        if (expandRail) railState.expand() else railState.collapse()
    }
    NavigationRail(state = railState) {
        navigationItems.forEach { dest ->
            val index = pageIndexOf[dest] ?: return@forEach
            NavigationRailItem(
                selected = mainPagerState.selectedPage == index,
                onClick = {
                    navigator.popUntil { it is Route.Main }
                    mainPagerState.animateToPage(index)
                },
                icon = dest.icon,
                label = stringResource(dest.labelRes),
            )
        }
    }
}