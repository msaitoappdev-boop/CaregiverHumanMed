package com.msaitodev.quiz.feature.analysis

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msaitodev.quiz.core.domain.model.LearningAnalysis
import com.msaitodev.quiz.core.domain.model.TrendPeriod
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalysisScreen(
    uiState: AnalysisUiState,
    onBack: () -> Unit,
    onPeriodChange: (TrendPeriod) -> Unit,
    onCategoryClick: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.analysis_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.analysis_back)
                        )
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
                PeriodSelector(
                    selectedPeriod = uiState.currentPeriod,
                    onPeriodChange = onPeriodChange,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                AnalysisContent(
                    modifier = Modifier.weight(1f),
                    analysis = uiState.analysis,
                    onCategoryClick = onCategoryClick
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
                TrendPeriod.DAILY -> stringResource(R.string.analysis_period_day)
                TrendPeriod.WEEKLY -> stringResource(R.string.analysis_period_week)
                TrendPeriod.MONTHLY -> stringResource(R.string.analysis_period_month)
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
    analysis: LearningAnalysis,
    onCategoryClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // アドバイス欄の背景色を設定画面のカードと統一 (surfaceVariant)
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.Info, 
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = analysis.overallComment,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        AnalysisSection(title = stringResource(R.string.analysis_section_overall), icon = Icons.Default.Timeline) {
            Column {
                LinearProgressIndicator(
                    progress = { analysis.totalProgress },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.analysis_total_progress_format, (analysis.totalProgress * 100).toInt()),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }

        AnalysisSection(title = stringResource(R.string.analysis_section_balance), icon = Icons.Default.BarChart) {
            RadarChart(
                summaries = analysis.categorySummaries,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )
        }

        AnalysisSection(title = stringResource(R.string.analysis_section_details), icon = Icons.Default.BarChart) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                analysis.categorySummaries.forEach { summary ->
                    CategoryItem(
                        summary = summary,
                        onClick = { onCategoryClick(summary.categoryId) }
                    )
                }
            }
        }

        if (analysis.dailyTrend.isNotEmpty()) {
            AnalysisSection(title = stringResource(R.string.analysis_section_trend), icon = Icons.Default.Timeline) {
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
private fun RadarChart(
    summaries: List<LearningAnalysis.CategorySummary>,
    modifier: Modifier = Modifier
) {
    val count = summaries.size
    if (count < 3) return

    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outlineVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    
    val density = LocalDensity.current
    val labelFontSize = with(density) { 10.sp.toPx() }

    Box(modifier = modifier.aspectRatio(1.2f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2 * 0.75f
            val angleStep = (2 * PI / count).toFloat()

            // 1. ガイドライン（同心円状の多角形）の描画
            for (i in 1..5) {
                val r = maxRadius * (i / 5f)
                val path = Path()
                for (j in 0 until count) {
                    val angle = j * angleStep - PI.toFloat() / 2
                    val x = center.x + r * cos(angle)
                    val y = center.y + r * sin(angle)
                    if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, outlineColor, style = Stroke(width = 1.dp.toPx()))
            }

            // 2. 軸線の描画
            for (j in 0 until count) {
                val angle = j * angleStep - PI.toFloat() / 2
                val x = center.x + maxRadius * cos(angle)
                val y = center.y + maxRadius * sin(angle)
                drawLine(outlineColor, center, Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            // 3. データの描画
            val dataPath = Path()
            for (j in 0 until count) {
                val angle = j * angleStep - PI.toFloat() / 2
                val r = maxRadius * summaries[j].accuracyRate.coerceIn(0f, 1f)
                val x = center.x + r * cos(angle)
                val y = center.y + r * sin(angle)
                if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()
            drawPath(dataPath, primaryColor.copy(alpha = 0.3f))
            drawPath(dataPath, primaryColor, style = Stroke(width = 2.dp.toPx()))

            // 4. ラベルの描画
            for (j in 0 until count) {
                val angle = j * angleStep - PI.toFloat() / 2
                val labelRadius = maxRadius + 20.dp.toPx()
                val x = center.x + labelRadius * cos(angle)
                val y = center.y + labelRadius * sin(angle)
                
                val categoryName = summaries[j].categoryName
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = labelFontSize
                        textAlign = when {
                            cos(angle) > 0.1 -> android.graphics.Paint.Align.LEFT
                            cos(angle) < -0.1 -> android.graphics.Paint.Align.RIGHT
                            else -> android.graphics.Paint.Align.CENTER
                        }
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    
                    // アプリ共通の色を使用 (Color -> ARGB)
                    paint.color = android.graphics.Color.argb(
                        (labelColor.alpha * 255).toInt(),
                        (labelColor.red * 255).toInt(),
                        (labelColor.green * 255).toInt(),
                        (labelColor.blue * 255).toInt()
                    )
                    
                    drawText(
                        if (categoryName.length > 6) categoryName.take(5) + ".." else categoryName,
                        x,
                        y + labelFontSize / 2,
                        paint
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryItem(
    summary: LearningAnalysis.CategorySummary,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
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
        Text(
            text = stringResource(R.string.analysis_train_category_action),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.align(Alignment.End).padding(top = 4.dp)
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
