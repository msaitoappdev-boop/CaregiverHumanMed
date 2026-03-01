package com.msaitodev.caregiver.humanmed

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.msaitodev.caregiver.humanmed.ui.AppNavHost
import com.msaitodev.caregiver.humanmed.ui.theme.CaregiverTheme
import com.msaitodev.quiz.core.ads.InterstitialHelper
import com.msaitodev.quiz.core.ads.RewardedHelper
import com.msaitodev.quiz.core.domain.repository.PremiumRepository
import com.msaitodev.quiz.core.domain.repository.RemoteConfigRepository
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var premiumRepo: PremiumRepository

    @Inject
    lateinit var interstitialHelper: InterstitialHelper

    @Inject
    lateinit var rewardedHelper: RewardedHelper

    @Inject
    lateinit var remoteConfigRepo: RemoteConfigRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Android 15 (Target SDK 35) で必須となる Edge-to-Edge を有効化
        enableEdgeToEdge()

        Log.i("MainActivity", "onCreate called")

        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onResume(owner: LifecycleOwner) {
                lifecycleScope.launch {
                    premiumRepo.refreshFromBilling()
                }
            }
        })

        setContent {
            CaregiverTheme {
                AppNavHost(interstitialHelper, rewardedHelper)
            }
        }
    }
}
