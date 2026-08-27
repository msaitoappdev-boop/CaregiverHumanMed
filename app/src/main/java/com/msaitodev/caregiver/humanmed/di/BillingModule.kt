package com.msaitodev.caregiver.humanmed.di

import android.content.Context
import com.msaitodev.quiz.feature.main.R
import com.msaitodev.core.common.billing.BillingProvider
import com.msaitodev.core.common.billing.PaywallConfig
import com.msaitodev.core.common.billing.PlanItemConfig
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
            override val productIdMonthly: String = context.getString(com.msaitodev.caregiver.humanmed.R.string.billing_product_id_premium)
            override val basePlanId: String = context.getString(com.msaitodev.caregiver.humanmed.R.string.billing_base_plan_id)
            override val productIdLifetime: String = context.getString(com.msaitodev.caregiver.humanmed.R.string.billing_product_id_lifetime)

            override val paywallConfig = PaywallConfig(
                title = context.getString(R.string.paywall_title),
                headline = context.getString(R.string.paywall_headline),
                monthly = PlanItemConfig(
                    planTitle = context.getString(R.string.paywall_monthly_plan_title),
                    planPrice = context.getString(R.string.paywall_monthly_plan_price),
                    purchaseButtonText = context.getString(R.string.paywall_monthly_purchase_button_text),
                    purchasedButtonText = context.getString(R.string.paywall_monthly_purchase_button_purchased),
                    description = context.getString(R.string.paywall_monthly_purchase_dialog_description),
                    benefits = context.getString(R.string.paywall_monthly_benefits).split("\\n")
                ),
                lifetime = PlanItemConfig(
                    planTitle = context.getString(R.string.paywall_lifetime_plan_title),
                    planPrice = context.getString(R.string.paywall_lifetime_plan_price),
                    purchaseButtonText = context.getString(R.string.paywall_lifetime_purchase_button_text),
                    purchasedButtonText = context.getString(R.string.paywall_lifetime_purchase_button_purchased),
                    description = context.getString(R.string.paywall_lifetime_purchase_dialog_description),
                    benefits = context.getString(R.string.paywall_lifetime_benefits).split("\\n")
                )
            )

            // BillingManager用のエラーメッセージを供給
            override val errorOfferNotFound: String = context.getString(R.string.billing_error_offer_not_found)
            override val errorAcknowledgeFailed: String = context.getString(R.string.billing_error_acknowledge_failed)
            override val errorPending: String = context.getString(R.string.billing_error_pending)
            override val errorGeneral: String = context.getString(R.string.billing_error_general)
        }
    }
}
