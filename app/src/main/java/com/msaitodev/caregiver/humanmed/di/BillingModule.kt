package com.msaitodev.caregiver.humanmed.di

import android.content.Context
import com.msaitodev.caregiver.humanmed.R
import com.msaitodev.quiz.core.common.billing.BillingProvider
import com.msaitodev.quiz.core.common.billing.PaywallConfig
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BillingModule {

    @Provides
    @Singleton
    fun provideBillingProvider(@ApplicationContext context: Context): BillingProvider {
        return object : BillingProvider {
            // プロダクトIDなども strings.xml から取得（translatable="false" で定義済み）
            override val productIdPremium: String = context.getString(R.string.billing_product_id_premium)
            override val basePlanId: String = context.getString(R.string.billing_base_plan_id)
            
            override val paywallConfig: PaywallConfig = PaywallConfig(
                title = context.getString(R.string.paywall_title),
                headline = context.getString(R.string.paywall_headline),
                // strings.xml の \n を分割してリスト化
                benefits = context.getString(R.string.paywall_benefits).split("\n"),
                planTitle = context.getString(R.string.paywall_plan_title),
                planPrice = context.getString(R.string.paywall_plan_price),
                purchaseButtonText = context.getString(R.string.paywall_purchase_button_text),
                purchasedButtonText = context.getString(R.string.paywall_purchase_button_purchased),
                description = context.getString(R.string.paywall_purchase_dialog_description)
            )

            // BillingManager用のエラーメッセージを供給
            override val errorOfferNotFound: String = context.getString(R.string.billing_error_offer_not_found)
            override val errorAcknowledgeFailed: String = context.getString(R.string.billing_error_acknowledge_failed)
            override val errorPending: String = context.getString(R.string.billing_error_pending)
            override val errorGeneral: String = context.getString(R.string.billing_error_general)
        }
    }
}
