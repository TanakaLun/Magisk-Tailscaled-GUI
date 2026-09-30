package io.github.tanakalun.tailcontrol.ui.nav

import top.yukonga.miuix.kmp.nav.core.NavBackStack
import top.yukonga.miuix.kmp.nav.core.NavKey

/**
 * 基于 miuix-nav [NavBackStack] 的导航助手，与官方 example 的 `Navigator` 行为保持一致。
 * 支持 push / replace / pop / popUntil / current。
 */
class Navigator(
    val backStack: NavBackStack,
) {
    /**
     * 将 key 压入 back stack。重复 push 相同 route 会被运行时拒绝（相同 contentKey），
     * 因此先检查栈内是否已存在；需要多实例的路由请携带唯一值（如 PeerDetail(name)）。
     */
    fun push(key: NavKey) {
        if (key !in backStack) {
            backStack.add(key)
        }
    }

    /** 替换栈顶 key；栈为空时直接 push。 */
    fun replace(key: NavKey) {
        if (backStack.isNotEmpty()) {
            backStack[backStack.lastIndex] = key
        } else {
            backStack.add(key)
        }
    }

    /** 弹出栈顶 key（至少保留根）。 */
    fun pop() {
        if (backStack.size > 1) {
            backStack.removeLastOrNull()
        }
    }

    /** 从栈顶弹出直到栈顶满足 predicate。 */
    fun popUntil(predicate: (NavKey) -> Boolean) {
        while (backStack.size > 1 && !predicate(backStack.last())) {
            backStack.removeAt(backStack.lastIndex)
        }
    }

    fun current(): NavKey? = backStack.lastOrNull()

    fun backStackSize(): Int = backStack.size
}