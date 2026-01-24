package jp.msaitoappdev.caregiver.humanmed.ui.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(
    state: QuizUiState,
    onSelect: (Int) -> Unit,
    onNext: () -> Unit,
    onPrev: () -> Unit,
    onNavUp: () -> Unit,
    // ★ 追加（デフォルト値付きで後方互換）
    canShowFullExplanation: Boolean = true,
    onUpgrade: () -> Unit = {}
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }
    val q = state.current ?: return

    val progressFraction =
        if (state.total == 0) 0f else (state.currentIndex + 1f) / max(1, state.total).toFloat()

    Scaffold(
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = { Text(text = "第 ${state.currentIndex + 1} 問 / 全 ${state.total} 問") },
                    navigationIcon = {
                        IconButton(onClick = onNavUp) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
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

        val CorrectBg = Color(0xFFDFF5E1)
        val CorrectBorder = Color(0xFF2F855A)
        val WrongBg = Color(0xFFFFE0E0)
        val WrongBorder = Color(0xFFC53030)
        val SelectedBg = Color(0xFFE5E5E5)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // 問題文
            Text(text = q.text, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(16.dp))

            // 選択肢（確定後も変更可）
            q.options.forEachIndexed { idx, option ->
                val isSelected = idx == state.selectedIndex
                val isCorrect = idx == q.correctIndex
                val bg = when {
                    state.isAnswered && isCorrect -> CorrectBg
                    state.isAnswered && isSelected && !isCorrect -> WrongBg
                    isSelected && !state.isAnswered -> SelectedBg
                    else -> Color.Transparent
                }
                val borderColor = when {
                    state.isAnswered && isCorrect -> CorrectBorder
                    state.isAnswered && isSelected && !isCorrect -> WrongBorder
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
                        .clickable { onSelect(idx) } // 選択変更を許可
                ) {
                    Text(
                        text = "・$option",
                        modifier = Modifier.padding(14.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            // 解説（回答後）
            if (state.isAnswered) {
                Spacer(Modifier.height(12.dp))
                val hasExplanation = q.explanation?.isNotBlank() == true
                if (hasExplanation) {
                    if (canShowFullExplanation) {
                        Text(
                            text = "解説：${q.explanation}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2F855A)
                        )
                    } else {
                        Text(
                            text = "解説の全文はプレミアムで解放されます。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF2F855A)
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = onUpgrade) {
                            Text("月額 ¥200 で解説を解放")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            } else {
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "選択肢をタップしてください",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.weight(1f))

            // 下部ナビゲーション
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onPrev,
                    enabled = state.currentIndex > 0,
                    modifier = Modifier.weight(1f)
                ) { Text("前の問題へ") }

                Button(
                    onClick = onNext,
                    enabled = state.isAnswered,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (state.currentIndex + 1 >= state.total) "結果を見る" else "次へ")
                }
            }
        }
    }
}
