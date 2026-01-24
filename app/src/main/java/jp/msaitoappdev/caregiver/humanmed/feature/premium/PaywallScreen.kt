package jp.msaitoappdev.caregiver.humanmed.feature.premium

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.material3.ExperimentalMaterial3Api

/**
 * Paywall の“本体”を feature/premium 側に実装。
 * ここから直接 UI を描画するため、旧 ui.screens.PaywallScreen への委譲は廃止します。
 *
 * 挙動は従来通り：
 *  - 「月額 ¥200 でアップグレード（トライアルなし）」押下で onUpgradeClicked() を呼ぶ
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaywallScreen(
    onUpgradeClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("プレミアム") }
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
                text = "毎日の学習を、もっと快適に。",
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))
            Text(
                text = "・解説の全文が閲覧可能\n・“今日の3問”の学習がスムーズに\n・将来の拡張機能にも対応",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(24.dp))

            // 料金表示（固定文言）
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("プラン", style = MaterialTheme.typography.labelLarge)
                    Spacer(Modifier.height(4.dp))
                    Text("月額 ¥200（トライアルなし）", style = MaterialTheme.typography.titleMedium)
                }
            }

            Spacer(Modifier.height(24.dp))

            Button(
                onClick = onUpgradeClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("月額 ¥200 でアップグレード（トライアルなし）")
            }

            Spacer(Modifier.height(12.dp))
            Text(
                text = "Google Play の購入ダイアログが表示されます",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
