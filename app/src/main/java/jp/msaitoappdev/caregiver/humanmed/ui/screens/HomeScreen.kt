
package jp.msaitoappdev.caregiver.humanmed.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun HomeScreen(onStart: () -> Unit, onCategory: () -> Unit, onWeak: () -> Unit, onSettings: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("今日のおすすめ 10問", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onStart, modifier = Modifier.fillMaxWidth()) { Text("すぐに始める") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onCategory, modifier = Modifier.fillMaxWidth()) { Text("カテゴリから選ぶ") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(onClick = onWeak, modifier = Modifier.fillMaxWidth()) { Text("弱点分析") }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onSettings) { Text("設定") }
    }
}
