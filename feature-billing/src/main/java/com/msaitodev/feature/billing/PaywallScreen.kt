package com.msaitodev.feature.billing

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

private const val BENEFIT_LINE_BREAK = "\n"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    uiState: PaywallUiState,
    onPurchaseClick: () -> Unit
) {
    val config = uiState.config ?: return

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(config.title) }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(inner)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = config.headline,
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = config.benefits.joinToString(BENEFIT_LINE_BREAK),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(config.planTitle, style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text(config.planPrice, style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onPurchaseClick,
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isPremium
            ) {
                if (uiState.isPremium) {
                    Text(config.purchasedButtonText)
                } else {
                    Text(config.purchaseButtonText)
                }
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = config.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
