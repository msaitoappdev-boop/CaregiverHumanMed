package com.msaitodev.quiz.feature.analysis

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.msaitodev.quiz.core.domain.model.LearningAnalysis
import com.msaitodev.quiz.core.domain.model.TrendPeriod
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * 学習状況の分析結果を表示する画面。
 * UDF (Unidirectional Data Flow) パターンに従い、ステートを購読しイベントを通知する。
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AnalysisScreen(
    uiState: AnalysisUiState,
    onBack: () -> Unit,
    onPeriodChange: (TrendPeriod) -> Unit,
    onCategoryClick: (String) -> Unit,
    onDateClick: (String) -> Unit
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
            AnalysisContent(
                modifier = Modifier.padding(padding),
                analysis = uiState.analysis,
                currentPeriod = uiState.currentPeriod,
                onPeriodChange = onPeriodChange,
                onCategoryClick = onCategoryClick,
                onDateClick = onDateClick
            )
        }
    }
}

@Composable
private fun AnalysisContent(
    modifier: Modifier = Modifier,
    analysis: LearningAnalysis,
    currentPeriod: TrendPeriod,
    onPeriodChange: (TrendPeriod) -> Unit,
    onCategoryClick: (String) -> Unit,
    onDateClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // 合格可能性パネル (推定スコア)
        ScorePredictionPanel(analysis)

        // AIアドバイス（総評）
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

        // 総合進捗
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

        // 学習継続カレンダー
        AnalysisSection(title = stringResource(R.string.analysis_section_heatmap), icon = Icons.Default.Timeline) {
            Column {
                if (analysis.currentStreak > 0) {
                    Text(
                        text = stringResource(R.string.analysis_streak_format, analysis.currentStreak),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                StudyCalendar(
                    studiedDays = analysis.studiedDays,
                    onDateClick = onDateClick,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
                )
            }
        }

        // 分野別バランス（レーダーチャート）
        AnalysisSection(title = stringResource(R.string.analysis_section_balance), icon = Icons.Default.BarChart) {
            RadarChart(
                summaries = analysis.categorySummaries,
                modifier = Modifier
                    .padding(vertical = 16.dp)
                    .fillMaxWidth()
            )
        }

        // 分野別詳細リスト
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

        // 正解率の推移（トレンドグラフ）
        AnalysisSection(
            title = stringResource(R.string.analysis_section_trend), 
            icon = Icons.Default.Timeline
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                PeriodSelector(
                    selectedPeriod = currentPeriod,
                    onPeriodChange = onPeriodChange
                )
                if (analysis.dailyTrend.isNotEmpty()) {
                    DailyTrendChart(
                        trends = analysis.dailyTrend,
                        onDateClick = onDateClick
                    )
                }
            }
        }
        
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun ScorePredictionPanel(analysis: LearningAnalysis) {
    val scoreFormat = stringResource(R.string.analysis_predicted_score_format)
    val totalFormat = stringResource(R.string.analysis_total_exam_questions_format)
    val primaryColor = MaterialTheme.colorScheme.primary
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.analysis_predicted_score_title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = String.format(scoreFormat, analysis.predictedScore),
                    style = MaterialTheme.typography.displayLarge,
                    fontWeight = FontWeight.Black,
                    color = primaryColor
                )
                Text(
                    text = String.format(totalFormat, analysis.totalExamQuestions),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 12.dp, start = 4.dp)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            // 合格目標ゲージ
            Box(modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(analysis.predictedAccuracyRate)
                        .fillMaxHeight()
                        .background(primaryColor)
                )
            }
            
            Spacer(Modifier.height(8.dp))
            
            Text(
                text = stringResource(R.string.analysis_predicted_score_caption),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
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
                Text(label, fontSize = 12.sp)
            }
        }
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
private fun StudyCalendar(
    studiedDays: List<Long>,
    onDateClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val onPrimaryColor = MaterialTheme.colorScheme.onPrimary
    val surfaceVariantColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val todayColor = MaterialTheme.colorScheme.primary
    val dateKeySdf = remember { SimpleDateFormat("yyyyMMdd", Locale.getDefault()) }
    
    val dayLabels = listOf(
        stringResource(R.string.analysis_day_sun),
        stringResource(R.string.analysis_day_mon),
        stringResource(R.string.analysis_day_tue),
        stringResource(R.string.analysis_day_wed),
        stringResource(R.string.analysis_day_thu),
        stringResource(R.string.analysis_day_fri),
        stringResource(R.string.analysis_day_sat)
    )

    val today = remember {
        Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
    }
    val todayMillis = today.timeInMillis

    val calendarDays = remember(studiedDays) {
        val days = mutableListOf<CalendarDay>()
        val cal = today.clone() as Calendar
        
        cal.add(Calendar.WEEK_OF_YEAR, -4)
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        
        val studiedSet = studiedDays.toSet()
        
        repeat(35) {
            days.add(
                CalendarDay(
                    millis = cal.timeInMillis,
                    dayOfMonth = cal.get(Calendar.DAY_OF_MONTH),
                    isStudied = studiedSet.contains(cal.timeInMillis),
                    isToday = cal.timeInMillis == todayMillis,
                    isCurrentMonth = cal.get(Calendar.MONTH) == today.get(Calendar.MONTH)
                )
            )
            cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        days
    }

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                dayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = labelColor.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(Modifier.height(8.dp))
            
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                for (week in 0 until 5) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        for (dayOfWeek in 0 until 7) {
                            val day = calendarDays[week * 7 + dayOfWeek]
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .aspectRatio(1f)
                                    .padding(2.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(
                                        if (day.isStudied) primaryColor else Color.Transparent
                                    )
                                    .clickable(enabled = day.isStudied) {
                                        onDateClick(dateKeySdf.format(Date(day.millis)))
                                    }
                                    .then(
                                        if (day.isToday && !day.isStudied) {
                                            Modifier.background(surfaceVariantColor, RoundedCornerShape(8.dp))
                                        } else Modifier
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (day.isToday) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .border(
                                                width = 2.dp,
                                                color = if (day.isStudied) onPrimaryColor.copy(alpha = 0.5f) else todayColor,
                                                shape = RoundedCornerShape(8.dp)
                                            )
                                    )
                                }
                                
                                Text(
                                    text = day.dayOfMonth.toString(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (day.isToday) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        day.isStudied -> onPrimaryColor
                                        !day.isCurrentMonth -> labelColor.copy(alpha = 0.3f)
                                        else -> labelColor
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class CalendarDay(
    val millis: Long,
    val dayOfMonth: Int,
    val isStudied: Boolean,
    val isToday: Boolean,
    val isCurrentMonth: Boolean
)

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
    val ellipsisFormat = stringResource(R.string.analysis_ellipsis_format)
    
    val density = androidx.compose.ui.platform.LocalDensity.current
    val labelFontSize = with(density) { 10.sp.toPx() }

    Box(modifier = modifier.aspectRatio(1.2f), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val maxRadius = size.minDimension / 2 * 0.75f
            val angleStep = (2 * PI / count).toFloat()

            for (i in 1..5) {
                val r = maxRadius * (i / 5f)
                val path = Path()
                for (j in 0 until count) {
                    val angle = (j * angleStep - PI / 2).toFloat()
                    val x = center.x + r * cos(angle.toDouble()).toFloat()
                    val y = center.y + r * sin(angle.toDouble()).toFloat()
                    if (j == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, outlineColor, style = Stroke(width = 1.dp.toPx()))
            }

            for (j in 0 until count) {
                val angle = (j * angleStep - PI / 2).toFloat()
                val x = center.x + maxRadius * cos(angle.toDouble()).toFloat()
                val y = center.y + maxRadius * sin(angle.toDouble()).toFloat()
                drawLine(outlineColor, center, Offset(x, y), strokeWidth = 1.dp.toPx())
            }

            val dataPath = Path()
            for (j in 0 until count) {
                val angle = (j * angleStep - PI / 2).toFloat()
                val r = maxRadius * summaries[j].accuracyRate.coerceIn(0f, 1f)
                val x = center.x + r * cos(angle.toDouble()).toFloat()
                val y = center.y + r * sin(angle.toDouble()).toFloat()
                if (j == 0) dataPath.moveTo(x, y) else dataPath.lineTo(x, y)
            }
            dataPath.close()
            drawPath(dataPath, primaryColor.copy(alpha = 0.3f))
            drawPath(dataPath, primaryColor, style = Stroke(width = 2.dp.toPx()))

            for (j in 0 until count) {
                val angle = (j * angleStep - PI / 2).toFloat()
                val labelRadius = maxRadius + 20.dp.toPx()
                val x = center.x + labelRadius * cos(angle.toDouble()).toFloat()
                val y = center.y + labelRadius * sin(angle.toDouble()).toFloat()
                
                val categoryName = summaries[j].categoryName
                drawContext.canvas.nativeCanvas.apply {
                    val paint = android.graphics.Paint().apply {
                        textSize = labelFontSize
                        textAlign = when {
                            cos(angle.toDouble()) > 0.1 -> android.graphics.Paint.Align.LEFT
                            cos(angle.toDouble()) < -0.1 -> android.graphics.Paint.Align.RIGHT
                            else -> android.graphics.Paint.Align.CENTER
                        }
                        isAntiAlias = true
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                    }
                    
                    paint.color = android.graphics.Color.argb(
                        (labelColor.alpha * 255).toInt(),
                        (labelColor.red * 255).toInt(),
                        (labelColor.green * 255).toInt(),
                        (labelColor.blue * 255).toInt()
                    )
                    
                    val displayText = if (categoryName.length > 6) {
                        String.format(ellipsisFormat, categoryName.take(5))
                    } else {
                        categoryName
                    }
                    drawText(displayText, x, y + labelFontSize / 2, paint)
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
    val percentageFormat = stringResource(R.string.analysis_percentage_format)
    val progressColor = if (summary.accuracyRate > 0.7f) {
        Color(0xFF4CAF50) // Success color
    } else {
        MaterialTheme.colorScheme.primary
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp)
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(summary.categoryName, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = String.format(percentageFormat, (summary.accuracyRate * 100).toInt()),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { summary.accuracyRate },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = progressColor
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
private fun DailyTrendChart(
    trends: List<LearningAnalysis.DailyScore>,
    onDateClick: (String) -> Unit
) {
    val percentageFormat = stringResource(R.string.analysis_percentage_format)
    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
    val yAxisWidth = 32.dp

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth().height(160.dp)) {
                // Y軸のラベル (100% ~ 0%)
                Column(
                    modifier = Modifier.width(yAxisWidth).fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf(100, 75, 50, 25, 0).forEach { pct ->
                        Text(
                            text = String.format(percentageFormat, pct),
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            color = labelColor
                        )
                    }
                }

                Spacer(Modifier.width(8.dp))

                // チャートエリア
                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    // グリッド線
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val lineCount = 5
                        for (i in 0 until lineCount) {
                            val y = i * (size.height / (lineCount - 1))
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(size.width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                    }

                    // データ棒
                    Row(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        trends.forEach { score ->
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(20.dp)
                                    .clickable { onDateClick(score.dateKey) },
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(score.averageAccuracy.coerceAtLeast(0.01f))
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(
                                            if (score.averageAccuracy > 0.7f) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondary
                                        )
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // X軸のラベル (日付)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = yAxisWidth + 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                trends.forEach { score ->
                    Text(
                        text = score.dateLabel,
                        modifier = Modifier
                            .width(20.dp)
                            .clickable { onDateClick(score.dateKey) },
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 9.sp,
                        color = labelColor,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
