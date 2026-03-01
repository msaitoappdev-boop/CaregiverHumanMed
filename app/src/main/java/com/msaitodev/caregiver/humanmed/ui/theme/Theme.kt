package com.msaitodev.caregiver.humanmed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import com.msaitodev.quiz.core.common.ui.LocalQuizColors
import com.msaitodev.quiz.core.common.ui.QuizColors

private val LightColorScheme = lightColorScheme(
    primary = GreenPrimary,
    secondary = GreenSecondary,
    tertiary = GreenTertiary,
    background = LightGreenBackground,
    surface = LightGreenBackground,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color(0xFF1B5E20),
    onSurface = Color(0xFF1B5E20),
)

private val DarkColorScheme = darkColorScheme(
    primary = GreenPrimaryDark,
    secondary = GreenSecondaryDark,
    tertiary = GreenTertiaryDark,
    background = Color(0xFF121212),
    surface = Color(0xFF121212),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

// クイズ用のセマンティックカラー定義（ライトモード用）
private val QuizLightColors = QuizColors(
    correctBorder = Color(0xFF2F855A),
    correctBackground = Color(0xFFDFF5E1),
    wrongBorder = Color(0xFFC53030),
    wrongBackground = Color(0xFFFFE0E0),
    selectedBackground = Color(0xFFE5E5E5)
)

// クイズ用のセマンティックカラー定義（ダークモード用：視認性を調整）
private val QuizDarkColors = QuizColors(
    correctBorder = Color(0xFF48BB78),
    correctBackground = Color(0xFF1C4532),
    wrongBorder = Color(0xFFF56565),
    wrongBackground = Color(0xFF652B19),
    selectedBackground = Color(0xFF4A5568)
)

@Composable
fun CaregiverTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val quizColors = if (darkTheme) QuizDarkColors else QuizLightColors

    CompositionLocalProvider(
        LocalQuizColors provides quizColors
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            content = content
        )
    }
}
