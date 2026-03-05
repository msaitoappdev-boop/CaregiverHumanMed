package com.msaitodev.quiz.core.data.repository

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.msaitodev.core.common.billing.BillingManager
import com.msaitodev.quiz.core.data.repository.PremiumRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock

class PremiumRepositoryImplTest {

    @Test
    fun isPremium_reflects_billing_manager_state() = runTest {
        // GIVEN: テスト用のMutableStateFlowを持つ、偽のBillingManagerを準備
        val fakeIsPremiumFlow = MutableStateFlow(false)
        val mockBillingManager = mock<BillingManager> {
            on { isPremium } doReturn fakeIsPremiumFlow
        }
        val repository = PremiumRepositoryImpl(mockBillingManager)

        // WHEN: isPremiumフローを購読する
        repository.isPremium.test {
            // THEN: 初期値としてfalseが流れてくることを確認
            assertThat(awaitItem()).isFalse()

            // WHEN: 偽BillingManagerの状態をtrueに変更する
            fakeIsPremiumFlow.value = true

            // THEN: 新しい値trueが流れてくることを確認
            assertThat(awaitItem()).isTrue()

            // FINALLY: Cancel the collector because the flow never completes
            cancelAndIgnoreRemainingEvents()
        }
    }
}
