package com.msaitodev.core.common.ui

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * アプリケーション画面等で使用する、セマンティックな（意味を持った）色定義。
 * アプリのブランド（Hub）ごとに異なる色を適用できるように CompositionLocal で管理します。
 */
data class AppColors(
    val correctBorder: Color = Color.Unspecified,
    val correctBackground: Color = Color.Unspecified,
    val wrongBorder: Color = Color.Unspecified,
    val wrongBackground: Color = Color.Unspecified,
    val selectedBackground: Color = Color.Unspecified // 削除してしまったプロパティを復元
)

/**
 * [AppColors] を提供するための CompositionLocal。
 */
val LocalAppColors = staticCompositionLocalOf { AppColors() }
