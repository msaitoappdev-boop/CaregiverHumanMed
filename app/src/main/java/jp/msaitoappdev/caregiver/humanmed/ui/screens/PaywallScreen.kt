package jp.msaitoappdev.caregiver.humanmed.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaywallScreen(
    onUpgradeClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(text = "プレミアムへアップグレード", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(12.dp))
        Text(text = "・AIのやさしい説明\n・詳細解説\n・弱点分析")
        Spacer(modifier = Modifier.height(12.dp))
//        Button(
//            onClick = { /* TODO: launch billing flow */ },
//            modifier = Modifier.fillMaxWidth()
//        ) {
//            Text(text = "7日間無料で試す")
//        }
        Button(
            onClick = onUpgradeClicked,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "月額 ¥200 でアップグレード（トライアルなし）")
        }
    }
}
