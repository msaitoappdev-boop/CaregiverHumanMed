package com.msaitodev.quiz.feature.main.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msaitodev.core.common.ui.LocalAppColors
import com.msaitodev.quiz.feature.main.R
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onNavUp: () -> Unit,
    canShowFullExplanation: Boolean,
    onUpgrade: () -> Unit
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val q = state.questions.getOrNull(state.currentIndex) ?: return

    val progressFraction =
        if (state.total == 0) 0f else (state.currentIndex + 1f) / max(1, state.total).toFloat()

    val appColors = LocalAppColors.current

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // メイン：進捗表示
                            Text(
                                text = stringResource(
                                    id = R.string.quiz_question_format,
                                    state.currentIndex + 1,
                                    state.total
                                ),
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            
                            // サブ：モード名 ｜ カテゴリ名
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                val modeText = when (val mode = state.mode) {
                                    QuizMode.Daily -> stringResource(R.string.quiz_mode_daily)
                                    QuizMode.Review -> stringResource(R.string.quiz_mode_review)
                                    QuizMode.WeaknessAll -> stringResource(R.string.quiz_mode_weakness_all)
                                    is QuizMode.WeaknessCategory -> stringResource(
                                        R.string.quiz_mode_weakness_category,
                                        mode.categoryName
                                    )
                                }
                                
                                Text(
                                    text = modeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                if (state.currentCategoryName.isNotBlank()) {
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = "|",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.outlineVariant
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text(
                                        text = state.currentCategoryName,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onNavUp) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.quiz_back)
                            )
                        }
                    }
                )
                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier.fillMaxWidth().height(4.dp)
                )
            }
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp)
        ) {
            // --- スクロール可能なコンテンツエリア --- 
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
            ) {
                // 問題文
                Text(
                    text = q.text, 
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(top = 16.dp) // 上パディングを内側に移動
                )
                Spacer(Modifier.height(16.dp))

                // 選択肢
                q.options.forEachIndexed { idx, option ->
                    val isSelected = idx == state.selectedIndex
                    val isCorrect = idx == q.correctIndex
                    val bg = when {
                        state.isAnswered && isCorrect -> appColors.correctBackground
                        state.isAnswered && isSelected && !isCorrect -> appColors.wrongBackground
                        isSelected && !state.isAnswered -> appColors.selectedBackground
                        else -> Color.Transparent
                    }
                    val borderColor = when {
                        state.isAnswered && isCorrect -> appColors.correctBorder
                        state.isAnswered && isSelected && !isCorrect -> appColors.wrongBorder
                        else -> Color.Transparent
                    }
                    Surface(
                        tonalElevation = if (isSelected) 1.dp else 0.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .background(bg)
                            .border(
                                width = if (borderColor == Color.Transparent) 0.dp else 1.dp,
                                color = borderColor
                            )
                            .clickable { onSelect(idx) }
                    ) {
                        Text(
                            text = "・$option",
                            modifier = Modifier.padding(14.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }

                // 解説
                if (state.isAnswered) {
                    Spacer(Modifier.height(12.dp))
                    val hasExplanation = q.explanation?.isNotBlank() == true
                    if (hasExplanation) {
                        if (canShowFullExplanation) {
                            Text(
                                text = stringResource(R.string.quiz_explanation_label, q.explanation ?: ""),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.quiz_explanation_premium),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = onUpgrade) {
                                Text(stringResource(R.string.quiz_upgrade_button))
                            }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                } else {
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = stringResource(R.string.quiz_tap_option),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(16.dp)) // 下部の余白
            }

            // --- 下部ナビゲーション（固定） ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrev,
                    enabled = state.currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) { Text(stringResource(R.string.quiz_prev)) }

                Button(
                    onClick = onNext,
                    enabled = state.isAnswered,
                    modifier = Modifier.weight(1f)
                ) {
                    val labelRes = if (state.currentIndex + 1 >= state.total) {
                        R.string.quiz_show_result
                    } else {
                        R.string.quiz_next
                    }
                    Text(stringResource(labelRes))
                }
            }
        }
    }
}
