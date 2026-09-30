# TailControl 重构生产计划（Production PLAN）

> 依据旧版 PLAN.md 扩展为可执行的生产计划。
> 目标：将本项目完全重写为 **Miuix（HyperOS 设计语言）**、**MVVM 架构** 的高性能 Android 应用。
> 新包名：`io.github.tanakalun.tailcontrol`

---

## 1. 项目背景与目标

Magisk-Tailscaled-GUI（现名 TailControl）是一个 **root 态 Tailscale 图形化控制台** + TailDrop（文件接收/发送）工具。
当前实现基于 Material 3 + Navigation-Compose + NavigationSuite，UI 粗糙、主题与组件均为手写 M3 原语、动画混用默认 Compose 动画。

生产目标（旧 PLAN 要点）：
1. **完全切换到 Miuix 组件体系**：`miuix-ui`、`miuix-preference`、`miuix-nav`、`miuix-icons`、`miuix-blur`。
2. **导航完全切换至 miuix-nav**：不使用 Compose 默认导航/动画，**严格对齐 `miuix-repo/example` 的导航行为**（`NavKey` + `NavBackStack` + `NavDisplay`）。
3. **Navigation Bar / Rail**：正确实现 example 中的 compact `NavigationBar` 与宽屏 `NavigationRail`（`NavigationRailValue` + `rememberNavigationRailState`）样式。
4. **MVVM 架构**：保持 Hilt + Repository + ViewModel 分层，新增接口抽象与显式 Hilt Module。
5. **根权限操作封装**：统一依赖 **libsu** 封装抽象层（`RootShell`），不直接操作 `su`；并将 `FileShareViewModel` 中绕过 libsu 的原生 `Runtime.exec("su")` 收口到统一下层。
6. **StatusCard 样式**：Home 页状态卡片按 `/storage/emulated/0/Projects/RikoNyamu/app/src/main/kotlin/io/tl/rikon/ui/screens/ConfigScreen.kt` 中的 `StatusCard()` 重构。

### 参考与证据源
- Miuix 源码 / 示例：`/data/user/0/com.termux/files/usr/tmp/opencode/miuix/`（`example/shared` 为对齐样本）。
- Miuix 标准示例导航：`AppState.kt` / `AppContent.kt` / `navigation/Navigator.kt` / `navigation/Route.kt` / `App.kt`。
- Gradle/依赖版本参考：`/storage/emulated/0/Projects/MyNotes`（miuix `0.9.4`，本计划采用同版本族）。
- StatusCard 模板：RikoNyamu `ConfigScreen.kt`。
- libsu 源码：`https://github.com/topjohnwu/libsu`（项目已含 version 6.0.0）。

---

## 2. 现状盘点（迁移输入，已审计）

### 2.1 当前 UI（Material 3 原语，全部待替换）

各屏幕当前 M3 组件与结构（迁移映射总表）：

| 屏幕 | 当前 M3 组件 | 迁移到 Miuix 组件 |
|---|---|---|
| Home（顶 Tab） | Scaffold、TopAppBar、Button、Canvas 状态点、PullToRefresh、DeviceCard | `Scaffold` + `TopAppBar` + `PullToRefresh` + 新 `StatusCard` + `Card` |
| Drop（顶 Tab） | ElevatedCard、Switch、OutlinedTextField、AlertDialog、DropdownMenu | `Card` + `SwitchPreference`/`InputField` + `OverlayDialog` |
| Netcheck（顶 Tab） | ElevatedCard、Button、SelectionContainer | `Card` + `Button` + `Selection` |
| Traffic（顶 Tab） | ElevatedCard、SpeedChart（Canvas） | `Card` + 自定义速度图（复用） |
| Settings（顶 Tab） | 11 张 ElevatedCard、Checkbox、ListItem、Switch、FAB | `miuix-preference` 体系（`SwitchPreference`/`ArrowPreference`/`SliderPreference`…） |
| Accounts（二级） | ElevatedCard、AlertDialog、RadioButton | `Card` + `List` + `OverlayDialog` |
| Logs（二级） | FilterChip、AssistChip、Snackbar、LazyColumn | `TabRow`/筛选 + `Card` |
| PeerDetail(name)（详情） | ElevatedCard、Button、Canvas | `Card` + `Button` |
| ExitNodePicker（二级） | RadioButton、ElevatedCard、AssistChip | 单选 `Dropdown`/`RadioButtonPreference` |
| SubnetEditor（二级） | OutlinedTextField 列表、FAB | `InputField` 列表 |
| Experimental（二级） | ElevatedCard、Switch、两个 Button | `Card` + `SwitchPreference` |
| NavBarCustomizer（二级） | ElevatedCard、Switch、Divider | `Card` + `SwitchPreference` |

> 现状：该应用**没有自定义 Material 设计体系**，全部为原始 M3 原语。替换相对直接，但每个屏幕都需重写外壳（`Scaffold` + `TopAppBar` + 列表结构在各屏幕内重复）。

### 2.2 导航现状
- AndroidX Navigation-Compose 2.9.8 + `NavigationSuiteScaffold`（自适应 bottom/rail）+ `@Serializable` 路由。
- `Destinations.kt`：11 个 route（10 个 `data object` + 1 个 `data class PeerDetail(name)`）。
- `TopLevelDestination` 枚举：5 个 `showInBar=true`（Home/Drop/Netcheck/Traffic/Settings）+ 2 个二级（Accounts/Logs）。
- **需迁移**为 miuix-nav：`NavBackStack` + pager 顶层 + `NavigationBar`/`NavigationRail`。

### 2.3 后端现状
- 仓库：`Drop`、`Log`、`NetcheckHistoryStore`、`PreferencesRepository`（DataStore）、`TailscaleRepository`（shell 主门面）、`TrafficRepository`。
- `RootShell`（`core/shell`）已基于 libsu，`@Singleton`；提供 `exec`/`stream`/`execText`；含 fallback PATH。
- `AltRepoRouteManager`、`UpdateChecker`（唯一网络来源，GitHub API）。
- `core/log`：`AppLogTree`（Timber→file）+ `AppLogReader`。
- 服务：`DropProtectService`、`FileTransferService`、`BootReceiver`、两个 Quick Settings Tile。
- 13 个 `@HiltViewModel`，无 Hilt `@Module`（主要靠构造注入）。

---

## 3. 项目骨架与构建配置重写

### 3.1 版本目录（`gradle/libs.versions.toml`）目标族（对齐 MyNotes）

```toml
[versions]
agp = "9.1.1"
kotlin = "2.4.10"
composeBom = "2026.06.01"
activity = "1.13.0"
lifecycle = "2.11.0"
core = "1.13.1"
core-splashscreen = "1.0.1"
kotlinx-serialization = "1.11.0"
datastore = "1.2.1"
hilt = "2.59.2"
hiltNav = "1.3.0"
libsu = "6.0.0"
timber = "5.0.1"
miuix = "0.9.4"
```

### 3.2 miuix 依赖坐标（目标 **0.9.4**，参考 MyNotes）

```toml
miuix-ui         = { group = "top.yukonga.miuix.kmp", name = "miuix-ui-android",        version.ref = "miuix" }
miuix-preference = { group = "top.yukonga.miuix.kmp", name = "miuix-preference",        version.ref = "miuix" }
miuix-nav        = { group = "top.yukonga.miuix.kmp", name = "miuix-nav-android",       version.ref = "miuix" }
miuix-icons      = { group = "top.yukonga.miuix.kmp", name = "miuix-icons-android",     version.ref = "miuix" }
miuix-blur       = { group = "top.yukonga.miuix.kmp", name = "miuix-blur-android",      version.ref = "miuix" }
```
> 实施第一步需在 `MyNotes` 与 `miuix-repo` 实际 `build.gradle.kts` 中逐条确认各坐标后缀（尤其 `miuix-preference` 是否带 `-android`）。

### 3.3 `app/build.gradle.kts` 变更
- `namespace` / `applicationId` → **`io.github.tanakalun.tailcontrol`**。
- 移除 `compose-material3*`、`material3-adaptive-nav`、`material3-window-size`、`material`、`material-icons-extended`。
- 保留：`hilt`、`datastore-preferences`、`kotlinx-serialization-json`、`timber`、`activity-compose`、`core-splashscreen`、`lifecycle-*`、`libsu-core`/`libsu-service`。
- 编译器 opt-in 移除 `ExperimentalMaterial3AdaptiveNavigationSuiteApi` 等 M3 相关。

### 3.4 `AndroidManifest.xml` / 资源
- 所有 `applicationId`、Activity/Service/Receiver/Tile 类路径随包名替换为 `io.github.tanakalun.tailcontrol`。
- 保留 `values/strings.xml`（235 条）沿用；图标资源沿用。

---

## 4. 主题（MiuixTheme）设计

目标：完全脱离 `MaterialTheme`，改用 `MiuixTheme` + `ThemeController` 模式（参考 `miuix-repo/example` 的 `ui/AppTheme.kt`、`App.kt`）。

- 新建 `ui/theme/MiuixThemeController.kt`：`rememberThemeController()` 管理亮/暗/动态色。
- `MiuixTheme(colorScheme = ...)` 替换 `MaterialTheme`；色板取 `MiuixTheme.colorScheme.*`。
- **保留现有 `StatusColors`（online/warning/offline/error/unknown）为应用级语义 `CompositionLocal`**（`LocalStatusColors`），包在 `MiuixTheme` 外层。
- `MainActivity`：`MiuixTheme { LocalStatusColors(...) { content } }`，移除 `MaterialTheme`。

> 不使用 Compose 默认动画；页面转场用 miuix `NavTransitions`（默认 / cross-activity）。

---

## 5. 导航架构（miuix-nav 对齐 example）

### 5.1 总体模型（对照 `example/shared`）
- **顶层 Tabs**（Home / Drop / Netcheck / Traffic / Settings）→ `HorizontalPager` + compact `NavigationBar` / 宽屏 `NavigationRail`，pager 页面与 Tab 同步。
- **二级/详情页**（Accounts、Logs、PeerDetail、ExitNodePicker、SubnetEditor、Experimental、NavBarCustomizer）→ `Navigator.push(Route.xxx)`。
- 路由类型 `sealed interface Route : NavKey`，子类全部 `@Serializable`：
  ```kotlin
  @Serializable sealed interface Route : NavKey {
      @Serializable data object Main : Route
      @Serializable data object Accounts : Route
      @Serializable data object Logs : Route
      @Serializable data object ExitNodePicker : Route
      @Serializable data object SubnetEditor : Route
      @Serializable data object Experimental : Route
      @Serializable data object NavBarCustomizer : Route
      @Serializable data class PeerDetail(val name: String) : Route
  }
  ```
- `val backStack = rememberNavBackStack<Route>(Route.Main)`（**必须显式 `<Route>`**，否则只能编码单例类型）。
- `Navigator(backStack)`：`push`/`replace`/`pop`/`popUntil`/`current`（复用 example 实现）。
- `NavDisplay(backStack, onBack = navigator::pop, transition, effects)` + `entry<Route.X> { }` 注册全部路由。

### 5.2 自适应 Tab / Rail（对照 `PersistentNavigationRail`）
- 宽屏（两窗格）左侧常驻 `NavigationRail`；`shouldExpandNavigationRail()` 决定初始 `NavigationRailValue`，`LaunchedEffect` 调 `expand()`/`collapse()`。
- `NavigationRailItem.onClick`：先 `navigator.popUntil { it is Route.Main }`，再 `mainPagerState.animateToPage(index)`。
- Rail 在 `NavDisplay` 之外常驻（`Row { Rail; Box(weight) { NavDisplay } }`），压栈二级页时 Rail 保持不变。
- Compact 底部 `NavigationBar`（可选 `FloatingNavigationBar`）同样驱动 pager。

### 5.3 Back 行为
- `NavigationBackHandler`：栈仅 `Main` 且 pager 非首页时，返回 → pager 回首页；其余 → `navigator.pop()`（交给 NavDisplay 预测性/边缘返回）。

### 5.4 Bar 项隐藏（NavBarCustomizer 映射）
- 原 `hiddenItems`（动画缩放/淡出）映射为：隐藏项不渲染；若需淡出用 `AnimatedVisibility`，**不使用 spring `graphicsLayer` 动画**（避免 Compose 默认动画）。

---

## 6. 屏幕级实现清单

### 6.1 共享组件（先抽 `ui/component/`）
1. **StatusCard（Home）**：对齐 RikoNyamu `StatusCard()` —— `Card(onClick={}, showIndication, pressFeedbackType=Tilt)`，`fillMaxWidth().padding(horizontal=12.dp)`，高 120.dp；右下角大状态图标（110.dp，偏移 27,31dp），左上标题（22sp SemiBold）+ 副行；状态色取自 `LocalStatusColors`。
2. `DeviceCard`：状态点 + 名称/IP/OS + 尾 `>`（`MiuixIcons`）。
3. `HealthBanner`、`SpeedChart`（Canvas 复用，改用 Miuix 主题 token）。
4. 通用 `OverlayDialog` 封装（替代 `AlertDialog`）。
5. `UpdateDialog` → `OverlayDialog`。
6. `CopyIpButton`/`CopyTextButton`（`MiuixIcons`）。
7. `SectionHeader` → `SmallTitle`。

### 6.2 各页映射（按优先级实施，逐步替换 M3 原语）

| 屏幕 | 关键实施 |
|---|---|
| **Home** | `Scaffold(topBar = TopAppBar)` → `PullToRefresh`；新 `StatusCard` → 全宽 `Button` → 自动刷新计数行 → `LazyColumn` 的 `DeviceCard`；逻辑保持 `HomeViewModel` |
| **Drop** | `Scaffold` + `TopAppBar("Tailscale Drop")`；`InputField`(文件夹)、`Dropdown`(冲突)、`Switch`、`Button`；结果 `OverlayDialog`；输出箱 `Card` 滚动 |
| **Netcheck** | `Button`(Run) + 结果 `Card`（`Selection`）+ 历史 `LazyColumn` 可展开 `Card` |
| **Traffic** | 两张 `Card`（速度图 + RX/TX 汇总） |
| **Settings** | 全面转 `miuix-preference`：`SwitchPreference`、`ArrowPreference`、`SliderPreference`、`OverlaySpinnerPreference`…按 `Card` 分组 + `SmallTitle`；保留更新检测行。**工作量最大** |
| **Accounts** | 账户 `Card` 列表 + 当前账户高亮；登录/登出 `OverlayDialog` |
| **Logs** | 来源筛选（Tab/FilterChip 三源）；`LazyColumn` 自动滚动；`OverlayDialog` 确认清空/删除 |
| **PeerDetail** | 身份/地址/whois/SSH/路由/ping 分 `Card`；ping 输出滚动 `Card` |
| **ExitNodePicker** | 单选列表（`Dropdown`/`RadioButtonPreference`）保存 |
| **SubnetEditor** | `InputField` 列表 + 校验错误态 |
| **Experimental** | `Switch` + 二级 `Arrow` |
| **NavBarCustomizer** | 每 Bar 项 `SwitchPreference` |

---

## 7. 后端保留与重构（业务逻辑不重写）

### 7.1 保留
- `core/data/*` 全部仓库（TailscaleRepository、Drop、Log、Traffic、Preferences…）核心逻辑保留，shell 调用统一经 `RootShell`。
- `core/model/*` 模型、JSON 映射（kotlinx-serialization）保留。
- `core/log/*`、`core/manager/*`、所有 service / tile、DataStore 迁移逻辑保留。

### 7.2 根权限抽象
- `RootShell` 保持 libsu 封装层；**新增规范化接口，杜绝裸 `su`**：
  - `exec(cmd): CommandResult`、`stream(cmd): Flow<String>` 等显式化；
  - 明确异常类型（`RootShellException`），替换 generic `runCatching` 吞异常。
- **收口 `FileShareViewModel` 中的 `Runtime.getRuntime().exec(arrayOf("su", "-c", ...))`**：其依赖 stdin 写流。方案：在 `RootShell` 提供 `streamStdin(cmd, InputProvider)`（基于 libsu 构建独立 shell，stdin 可写），无备份文件写 stdin。
- 原则：**代码中不再出现裸 `su` / 字符串拼接的 root 命令**，统一走单个 shell 抽象。

### 7.3 接口 / Hilt 显式化（可选阶段）
- 将主要 Repository 拆出 interface + `@Binds` 绑定；不影响编译则先保留单一实现类，避免过度工程。

---

## 8. 实施顺序（每步可编译验证）

1. **Gradle 配置**：重写版本目录 + `app/build.gradle.kts`（改包名、移除 M3、加 miuix）→ `assembleDebug` 通过。
2. **主题**：`ThemeController` + MiuixTheme 外壳 + `LocalStatusColors`。
3. **RootShell 抽象**：新增异常类型 + `streamStdin`，收口 FileShare 的裸 su。
4. **导航**：`Route` + `Navigator` + `NavDisplay` + 自适应 `NavigationBar`/`NavigationRail`。
5. **共享组件**：StatusCard（新样式）、DeviceCard、Banner、Dialog 封装、Top bar。
6. **逐个屏幕**（低风险 → 重）：Traffic → Netcheck → Drop → Accounts → Logs → PeerDetail → 子页 → Settings（最重）。
7. **服务回归**：验证 Boot、DropProtect、Tile 正常（逻辑未改）。
8. **清理/回归**：`rg` 验证无 `material3`、无 `androidx.navigation`、无 `Runtime.exec("su")`、无默认动画残留。

每步结束执行 `sh gradlew app:assembleDebug --no-daemon --console=plain` 编译；条件允许时安装真机验证 Miuix 主题与导航行为。

---

## 9. 完成定义（Done Criteria）

- 新包 `io.github.tanakalun.tailcontrol` 生效；manifest / 所有 `.kt` 头已更新。
- 不再依赖 `material3`、`NavigationSuite`、`compose-animation` 默认动画。
- 全部导航基于 `NavBackStack` + `NavDisplay` + `NavigationBar`/`NavigationRail`，行为对齐 example。
- 各屏使用 `miuix-ui`/`miuix-preference` 组件：`TopAppBar`、`Scaffold`、`Card`、`Button`、`Switch`、`InputField`、`OverlayDialog` 等，无裸 M3 原语。
- Home `StatusCard` 视觉对齐 RikoNyamu `ConfigScreen.kt` 的 `StatusCard`。
- Root 操作 100% 走 `RootShell`/libsu；源码中 `Runtime.exec("su")`、裸 `su -c` 数量为 0。
- MVVM：ViewModel 不直接触 shell/DataStore，只经 Repository + StateFlow。
- `assembleDebug` 编译通过，旧 Material 主题 / 屏幕 / 文件不再存在。
