
package jp.msaitoappdev.caregiver.humanmed.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

private val categories = listOf("循環器", "呼吸器", "消化器", "感染予防", "服薬", "観察・バイタル")

@Composable
fun CategoryScreen(onStartQuiz: (String) -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("カテゴリを選択", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(12.dp))
        categories.forEach { cat ->
            ElevatedCard(Modifier.fillMaxWidth().padding(vertical = 6.dp).clickable { onStartQuiz(cat) }) {
                Text(cat, modifier = Modifier.padding(16.dp))
            }
        }
    }
}
