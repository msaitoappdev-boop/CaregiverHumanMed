
package jp.msaitoappdev.caregiver.humanmed.ui.result

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

//@Composable
//fun ResultRoute(navController: NavController, score: Int, total: Int) {
//    ResultScreen(
//        score = score,
//        total = total,
//        onRetry = { navController.navigate("quiz") { popUpTo("home") } },
//        onHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
//    )
//}

@Composable
private fun ResultScreen(score: Int, total: Int, onRetry: () -> Unit, onHome: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = "結果", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Text(text = "$score / $total 正解", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(24.dp))
            Button(onClick = onRetry, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) { Text("もう一度チャレンジ") }
            Spacer(Modifier.height(12.dp))
            OutlinedButton(onClick = onHome, modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)) { Text("ホームへ") }
        }
    }
}
