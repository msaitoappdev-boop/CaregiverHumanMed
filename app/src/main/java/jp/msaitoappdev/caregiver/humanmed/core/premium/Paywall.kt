package jp.msaitoappdev.caregiver.humanmed.feature.premium

import android.app.Activity
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

@Composable
fun PremiumGate(
    isPremium: Boolean,
    premiumContent: @Composable () -> Unit,
    onPurchaseClick: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    if (isPremium) {
        premiumContent()
    } else {
        Paywall(onPurchaseClick, modifier)
    }
}

@Composable
fun Paywall(
    onPurchaseClick: (Activity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Column(modifier.padding(24.dp)) {
        Text("弱点を最短で克服", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("・詳細解説（要因・対比・図表）\n・追加練習（+10問/日）\n・誤答ノート保存",
            style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(16.dp))
        val activity = context.findActivity()
        Button(
            onClick = { activity?.let(onPurchaseClick) },
            enabled = activity != null
        ) {
            Text("月額 ¥200 で今すぐ解放")
        }
        if (activity == null) {
            Spacer(Modifier.height(8.dp))
            Text("購入は画面アクティビティから実行してください", style = MaterialTheme.typography.bodySmall)
        }
    }
}

private fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    else -> when (val base = this as? android.content.ContextWrapper) {
        null -> null
        else -> base.baseContext.findActivity()
    }
}
