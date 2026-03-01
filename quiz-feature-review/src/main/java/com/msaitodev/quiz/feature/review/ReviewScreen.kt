package com.msaitodev.quiz.feature.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.msaitodev.quiz.core.common.ui.LocalQuizColors
import com.msaitodev.quiz.core.common.ui.QuizColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    uiState: ReviewUiState,
    onNavUp: () -> Unit
) {
    val quizColors = LocalQuizColors.current

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = {
                    IconButton(onClick = onNavUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            // TODO: Add loading indicator
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.items, key = { it.number }) { item ->
                    ReviewCard(
                        item = item,
                        quizColors = quizColors
                    )
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(
    item: ReviewItem,
    quizColors: QuizColors
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                text = "第 ${item.number} 問",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))
            Text(text = item.question, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            item.options.forEachIndexed { idx, option ->
                val isSelected = idx == item.selectedIndex
                val isCorrect = idx == item.correctIndex
                val bg = when {
                    isCorrect -> quizColors.correctBackground
                    isSelected && !isCorrect -> quizColors.wrongBackground
                    else -> Color.Transparent
                }
                val borderColor = when {
                    isCorrect -> quizColors.correctBorder
                    isSelected && !isCorrect -> quizColors.wrongBorder
                    else -> Color.Transparent
                }
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .background(bg)
                        .border(
                            width = if (borderColor == Color.Transparent) 0.dp else 1.dp,
                            color = borderColor
                        ),
                    tonalElevation = if (isSelected || isCorrect) 1.dp else 0.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "・$option", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))
            val my = item.selectedIndex?.let { idx -> item.options.getOrNull(idx) } ?: "未回答"
            val correct = item.options.getOrNull(item.correctIndex) ?: "-"
            Text(text = "あなたの回答：$my", style = MaterialTheme.typography.labelLarge)
            Text(
                text = "正解：$correct",
                style = MaterialTheme.typography.labelLarge,
                color = if (quizColors.correctBorder != Color.Unspecified) quizColors.correctBorder else MaterialTheme.colorScheme.primary
            )

            item.explanation?.let { exp ->
                if (exp.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "解説：$exp",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (quizColors.correctBorder != Color.Unspecified) quizColors.correctBorder else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
