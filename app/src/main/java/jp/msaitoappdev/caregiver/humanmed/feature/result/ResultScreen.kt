package jp.msaitoappdev.caregiver.humanmed.feature.result

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.msaitoappdev.caregiver.humanmed.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    score: Int,
    total: Int,
    message: String,
    showOfferDialog: Boolean,
    onOfferConfirm: () -> Unit,
    onOfferDismiss: () -> Unit,
    onNextSet: () -> Unit,
    onReviewSameOrder: () -> Unit,
    onReviewList: () -> Unit,
    onScoreHistory: () -> Unit,
    onBackToHome: () -> Unit,
    onNavUp: () -> Unit
) {
    val pct: Int = if (total == 0) 0 else ((score.toFloat() / total) * 100).toInt()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(stringResource(id = R.string.result_title)) },
                navigationIcon = { 
                    IconButton(onClick = onNavUp) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(id = R.string.common_back))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "$score / $total",
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Text(text = "$pct%", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))
            LinearProgressIndicator(
                progress = { (pct / 100f) },
                modifier = Modifier.fillMaxWidth().height(10.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(text = message, style = MaterialTheme.typography.titleMedium)

            Spacer(Modifier.height(32.dp))

            Button(
                onClick = onNextSet,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(id = R.string.result_next_set)) }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onReviewSameOrder,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(id = R.string.result_review_same_order)) }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onReviewList,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(id = R.string.result_review_list)) }

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = onScoreHistory,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(id = R.string.result_score_history)) }

            Spacer(Modifier.height(12.dp))

            TextButton(
                onClick = onBackToHome,
                modifier = Modifier.fillMaxWidth()
            ) { Text(stringResource(id = R.string.result_back_to_home)) }
        }
    }

    if (showOfferDialog) {
        AlertDialog(
            onDismissRequest = onOfferDismiss,
            title = { Text(stringResource(R.string.dialog_rewarded_ad_title)) },
            text = { Text(stringResource(R.string.dialog_rewarded_ad_message)) },
            confirmButton = {
                TextButton(onClick = onOfferConfirm) { Text(stringResource(R.string.dialog_rewarded_ad_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = onOfferDismiss) { Text(stringResource(R.string.dialog_rewarded_ad_dismiss)) }
            }
        )
    }
}
