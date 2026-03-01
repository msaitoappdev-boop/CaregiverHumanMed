package com.msaitodev.quiz.core.common.ui

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * クイズ画面等で使用する、セマンティックな（意味を持った）色定義。
 * アプリのブランド（Hub）ごとに異なる色を適用できるように CompositionLocal で管理する。
 */
@Immutable
data class QuizColors(
    val correctBorder: Color = Color.Unspecified,
    val correctBackground: Color = Color.Unspecified,
    val wrongBorder: Color = Color.Unspecified,
    val wrongBackground: Color = Color.Unspecified,
    val selectedBackground: Color = Color.Unspecified
)

val LocalQuizColors = staticCompositionLocalOf { QuizColors() }
