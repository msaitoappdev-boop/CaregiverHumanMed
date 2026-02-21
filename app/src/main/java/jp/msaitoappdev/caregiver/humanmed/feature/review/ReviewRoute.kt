package jp.msaitoappdev.caregiver.humanmed.feature.review

import android.util.Log
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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import jp.msaitoappdev.caregiver.humanmed.R
import jp.msaitoappdev.caregiver.humanmed.core.navigation.NavRoutes
import jp.msaitoappdev.caregiver.humanmed.feature.premium.PremiumViewModel
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.QuizViewModel
import jp.msaitoappdev.caregiver.humanmed.feature.quiz.ReviewItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewRoute(navController: NavController) {
    val TAG = "ReviewRoute"
    val currentEntry by navController.currentBackStackEntryAsState()
    val quizEntry = remember(currentEntry) {
        runCatching { navController.getBackStackEntry(NavRoutes.QUIZ) }.getOrNull()
    }
    if (quizEntry == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }
    val vm: QuizViewModel = hiltViewModel(quizEntry)
    val premiumVm: PremiumViewModel = hiltViewModel()
    val premiumState by premiumVm.uiState.collectAsStateWithLifecycle()

    val state by vm.uiState.collectAsState()

    val items: List<ReviewItem> = remember(state, premiumState.isPremium) { vm.getReviewItems(premiumState.isPremium) }

    val CorrectBg = Color(0xFFDFF5E1)
    val CorrectBorder = Color(0xFF2F855A)
    val WrongBg = Color(0xFFFFE0E0)
    val WrongBorder = Color(0xFFC53030)

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(R.string.review_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.common_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(items, key = { it.number }) { item ->
                ReviewCard(
                    item = item,
                    correctBg = CorrectBg,
                    correctBorder = CorrectBorder,
                    wrongBg = WrongBg,
                    wrongBorder = WrongBorder
                )
            }
        }
    }
}

@Composable
private fun ReviewCard(
    item: ReviewItem,
    correctBg: Color,
    correctBorder: Color,
    wrongBg: Color,
    wrongBorder: Color
) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(text = "第 ${item.number} 問", style = MaterialTheme.typography.titleSmall, color = Color.Gray)
            Spacer(Modifier.height(4.dp))
            Text(text = item.question, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            item.options.forEachIndexed { idx, option ->
                val isSelected = idx == item.selectedIndex
                val isCorrect = idx == item.correctIndex
                val bg = when {
                    isCorrect -> correctBg
                    isSelected && !isCorrect -> wrongBg
                    else -> Color.Transparent
                }
                val borderColor = when {
                    isCorrect -> correctBorder
                    isSelected && !isCorrect -> wrongBorder
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
            Text(text = "正解：$correct", style = MaterialTheme.typography.labelLarge, color = correctBorder)

            item.explanation?.let { exp ->
                if (exp.isNotBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(text = "解説：$exp", style = MaterialTheme.typography.bodyMedium, color = correctBorder)
                }
            }
        }
    }
}
