package com.msaitodev.feature.billing

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.msaitodev.core.common.util.findActivity

@Composable
fun PaywallRoute(
    viewModel: PremiumViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                is PaywallEvent.ShowMessage -> {
                    Toast.makeText(context, context.getString(event.messageResId), Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    PaywallScreen(
        uiState = uiState,
        onPurchaseClick = {
            context.findActivity()?.let { activity ->
                viewModel.onPurchaseClick(activity)
            }
        },
    )
}
