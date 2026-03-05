package com.msaitodev.quiz.feature.analysis

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msaitodev.quiz.core.domain.model.LearningAnalysis
import com.msaitodev.quiz.core.domain.model.TrendPeriod

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalysisScreen(
    uiState: AnalysisUiState,
    onBack: () -> Unit,
    onPeriodChange: (TrendPeriod) -> Unit // 追加
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("学習分析") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "戻る")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.analysis != null) {
            Column(modifier = Modifier.padding(padding)) {
                // 期間切り替えセレクターを上部に配置
                PeriodSelector(
                    selectedPeriod = uiState.currentPeriod,
                    onPeriodChange = onPeriodChange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                AnalysisContent(
                    modifier = Modifier.weight(1f),
                    analysis = uiState.analysis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PeriodSelector(
    selectedPeriod: TrendPeriod,
    onPeriodChange: (TrendPeriod) -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.fillMaxWidth()
    ) {
        TrendPeriod.entries.forEachIndexed { index, period ->
            val label = when (period) {
                TrendPeriod.DAILY -> "日"
                TrendPeriod.WEEKLY -> "週"
                TrendPeriod.MONTHLY -> "月"
            }
            SegmentedButton(
                shape = SegmentedButtonDefaults.itemShape(index = index, count = TrendPeriod.entries.size),
                onClick = { onPeriodChange(period) },
                selected = period == selectedPeriod
            ) {
                Text(label)
            }
        }
    }
}

@Composable
private fun AnalysisContent(
    modifier: Modifier = Modifier,
    analysis: LearningAnalysis
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 1. 総評カード
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Info, contentDescription = null)
                Spacer(Modifier.width(12.dp))
                Text(
                    text = analysis.overallComment,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // 2. 総合進捗
        AnalysisSection(title = "総合進捗", icon = Icons.Default.Timeline) {
            Column {
                LinearProgressIndicator(
                    progress = { analysis.totalProgress },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "全問題の ${(analysis.totalProgress * 100).toInt()}% を学習済み",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        // 3. 分野別正解率
        AnalysisSection(title = "分野別正解率", icon = Icons.Default.BarChart) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                analysis.categorySummaries.forEach { summary ->
                    CategoryItem(summary)
                }
            }
        }

        // 4. 学習トレンド
        if (analysis.dailyTrend.isNotEmpty()) {
            AnalysisSection(title = "正解率の推移", icon = Icons.Default.Timeline) {
                DailyTrendChart(analysis.dailyTrend)
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun AnalysisSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
        content()
    }
}

@Composable
private fun CategoryItem(summary: LearningAnalysis.CategorySummary) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(summary.categoryName, style = MaterialTheme.typography.bodyMedium)
            Text("${(summary.accuracyRate * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { summary.accuracyRate },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = if (summary.accuracyRate > 0.7f) Color(0xFF4CAF50) else MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun DailyTrendChart(trends: List<LearningAnalysis.DailyScore>) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .padding(top = 20.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        trends.forEach { score ->
            val percentage = (score.averageAccuracy * 100).toInt()
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.fillMaxHeight()
            ) {
                Text(
                    text = "$percentage%",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .width(24.dp)
                        .fillMaxHeight(score.averageAccuracy.coerceAtLeast(0.05f))
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (score.averageAccuracy > 0.7f) MaterialTheme.colorScheme.primary 
                            else MaterialTheme.colorScheme.secondary
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = score.dateLabel,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
