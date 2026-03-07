package com.msaitodev.quiz.core.common.billing

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import com.msaitodev.core.common.billing.BillingManager
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class BillingManagerTest {

    private lateinit var context: Context
    private lateinit var billingManager: BillingManager

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        billingManager = BillingManager(context)
    }

    @Test
    fun setPremiumForDebug_updatesIsPremiumStateFlow() = runTest {
        // GIVEN: Initial state is false
        assertThat(billingManager.isPremium.value).isFalse()

        // WHEN: set premium to true for debug
        billingManager.setPremiumForDebug(true)

        // THEN: isPremium StateFlow should be updated to true
        assertThat(billingManager.isPremium.value).isTrue()

        // WHEN: set premium back to false for debug
        billingManager.setPremiumForDebug(false)

        // THEN: isPremium StateFlow should be updated to false
        assertThat(billingManager.isPremium.value).isFalse()
    }
}
