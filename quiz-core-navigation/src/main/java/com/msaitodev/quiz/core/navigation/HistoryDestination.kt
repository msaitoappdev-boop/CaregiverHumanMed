package com.msaitodev.quiz.core.navigation

import androidx.navigation.NavType
import androidx.navigation.navArgument

/**
 * スコア履歴画面のナビゲーション定義。
 */
object HistoryDestination {
    private const val baseRoute = "history"
    const val argDateKey = "dateKey"

    // dateKey はオプション引数として定義 (history?dateKey={dateKey})
    val route = "$baseRoute?$argDateKey={$argDateKey}"

    val arguments = listOf(
        navArgument(argDateKey) {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }
    )

    /**
     * 遷移用のパスを構築する。
     * @param dateKey フィルタリングする日付 (yyyyMMdd)。null の場合は全期間。
     */
    fun buildRoute(dateKey: String? = null): String {
        return if (dateKey != null) {
            "$baseRoute?$argDateKey=$dateKey"
        } else {
            baseRoute
        }
    }
}
